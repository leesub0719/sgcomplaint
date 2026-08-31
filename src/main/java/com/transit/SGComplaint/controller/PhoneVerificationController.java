package com.transit.SGComplaint.controller;

import com.transit.SGComplaint.DTO.PhoneCodeRequest;
import com.transit.SGComplaint.DTO.PhoneCodeVerifyRequest;
import com.transit.SGComplaint.DTO.PhoneVerificationResponse;
import com.transit.SGComplaint.service.PhoneVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phone-verifications")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationController(
            PhoneVerificationService phoneVerificationService) {
        this.phoneVerificationService = phoneVerificationService;
    }

    @PostMapping("/request")
    public PhoneVerificationResponse requestCode(
            @Valid @RequestBody PhoneCodeRequest request) {
        phoneVerificationService.requestCode(request.phone());
        return PhoneVerificationResponse.success(
                "인증번호를 문자로 발송했습니다.");
    }

    @PostMapping("/verify")
    public PhoneVerificationResponse verifyCode(
            @Valid @RequestBody PhoneCodeVerifyRequest request) {
        String token = phoneVerificationService.verifyCode(
                request.phone(), request.code());
        return PhoneVerificationResponse.verified(
                "휴대전화 인증이 완료되었습니다.", token);
    }
}
