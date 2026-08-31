package com.transit.SGComplaint.service;

import com.transit.SGComplaint.DTO.NoticeCreateRequest;
import com.transit.SGComplaint.DTO.NoticeDetail;
import com.transit.SGComplaint.DTO.NoticeItem;
import com.transit.SGComplaint.DTO.NoticePopupItem;
import com.transit.SGComplaint.DTO.StoredAttachment;
import com.transit.SGComplaint.domain.Employee;
import com.transit.SGComplaint.domain.Notice;
import com.transit.SGComplaint.domain.NoticeImage;
import com.transit.SGComplaint.repository.EmployeeRepository;
import com.transit.SGComplaint.repository.NoticeImageRepository;
import com.transit.SGComplaint.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class NoticeService {

    private static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Pattern IMAGE_MARKER_PATTERN = Pattern.compile("notice-image:([0-4])");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final NoticeRepository noticeRepository;
    private final EmployeeRepository employeeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final RichTextSanitizer richTextSanitizer;
    private final Path imageStorageRoot;

    public NoticeService(
            NoticeRepository noticeRepository,
            EmployeeRepository employeeRepository,
            NoticeImageRepository noticeImageRepository,
            RichTextSanitizer richTextSanitizer,
            @Value("${app.upload.notice-image-dir:uploads/notices}") String imageStorageDirectory) {
        this.noticeRepository = noticeRepository;
        this.employeeRepository = employeeRepository;
        this.noticeImageRepository = noticeImageRepository;
        this.richTextSanitizer = richTextSanitizer;
        this.imageStorageRoot = Path.of(imageStorageDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public Long createNotice(String administratorId, NoticeCreateRequest request) {
        Employee administrator = employeeRepository
                  .findByEmpIdAndEmpStatus(administratorId, "Y")
                  .filter(Employee::hasAdminRole)
                .orElseThrow(() -> new NoticeException("공지사항을 등록할 관리자 권한이 없습니다."));
        List<MultipartFile> images = request.getContentImages().stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList();
        validateImages(images);

        String sanitizedContent = richTextSanitizer.sanitize(request.getContent());
        int textLength = richTextSanitizer.plainTextLength(sanitizedContent);
        if (textLength == 0) throw new NoticeException("공지사항 내용을 입력해 주세요.");
        if (textLength > 3000) throw new NoticeException("내용은 3,000자 이내로 입력해 주세요.");
        validateImageMarkers(sanitizedContent, images.size());

        Notice notice = Notice.create(
                administrator,
                request.getCategory(),
                request.getTitle(),
                sanitizedContent,
                request.isPinned(),
                request.isPopup()
        );
        noticeRepository.saveAndFlush(notice);
        if (!images.isEmpty()) {
            notice.changeContent(saveContentImages(notice.getNoticeNo(), sanitizedContent, images));
        }
        return notice.getNoticeNo();
    }

    public Page<NoticeItem> getNoticePage(String keyword, int page) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return noticeRepository.searchNotices(
                        normalizedKeyword,
                        PageRequest.of(Math.max(0, page), 10)
                )
                .map(this::toItem);
    }

    public Page<NoticeItem> getAdminNoticePage(int page) {
        int requestedPage = Math.max(0, page);
        Page<Notice> noticePage = noticeRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(requestedPage, 10)
        );
        if (noticePage.getTotalPages() > 0 && requestedPage >= noticePage.getTotalPages()) {
            noticePage = noticeRepository.findAllByOrderByCreatedAtDesc(
                    PageRequest.of(noticePage.getTotalPages() - 1, 10)
            );
        }
        return noticePage.map(this::toItem);
    }

    public List<NoticeItem> getMainNotices() {
        return noticeRepository.findTop3ByOrderByPinnedDescCreatedAtDesc()
                .stream().map(this::toItem).toList();
    }

    public List<NoticePopupItem> getMainPopupNotices() {
        return noticeRepository.findByPopupOrderByCreatedAtDesc("Y").stream()
                .map(notice -> new NoticePopupItem(
                        notice.getNoticeNo(),
                        notice.getTitle(),
                        richTextSanitizer.sanitize(notice.getContent())
                ))
                .toList();
    }

    @Transactional
    public void changePopupSetting(String administratorId, Long noticeNo, boolean popup) {
        employeeRepository.findByEmpIdAndEmpStatus(administratorId, "Y")
                .filter(Employee::hasAdminRole)
                .orElseThrow(() -> new NoticeException("공지사항 설정을 변경할 관리자 권한이 없습니다."));
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new NoticeException("공지사항을 찾을 수 없습니다."));
        notice.changePopup(popup);
    }

    @Transactional
    public NoticeDetail getNoticeDetail(Long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new NoticeException("공지사항을 찾을 수 없습니다."));
        notice.increaseViewCount();
        return new NoticeDetail(
                notice.getNoticeNo(),
                categoryLabel(notice.getCategory()),
                notice.getTitle(),
                richTextSanitizer.sanitize(notice.getContent()),
                notice.getAdminName(),
                "Y".equals(notice.getPinned()),
                notice.getCreatedAt().format(DATE_TIME_FORMATTER),
                notice.getViewCount()
        );
    }

    public StoredAttachment getNoticeImage(Long noticeImageNo) {
        NoticeImage image = noticeImageRepository.findById(noticeImageNo)
                .orElseThrow(() -> new NoticeException("공지사항 이미지를 찾을 수 없습니다."));
        Path file = imageStorageRoot.resolve(image.getFilePath()).normalize();
        if (!file.startsWith(imageStorageRoot) || !Files.isRegularFile(file)) {
            throw new NoticeException("저장된 공지사항 이미지를 찾을 수 없습니다.");
        }
        return new StoredAttachment(file, image.getOriginalName(), image.getContentType());
    }

    private void validateImages(List<MultipartFile> images) {
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new NoticeException("본문 이미지는 최대 5장까지 등록할 수 있습니다.");
        }
        for (MultipartFile image : images) {
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new NoticeException("본문 이미지 한 장의 크기는 5MB를 넘을 수 없습니다.");
            }
            String originalName = safeOriginalName(image);
            String extension = extensionOf(originalName);
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                throw new NoticeException("JPG, PNG, GIF, WEBP 이미지만 등록할 수 있습니다.");
            }
            String contentType = image.getContentType() == null ? "" : image.getContentType();
            if (!contentType.startsWith("image/")) {
                throw new NoticeException("올바른 이미지 파일을 선택해 주세요.");
            }
        }
    }

    private void validateImageMarkers(String content, int imageCount) {
        Matcher matcher = IMAGE_MARKER_PATTERN.matcher(content);
        while (matcher.find()) {
            if (Integer.parseInt(matcher.group(1)) >= imageCount) {
                throw new NoticeException("본문 이미지 정보가 올바르지 않습니다. 이미지를 다시 삽입해 주세요.");
            }
        }
        for (int index = 0; index < imageCount; index++) {
            if (!content.contains("notice-image:" + index)) {
                throw new NoticeException("본문에서 제거한 이미지를 다시 확인해 주세요.");
            }
        }
    }

    private String saveContentImages(Long noticeNo, String content, List<MultipartFile> images) {
        Path noticeDirectory = imageStorageRoot.resolve(String.valueOf(noticeNo)).normalize();
        ensureInsideStorage(noticeDirectory);
        List<Path> savedPaths = new ArrayList<>();
        String resolvedContent = content;
        try {
            Files.createDirectories(noticeDirectory);
            for (int index = 0; index < images.size(); index++) {
                MultipartFile image = images.get(index);
                String originalName = safeOriginalName(image);
                String extension = extensionOf(originalName);
                String storedName = UUID.randomUUID() + "." + extension;
                Path destination = noticeDirectory.resolve(storedName).normalize();
                ensureInsideStorage(destination);
                Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add(destination);

                String relativePath = imageStorageRoot.relativize(destination).toString().replace('\\', '/');
                NoticeImage savedImage = noticeImageRepository.saveAndFlush(NoticeImage.create(
                        noticeNo, originalName, storedName, relativePath,
                        image.getContentType(), image.getSize()
                ));
                resolvedContent = resolvedContent.replace(
                        "notice-image:" + index,
                        "/notices/images/" + savedImage.getNoticeImageNo()
                );
            }
            return resolvedContent;
        } catch (IOException | RuntimeException exception) {
            savedPaths.forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) { }
            });
            if (exception instanceof NoticeException noticeException) throw noticeException;
            throw new NoticeException("공지사항 이미지 저장 중 오류가 발생했습니다.");
        }
    }

    private String safeOriginalName(MultipartFile image) {
        String original = image.getOriginalFilename() == null ? "" : image.getOriginalFilename();
        String normalized = original.replace('\\', '/');
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        filename = Normalizer.normalize(filename, Normalizer.Form.NFC);
        if (!StringUtils.hasText(filename) || filename.length() > 255
                || filename.chars().anyMatch(character -> character < 32 || character == 127)) {
            throw new NoticeException("올바르지 않은 이미지 파일명입니다.");
        }
        return filename;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void ensureInsideStorage(Path path) {
        if (!path.startsWith(imageStorageRoot)) {
            throw new NoticeException("올바르지 않은 공지사항 이미지 저장 경로입니다.");
        }
    }

    private NoticeItem toItem(Notice notice) {
        return new NoticeItem(
                notice.getNoticeNo(),
                categoryLabel(notice.getCategory()),
                notice.getTitle(),
                "Y".equals(notice.getPinned()),
                "Y".equals(notice.getPopup()),
                notice.getCreatedAt().format(DATE_FORMATTER),
                notice.getViewCount()
        );
    }

    private String categoryLabel(String category) {
        return switch (category) {
            case "SYSTEM" -> "시스템 점검";
            case "SERVICE" -> "서비스 변경";
            default -> "일반 안내";
        };
    }
}
