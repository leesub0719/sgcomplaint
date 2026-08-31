package com.transit.SGComplaint.service;

import com.transit.SGComplaint.DTO.EmployeeSignupRequest;
import com.transit.SGComplaint.domain.Employee;
import com.transit.SGComplaint.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            PhoneVerificationService phoneVerificationService,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.phoneVerificationService = phoneVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isDuplicateId(String empId) {
        if (empId == null) {
            return false;
        }
        return employeeRepository.existsByEmpId(
                empId.trim().toLowerCase());
    }

    public boolean isEmpIdAvailable(String empId) {
        if (empId == null || !empId.matches("^[a-z0-9]{4,20}$")) {
            return false;
        }
        return !isDuplicateId(empId);
    }

    public String getActiveEmployeeName(String empId) {
        return getRequiredActiveEmployee(empId).getEmpName();
    }

    public Employee getRequiredActiveEmployee(String empId) {
        return employeeRepository.findByEmpIdAndEmpStatus(empId, "Y")
                .orElseThrow(() -> new IllegalStateException(
                        "로그인 회원 정보를 찾을 수 없습니다."
                ));
    }

    public boolean isActiveAdministrator(String empId) {
        return employeeRepository.findByEmpIdAndEmpStatus(empId, "Y")
                .map(Employee::hasAdminRole)
                .orElse(false);
    }

    @Transactional
    public Long signupUser(EmployeeSignupRequest request) {
        return signup(request, false);
    }

    @Transactional
    public Long signupAdmin(EmployeeSignupRequest request) {
        return signup(request, true);
    }

    private Long signup(EmployeeSignupRequest request, boolean admin) {
        if (!request.passwordMatches()) {
            throw new IllegalArgumentException(
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String empId = request.getEmpId().trim().toLowerCase();
        String phone = PhoneNumberUtils.normalize(request.getEmpPhone());

        if (employeeRepository.existsByEmpId(empId)) {
            throw new DuplicateEmployeeIdException(
                    "이미 사용 중인 아이디입니다.");
        }

        // 인증에 성공한 휴대전화와 서버가 발급한 토큰이 일치해야 가입할 수 있습니다.
        phoneVerificationService.consumeVerification(
                phone, request.getPhoneVerificationToken());

        String encodedPassword = passwordEncoder.encode(
                request.getEmpPassword());

        Employee employee = admin
                ? Employee.createAdmin(
                    empId, encodedPassword, request.getEmpName(),
                    request.getEmpEmail(), phone, request.fullAddress())
                : Employee.createUser(
                    empId, encodedPassword, request.getEmpName(),
                    request.getEmpEmail(), phone, request.fullAddress());

        try {
            return employeeRepository.saveAndFlush(employee).getEmpNo();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmployeeIdException(
                    "이미 사용 중인 아이디입니다.");
        }
    }
}
