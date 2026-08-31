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
@Table(name = "sgtransit_employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "emp_id", nullable = false, unique = true, length = 20)
    private String empId;

    @Column(name = "emp_password", nullable = false, length = 100)
    private String empPassword;

    @Column(name = "emp_name", nullable = false, length = 50)
    private String empName;

    @Column(name = "emp_email", nullable = false, length = 100)
    private String empEmail;

    @Column(name = "emp_phone", nullable = false, length = 20)
    private String empPhone;

    @Column(name = "emp_address", nullable = false, length = 255)
    private String empAddress;

    @Column(name = "emp_role", nullable = false, length = 1)
    private String empRole;

    @Column(name = "emp_status", nullable = false, length = 1)
    private String empStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Employee() {
    }

    public static Employee createUser(
            String empId,
            String encodedPassword,
            String empName,
            String empEmail,
            String empPhone,
            String empAddress) {

        Employee employee = new Employee();
        employee.empId = empId;
        employee.empPassword = encodedPassword;
        employee.empName = empName;
        employee.empEmail = empEmail;
        employee.empPhone = empPhone;
        employee.empAddress = empAddress;
        employee.empRole = "U";
        employee.empStatus = "Y";
        return employee;
    }

    public static Employee createAdmin(
            String empId,
            String encodedPassword,
            String empName,
            String empEmail,
            String empPhone,
            String empAddress) {

        Employee employee = createUser(
                empId, encodedPassword, empName,
                empEmail, empPhone, empAddress);
        employee.empRole = "A";
        return employee;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getEmpNo() { return empNo; }
    public String getEmpId() { return empId; }
    public String getEmpPassword() { return empPassword; }
    public String getEmpName() { return empName; }
    public String getEmpEmail() { return empEmail; }
    public String getEmpPhone() { return empPhone; }
    public String getEmpAddress() { return empAddress; }
    public String getEmpRole() { return empRole; }
    public String getEmpStatus() { return empStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void changeRole(String role) {
        if (!"U".equals(role) && !"A".equals(role) && !"M".equals(role)) {
            throw new IllegalArgumentException("회원 권한은 U, A 또는 M만 가능합니다.");
        }
        this.empRole = role;
    }

    public boolean hasAdminRole() {
        return "A".equals(empRole) || "M".equals(empRole);
    }

    public boolean isMaster() {
        return "M".equals(empRole);
    }
}
