package com.transit.SGComplaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sgtransit_notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_no")
    private Long noticeNo;

    @Column(name = "admin_emp_no", nullable = false)
    private Long adminEmpNo;

    @Column(name = "admin_name", nullable = false, length = 50)
    private String adminName;

    @Column(name = "notice_category", nullable = false, length = 20)
    private String category;

    @Column(name = "notice_title", nullable = false, length = 100)
    private String title;

    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_pinned", nullable = false, length = 1)
    private String pinned;

    @Column(name = "is_popup", nullable = false, length = 1)
    private String popup;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Notice() {
    }

    public static Notice create(
            Employee administrator,
            String category,
            String title,
            String content,
            boolean pinned,
            boolean popup) {
        Notice notice = new Notice();
        notice.adminEmpNo = administrator.getEmpNo();
        notice.adminName = administrator.getEmpName();
        notice.category = category;
        notice.title = title.trim();
        notice.content = content.trim();
        notice.pinned = pinned ? "Y" : "N";
        notice.popup = popup ? "Y" : "N";
        notice.viewCount = 0L;
        return notice;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public void increaseViewCount() {
        viewCount = viewCount == null ? 1L : viewCount + 1L;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changePopup(boolean popup) {
        this.popup = popup ? "Y" : "N";
    }

    public Long getNoticeNo() { return noticeNo; }
    public String getAdminName() { return adminName; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPinned() { return pinned; }
    public String getPopup() { return popup; }
    public Long getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
