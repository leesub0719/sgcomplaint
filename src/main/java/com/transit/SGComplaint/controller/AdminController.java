package com.transit.SGComplaint.controller;

import com.transit.SGComplaint.DTO.AdminComplaintItem;
import com.transit.SGComplaint.DTO.AdminMemberItem;
import com.transit.SGComplaint.DTO.StoredAttachment;
import com.transit.SGComplaint.DTO.NoticeCreateRequest;
import com.transit.SGComplaint.DTO.NoticeItem;
import com.transit.SGComplaint.domain.ComplaintStatus;
import com.transit.SGComplaint.domain.Employee;
import com.transit.SGComplaint.service.AdminComplaintService;
import com.transit.SGComplaint.service.AdminMemberService;
import com.transit.SGComplaint.service.ComplaintException;
import com.transit.SGComplaint.service.EmployeeService;
import com.transit.SGComplaint.service.NoticeException;
import com.transit.SGComplaint.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EmployeeService employeeService;
    private final AdminComplaintService adminComplaintService;
    private final AdminMemberService adminMemberService;
    private final NoticeService noticeService;

    public AdminController(
            EmployeeService employeeService,
            AdminComplaintService adminComplaintService,
            AdminMemberService adminMemberService,
            NoticeService noticeService) {
        this.employeeService = employeeService;
        this.adminComplaintService = adminComplaintService;
        this.adminMemberService = adminMemberService;
        this.noticeService = noticeService;
    }

    @GetMapping
    public String dashboard(
            Authentication authentication,
            @RequestParam(name = "status", required = false) String statusValue,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        ComplaintStatus selectedStatus = parseStatus(statusValue);
        Page<AdminComplaintItem> complaintPage =
                adminComplaintService.getDashboardComplaints(selectedStatus, page);

        addAdministrator(authentication, model);
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("totalComplaints", adminComplaintService.countAllComplaints());
        model.addAttribute("checkingCount", adminComplaintService.countComplaints(ComplaintStatus.CHECKING));
        model.addAttribute("processingCount", adminComplaintService.countComplaints(ComplaintStatus.PROCESSING));
        model.addAttribute("completedCount", adminComplaintService.countComplaints(ComplaintStatus.COMPLETED));
        model.addAttribute("activeMemberCount", adminComplaintService.countActiveMembers());
        model.addAttribute("selectedStatus", selectedStatus == null ? "ALL" : selectedStatus.name());
        model.addAttribute("selectedStatusLabel", selectedStatus == null ? "전체" : selectedStatus.getLabel());
        model.addAttribute("complaintPage", complaintPage);
        model.addAttribute("recentComplaints", complaintPage.getContent());
        return "admin/dashboard";
    }

    @GetMapping("/complaints")
    public String complaints(
            Authentication authentication,
            @RequestParam(name = "status", required = false) String statusValue,
            Model model) {

        ComplaintStatus selectedStatus = parseStatus(statusValue);
        List<AdminComplaintItem> complaints =
                adminComplaintService.getComplaints(selectedStatus);

        addAdministrator(authentication, model);
        model.addAttribute("activeMenu", "complaints");
        model.addAttribute("selectedStatus", selectedStatus == null ? "ALL" : selectedStatus.name());
        model.addAttribute("complaints", complaints);
        model.addAttribute("complaintCount", complaints.size());
        return "admin/complaints";
    }

    @PostMapping("/complaints/{complaintNo}")
    public String updateComplaint(
            Authentication authentication,
            @PathVariable(name = "complaintNo") Long complaintNo,
            @RequestParam(name = "status") ComplaintStatus status,
            @RequestParam(name = "answerContent") String answerContent,
            @RequestParam(name = "answerAttachments", required = false)
            List<MultipartFile> answerAttachments,
            RedirectAttributes redirectAttributes) {

        try {
            adminComplaintService.answerComplaint(
                    authentication.getName(),
                    complaintNo,
                    status,
                    answerContent,
                    answerAttachments
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "민원 답변과 처리상태가 저장되었습니다."
            );
        } catch (ComplaintException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }
        return "redirect:/admin/complaints";
    }

    @GetMapping("/complaints/attachments/{attachmentNo}")
    public ResponseEntity<Resource> downloadComplaintAttachment(
            @PathVariable(name = "attachmentNo") Long attachmentNo) {
        return download(adminComplaintService.getComplaintAttachment(attachmentNo));
    }

    @GetMapping("/answers/attachments/{attachmentNo}")
    public ResponseEntity<Resource> downloadAnswerAttachment(
            @PathVariable(name = "attachmentNo") Long attachmentNo) {
        return download(adminComplaintService.getAnswerAttachment(attachmentNo));
    }

    @GetMapping("/notices")
    public String notices(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        Page<NoticeItem> noticePage = noticeService.getAdminNoticePage(page);
        addAdministrator(authentication, model);
        model.addAttribute("activeMenu", "notices");
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("notices", noticePage.getContent());
        int endPage = Math.min(Math.max(0, noticePage.getTotalPages() - 1), noticePage.getNumber() + 2);
        int startPage = Math.max(0, endPage - 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "admin/notices";
    }

    @GetMapping("/notices/new")
    public String newNotice(Authentication authentication, Model model) {
        addAdministrator(authentication, model);
        model.addAttribute("activeMenu", "notices");
        if (!model.containsAttribute("noticeRequest")) {
            model.addAttribute("noticeRequest", new NoticeCreateRequest());
        }
        return "admin/notice-form";
    }

    @PostMapping("/notices")
    public String createNotice(
            Authentication authentication,
            @Valid @ModelAttribute("noticeRequest") NoticeCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("공지사항 입력 내용을 확인해 주세요.");
            redirectAttributes.addFlashAttribute("errorMessage", message);
            return "redirect:/admin/notices/new";
        }
        try {
            Long noticeNo = noticeService.createNotice(authentication.getName(), request);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "공지사항 " + noticeNo + "번이 등록되었습니다."
            );
        } catch (NoticeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/notices/new";
        }
        return "redirect:/admin/notices";
    }

    @PostMapping("/notices/{noticeNo}/popup")
    public String changeNoticePopup(
            Authentication authentication,
            @PathVariable(name = "noticeNo") Long noticeNo,
            @RequestParam(name = "popup", defaultValue = "false") boolean popup,
            @RequestParam(name = "page", defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            noticeService.changePopupSetting(authentication.getName(), noticeNo, popup);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    popup ? "메인 공지 팝업을 사용하도록 변경했습니다." : "메인 공지 팝업을 해제했습니다."
            );
        } catch (NoticeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/notices?page=" + Math.max(0, page);
    }

    @GetMapping("/members")
    public String members(
            Authentication authentication,
            @RequestParam(name = "memberStatus", defaultValue = "ALL") String memberStatus,
            @RequestParam(name = "memberRole", defaultValue = "ALL") String memberRole,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model) {
        List<AdminMemberItem> members = adminMemberService.searchMembers(
                memberStatus,
                memberRole,
                keyword,
                authentication.getName()
        );
        addAdministrator(authentication, model);
        model.addAttribute("activeMenu", "members");
        model.addAttribute("totalMemberCount", adminMemberService.countAllMembers());
        model.addAttribute("activeMemberCount", adminMemberService.countActiveMembers());
        model.addAttribute("withdrawnMemberCount", adminMemberService.countWithdrawnMembers());
        model.addAttribute("administratorCount", adminMemberService.countAdministrators());
        model.addAttribute("masterCount", adminMemberService.countMasters());
        model.addAttribute("members", members);
        model.addAttribute("searchCount", members.size());
        model.addAttribute("selectedMemberStatus", memberStatus);
        model.addAttribute("selectedMemberRole", memberRole);
        model.addAttribute("keyword", keyword);
        return "admin/members";
    }

    @PostMapping("/members/{empNo}/role")
    public String changeMemberRole(
            Authentication authentication,
            @PathVariable(name = "empNo") Long empNo,
            @RequestParam(name = "role") String role,
            @RequestParam(name = "memberStatus", defaultValue = "ALL") String memberStatus,
            @RequestParam(name = "memberRole", defaultValue = "ALL") String memberRole,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes) {
        try {
            String message = adminMemberService.changeMemberRole(
                    authentication.getName(),
                    empNo,
                    role
            );
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        redirectAttributes.addAttribute("memberStatus", memberStatus);
        redirectAttributes.addAttribute("memberRole", memberRole);
        redirectAttributes.addAttribute("keyword", keyword);
        return "redirect:/admin/members";
    }

    private void addAdministrator(Authentication authentication, Model model) {
        Employee administrator = employeeService
                .getRequiredActiveEmployee(authentication.getName());
        model.addAttribute("adminName", administrator.getEmpName());
        model.addAttribute("adminRole", administrator.getEmpRole());
        model.addAttribute("isMaster", administrator.isMaster());
    }

    private ComplaintStatus parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank() || "ALL".equals(statusValue)) {
            return null;
        }
        try {
            return ComplaintStatus.valueOf(statusValue);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private ResponseEntity<Resource> download(StoredAttachment attachment) {
        FileSystemResource resource = new FileSystemResource(attachment.path());
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.contentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(attachment.contentType());
            } catch (IllegalArgumentException ignored) {
                // 알 수 없는 MIME 타입은 일반 바이너리 다운로드로 처리합니다.
            }
        }
        String disposition = ContentDisposition.attachment()
                .filename(attachment.originalName(), StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }
}
