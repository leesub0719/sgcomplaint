package com.transit.SGComplaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sgtransit_complaint")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_no")
    private Long complaintNo;

    @Column(name = "emp_no", nullable = false)
    private Long empNo;

    @Column(name = "emp_id", nullable = false, length = 20)
    private String empId;

    @Column(name = "emp_name", nullable = false, length = 50)
    private String empName;

    @Column(name = "emp_phone", nullable = false, length = 20)
    private String empPhone;

    @Column(name = "complaint_category", nullable = false, length = 20)
    private String category;

    @Column(name = "complaint_title", nullable = false, length = 100)
    private String title;

    @Column(name = "complaint_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "complaint_password", nullable = false, length = 100)
    private String password;

    @Column(name = "complaint_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Complaint() {
    }

    public static Complaint create(
            Employee employee,
            String category,
            String title,
            String content,
            String password) {

        Complaint complaint = new Complaint();
        complaint.empNo = employee.getEmpNo();
        complaint.empId = employee.getEmpId();
        complaint.empName = employee.getEmpName();
        complaint.empPhone = employee.getEmpPhone();
        complaint.category = category;
        complaint.title = title.trim();
        complaint.content = content;
        complaint.password = password;
        complaint.status = ComplaintStatus.CHECKING;
        return complaint;
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

    public Long getComplaintNo() { return complaintNo; }
    public Long getEmpNo() { return empNo; }
    public String getEmpId() { return empId; }
    public String getEmpName() { return empName; }
    public String getEmpPhone() { return empPhone; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPassword() { return password; }
    public ComplaintStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void changeStatus(ComplaintStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("민원 처리상태는 필수입니다.");
        }
        this.status = status;
    }
}
