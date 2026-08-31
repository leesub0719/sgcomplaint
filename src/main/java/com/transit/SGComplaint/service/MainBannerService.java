package com.transit.SGComplaint.service;

import com.transit.SGComplaint.DTO.MainBannerItem;
import com.transit.SGComplaint.DTO.StoredAttachment;
import com.transit.SGComplaint.domain.Employee;
import com.transit.SGComplaint.domain.MainBanner;
import com.transit.SGComplaint.repository.EmployeeRepository;
import com.transit.SGComplaint.repository.MainBannerRepository;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@Transactional(readOnly = true)
public class MainBannerService {

    private static final int MAX_BANNER_COUNT = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    private final MainBannerRepository bannerRepository;
    private final EmployeeRepository employeeRepository;
    private final Path storageRoot;

    public MainBannerService(
            MainBannerRepository bannerRepository,
            EmployeeRepository employeeRepository,
            @Value("${app.upload.main-banner-dir:uploads/main-banners}")
            String storageDirectory) {
        this.bannerRepository = bannerRepository;
        this.employeeRepository = employeeRepository;
        this.storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public List<MainBannerItem> getBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAscBannerNoAsc()
                .stream()
                .map(this::toItem)
                .toList();
    }

    public long countBanners() {
        return bannerRepository.count();
    }

    public int remainingCount() {
        return Math.max(0, MAX_BANNER_COUNT - (int) bannerRepository.count());
    }

    @Transactional
    public int addBanners(String loginId, List<MultipartFile> files) {
        requireAdministrator(loginId);
        List<MultipartFile> uploadFiles = files == null
                ? List.of()
                : files.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (uploadFiles.isEmpty()) {
            throw new IllegalArgumentException("등록할 메인 배너 이미지를 선택해 주세요.");
        }
        long currentCount = bannerRepository.count();
        if (currentCount + uploadFiles.size() > MAX_BANNER_COUNT) {
            throw new IllegalArgumentException(
                    "메인 배너는 최대 5장까지 등록할 수 있습니다. 현재 "
                            + currentCount + "장이 등록되어 있습니다."
            );
        }
        uploadFiles.forEach(this::validateFile);

        int nextOrder = bannerRepository.findTopByOrderByDisplayOrderDescBannerNoDesc()
                .map(banner -> banner.getDisplayOrder() + 1)
                .orElse(1);
        List<Path> savedPaths = new ArrayList<>();
        try {
            Files.createDirectories(storageRoot);
            for (MultipartFile file : uploadFiles) {
                String originalName = safeOriginalName(file);
                String extension = extensionOf(originalName);
                String storedName = UUID.randomUUID() + "." + extension;
                Path destination = storageRoot.resolve(storedName).normalize();
                ensureInsideStorage(destination);
                Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add(destination);

                MainBanner banner = MainBanner.create(
                        originalName,
                        storedName,
                        storedName,
                        contentType(file, extension),
                        file.getSize(),
                        nextOrder++,
                        loginId
                );
                bannerRepository.save(banner);
            }
            bannerRepository.flush();
            return uploadFiles.size();
        } catch (IOException | RuntimeException exception) {
            savedPaths.forEach(this::deleteFileQuietly);
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalStateException("메인 배너 저장 중 오류가 발생했습니다.", exception);
        }
    }

    @Transactional
    public void deleteBanner(String loginId, Long bannerNo) {
        requireAdministrator(loginId);
        MainBanner banner = bannerRepository.findById(bannerNo)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 배너를 찾을 수 없습니다."));
        Path file = resolveStoredFile(banner);
        bannerRepository.delete(banner);
        bannerRepository.flush();
        deleteFileQuietly(file);
    }

    public StoredAttachment getBannerImage(Long bannerNo) {
        MainBanner banner = bannerRepository.findById(bannerNo)
                .orElseThrow(() -> new IllegalArgumentException("배너 이미지를 찾을 수 없습니다."));
        Path file = resolveStoredFile(banner);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("저장된 배너 이미지를 찾을 수 없습니다.");
        }
        return new StoredAttachment(file, banner.getOriginalName(), banner.getContentType());
    }

    private MainBannerItem toItem(MainBanner banner) {
        return new MainBannerItem(
                banner.getBannerNo(),
                banner.getOriginalName(),
                formatFileSize(banner.getFileSize()),
                banner.getCreatedBy(),
                banner.getCreatedAt().format(DATE_TIME_FORMATTER),
                "/main-banners/" + banner.getBannerNo() + "/image"
        );
    }

    private Employee requireAdministrator(String loginId) {
        return employeeRepository.findByEmpIdAndEmpStatus(loginId, "Y")
                .filter(Employee::hasAdminRole)
                .orElseThrow(() -> new IllegalStateException("관리자 권한을 확인할 수 없습니다."));
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("배너 이미지 한 장은 10MB를 넘을 수 없습니다.");
        }
        String originalName = safeOriginalName(file);
        if (!ALLOWED_EXTENSIONS.contains(extensionOf(originalName))) {
            throw new IllegalArgumentException("JPG, PNG, WEBP 이미지만 등록할 수 있습니다.");
        }
    }

    private String safeOriginalName(MultipartFile file) {
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String normalizedPath = originalName.replace('\\', '/');
        int separator = normalizedPath.lastIndexOf('/');
        String filename = separator >= 0 ? normalizedPath.substring(separator + 1) : normalizedPath;
        filename = Normalizer.normalize(filename.trim(), Normalizer.Form.NFC);
        boolean hasControlCharacter = filename.chars()
                .anyMatch(character -> character < 32 || character == 127);
        if (!StringUtils.hasText(filename) || ".".equals(filename) || "..".equals(filename)
                || filename.length() > 255 || hasControlCharacter) {
            throw new IllegalArgumentException("올바르지 않은 배너 이미지 파일명입니다.");
        }
        return filename;
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) return "";
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String contentType(MultipartFile file, String extension) {
        if (StringUtils.hasText(file.getContentType())
                && file.getContentType().startsWith("image/")) {
            return file.getContentType();
        }
        return switch (extension) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    private Path resolveStoredFile(MainBanner banner) {
        Path file = storageRoot.resolve(banner.getFilePath()).normalize();
        ensureInsideStorage(file);
        return file;
    }

    private void ensureInsideStorage(Path path) {
        if (!path.startsWith(storageRoot)) {
            throw new IllegalArgumentException("올바르지 않은 배너 저장 경로입니다.");
        }
    }

    private void deleteFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // DB 처리를 유지하고 남은 파일은 운영 정리 대상으로 둡니다.
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
