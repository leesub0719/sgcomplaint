package com.transit.SGComplaint.controller;

import com.transit.SGComplaint.DTO.EmployeeSignupRequest;
import com.transit.SGComplaint.service.DuplicateEmployeeIdException;
import com.transit.SGComplaint.service.EmployeeService;
import com.transit.SGComplaint.service.PhoneVerificationException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class MemberController {

    private final EmployeeService employeeService;

    public MemberController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new EmployeeSignupRequest());
        }
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupForm") EmployeeSignupRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (!request.passwordMatches()) {
            bindingResult.rejectValue(
                    "passwordConfirm",
                    "password.mismatch",
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }

        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        try {
            Long empNo = employeeService.signupUser(request);
            redirectAttributes.addFlashAttribute("empNo", empNo);
            return "redirect:/signup/complete";
        } catch (DuplicateEmployeeIdException exception) {
            bindingResult.rejectValue(
                    "empId",
                    "empId.duplicate",
                    exception.getMessage()
            );
            return "member/signup";
        } catch (PhoneVerificationException exception) {
            bindingResult.rejectValue(
                    "phoneVerificationToken",
                    "phone.verification.invalid",
                    exception.getMessage()
            );
            return "member/signup";
        }
    }

    @GetMapping("/signup/complete")
    public String signupComplete() {
        return "member/signup-complete";
    }

    @GetMapping("/api/members/check-id")
    @ResponseBody
    public Map<String, Boolean> checkDuplicateId(
            @RequestParam(name = "empId") String empId) {

        boolean available = employeeService.isEmpIdAvailable(empId);
        return Map.of("available", available);
    }
}
