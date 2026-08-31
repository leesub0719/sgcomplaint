package com.transit.SGComplaint.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmployeeSignupRequest {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Pattern(
        regexp = "^[a-z0-9]{4,20}$",
        message = "아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요."
    )
    private String empId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 72, message = "비밀번호는 8~72자로 입력해 주세요.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).+$",
        message = "비밀번호에는 영문과 숫자가 포함되어야 합니다."
    )
    private String empPassword;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
    private String empName;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "올바른 이메일 형식으로 입력해 주세요.")
    @Size(max = 100, message = "이메일은 100자 이내로 입력해 주세요.")
    private String empEmail;

    @NotBlank(message = "휴대전화 번호를 입력해 주세요.")
    @Pattern(
        regexp = "^01[0-9]{8,9}$",
        message = "휴대전화 번호는 숫자만 입력해 주세요."
    )
    private String empPhone;

    @NotBlank(message = "휴대전화 인증을 완료해 주세요.")
    private String phoneVerificationToken;

    @NotBlank(message = "우편번호를 입력해 주세요.")
    private String postcode;

    @NotBlank(message = "주소를 입력해 주세요.")
    private String address;

    @NotBlank(message = "상세 주소를 입력해 주세요.")
    @Size(max = 100, message = "상세 주소는 100자 이내로 입력해 주세요.")
    private String addressDetail;

    public boolean passwordMatches() {
        return empPassword != null && empPassword.equals(passwordConfirm);
    }

    public String fullAddress() {
        return String.format("(%s) %s %s", postcode, address, addressDetail).trim();
    }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getEmpPassword() { return empPassword; }
    public void setEmpPassword(String empPassword) { this.empPassword = empPassword; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
    public String getEmpEmail() { return empEmail; }
    public void setEmpEmail(String empEmail) { this.empEmail = empEmail; }
    public String getEmpPhone() { return empPhone; }
    public void setEmpPhone(String empPhone) { this.empPhone = empPhone; }
    public String getPhoneVerificationToken() { return phoneVerificationToken; }
    public void setPhoneVerificationToken(String phoneVerificationToken) { this.phoneVerificationToken = phoneVerificationToken; }
    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
}
