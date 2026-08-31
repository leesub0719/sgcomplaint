package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository
        extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneOrderByRequestedAtDesc(String phone);

    Optional<PhoneVerification> findByPhoneAndVerificationTokenHashAndUsedAtIsNull(
            String phone, String verificationTokenHash);
}
