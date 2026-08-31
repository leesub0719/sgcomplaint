package com.transit.SGComplaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sgtransit_phone_verification")
public class PhoneVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_no")
    private Long verificationNo;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "verification_token_hash", length = 64)
    private String verificationTokenHash;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "code_expires_at", nullable = false)
    private LocalDateTime codeExpiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    protected PhoneVerification() {
    }

    public static PhoneVerification create(
            String phone,
            String codeHash,
            LocalDateTime codeExpiresAt) {

        PhoneVerification verification = new PhoneVerification();
        verification.phone = phone;
        verification.codeHash = codeHash;
        verification.failedAttempts = 0;
        verification.requestedAt = now();
        verification.codeExpiresAt = codeExpiresAt;
        return verification;
    }

    public boolean isCodeExpired(LocalDateTime currentTime) {
        return !currentTime.isBefore(codeExpiresAt);
    }

    public boolean hasReachedAttemptLimit(int maxAttempts) {
        return failedAttempts >= maxAttempts;
    }

    public void recordFailedAttempt() { failedAttempts += 1; }

    public void markVerified(String tokenHash, LocalDateTime tokenExpiresAt) {
        this.verificationTokenHash = tokenHash;
        this.verifiedAt = now();
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public boolean isTokenExpired(LocalDateTime currentTime) {
        return tokenExpiresAt == null || !currentTime.isBefore(tokenExpiresAt);
    }

    public boolean isAlreadyUsed() { return usedAt != null; }
    public void markUsed() { usedAt = now(); }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getCodeHash() { return codeHash; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
}
