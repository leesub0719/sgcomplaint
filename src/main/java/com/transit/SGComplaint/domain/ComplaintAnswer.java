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
@Table(name = "sgtransit_complaint_answer")
public class ComplaintAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_no")
    private Long answerNo;

    @Column(name = "complaint_no", nullable = false, unique = true)
    private Long complaintNo;

    @Column(name = "admin_emp_no", nullable = false)
    private Long adminEmpNo;

    @Column(name = "admin_name", nullable = false, length = 50)
    private String adminName;

    @Column(name = "answer_content", nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ComplaintAnswer() {
    }

    public static ComplaintAnswer create(
            Long complaintNo,
            Employee administrator,
            String answerContent) {
        ComplaintAnswer answer = new ComplaintAnswer();
        answer.complaintNo = complaintNo;
        answer.adminEmpNo = administrator.getEmpNo();
        answer.adminName = administrator.getEmpName();
        answer.answerContent = answerContent.trim();
        return answer;
    }

    public void update(Employee administrator, String answerContent) {
        adminEmpNo = administrator.getEmpNo();
        adminName = administrator.getEmpName();
        this.answerContent = answerContent.trim();
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

    public Long getAnswerNo() { return answerNo; }
    public Long getComplaintNo() { return complaintNo; }
    public String getAdminName() { return adminName; }
    public String getAnswerContent() { return answerContent; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
