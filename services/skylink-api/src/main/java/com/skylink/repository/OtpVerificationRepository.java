package com.skylink.repository;

import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import com.skylink.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification>
    findTopByIdentifierAndOtpTypeAndOtpPurposeOrderByCreatedAtDesc(
            String identifier,
            OtpType otpType,
            OtpPurpose otpPurpose
    );
}