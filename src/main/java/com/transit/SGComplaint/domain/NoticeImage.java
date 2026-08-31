package com.transit.SGComplaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sgtransit_notice_image")
public class NoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_image_no")
    private Long noticeImageNo;

    @Column(name = "notice_no", nullable = false)
    private Long noticeNo;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NoticeImage() {
    }

    public static NoticeImage create(
            Long noticeNo,
            String originalName,
            String storedName,
            String filePath,
            String contentType,
            long fileSize) {
        NoticeImage image = new NoticeImage();
        image.noticeNo = noticeNo;
        image.originalName = originalName;
        image.storedName = storedName;
        image.filePath = filePath;
        image.contentType = contentType;
        image.fileSize = fileSize;
        return image;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getNoticeImageNo() { return noticeImageNo; }
    public Long getNoticeNo() { return noticeNo; }
    public String getOriginalName() { return originalName; }
    public String getFilePath() { return filePath; }
    public String getContentType() { return contentType; }
}
