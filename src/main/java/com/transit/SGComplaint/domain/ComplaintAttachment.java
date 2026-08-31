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
@Table(name = "sgtransit_complaint_attachment")
public class ComplaintAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_no")
    private Long attachmentNo;

    @Column(name = "complaint_no", nullable = false)
    private Long complaintNo;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ComplaintAttachment() {
    }

    public static ComplaintAttachment create(
            Long complaintNo,
            String originalName,
            String storedName,
            String filePath,
            String contentType,
            long fileSize) {

        ComplaintAttachment attachment = new ComplaintAttachment();
        attachment.complaintNo = complaintNo;
        attachment.originalName = originalName;
        attachment.storedName = storedName;
        attachment.filePath = filePath;
        attachment.contentType = contentType;
        attachment.fileSize = fileSize;
        return attachment;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getAttachmentNo() { return attachmentNo; }
    public Long getComplaintNo() { return complaintNo; }
    public String getOriginalName() { return originalName; }
    public String getFilePath() { return filePath; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
}
