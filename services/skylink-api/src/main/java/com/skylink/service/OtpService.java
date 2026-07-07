package com.skylink.service;

import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import com.skylink.entity.OtpVerification;
import com.skylink.repository.OtpVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MAX_RESEND_COUNT = 5;

    private final OtpVerificationRepository otpVerificationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            OtpVerificationRepository otpVerificationRepository
    ) {
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @Transactional
    public String generateOtp(
            String identifier,
            OtpType otpType,
            OtpPurpose otpPurpose
    ) {

        String normalizedIdentifier = normalizeIdentifier(
                identifier,
                otpType
        );

        OtpVerification previousOtp = otpVerificationRepository
                .findTopByIdentifierAndOtpTypeAndOtpPurposeOrderByCreatedAtDesc(
                        normalizedIdentifier,
                        otpType,
                        otpPurpose
                )
                .orElse(null);

        int resendCount = 0;

        if (previousOtp != null) {

            if (previousOtp.getLastSentAt() != null
                    && previousOtp.getLastSentAt()
                    .plusSeconds(RESEND_COOLDOWN_SECONDS)
                    .isAfter(LocalDateTime.now())) {

                throw new RuntimeException(
                        "Please wait 60 seconds before requesting another OTP."
                );
            }

            if (previousOtp.getResendCount() >= MAX_RESEND_COUNT) {

                throw new RuntimeException(
                        "Maximum OTP resend limit reached."
                );
            }

            resendCount = previousOtp.getResendCount() + 1;
        }

        String otpCode = generateSecureOtp();

        LocalDateTime now = LocalDateTime.now();

        OtpVerification otpVerification = OtpVerification.builder()
                .identifier(normalizedIdentifier)
                .otpCode(otpCode)
                .otpType(otpType)
                .otpPurpose(otpPurpose)
                .expiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .failedAttempts(0)
                .resendCount(resendCount)
                .lastSentAt(now)
                .build();

        otpVerificationRepository.save(otpVerification);

        return otpCode;
    }

    @Transactional
    public boolean verifyOtp(
            String identifier,
            String otpCode,
            OtpType otpType,
            OtpPurpose otpPurpose
    ) {

        String normalizedIdentifier = normalizeIdentifier(
                identifier,
                otpType
        );

        OtpVerification otpVerification = otpVerificationRepository
                .findTopByIdentifierAndOtpTypeAndOtpPurposeOrderByCreatedAtDesc(
                        normalizedIdentifier,
                        otpType,
                        otpPurpose
                )
                .orElseThrow(
                        () -> new RuntimeException("OTP not found.")
                );

        if (otpVerification.isVerified()) {

            throw new RuntimeException(
                    "OTP has already been verified."
            );
        }

        if (otpVerification.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired."
            );
        }

        if (otpVerification.getFailedAttempts()
                >= MAX_FAILED_ATTEMPTS) {

            throw new RuntimeException(
                    "Maximum OTP verification attempts reached."
            );
        }

        if (!otpVerification.getOtpCode().equals(otpCode)) {

            otpVerification.setFailedAttempts(
                    otpVerification.getFailedAttempts() + 1
            );

            otpVerificationRepository.save(otpVerification);

            throw new RuntimeException(
                    "Invalid OTP."
            );
        }

        otpVerification.setVerified(true);

        otpVerificationRepository.save(otpVerification);

        return true;
    }

    public boolean isOtpVerified(
            String identifier,
            OtpType otpType,
            OtpPurpose otpPurpose
    ) {

        String normalizedIdentifier = normalizeIdentifier(
                identifier,
                otpType
        );

        OtpVerification otpVerification = otpVerificationRepository
                .findTopByIdentifierAndOtpTypeAndOtpPurposeOrderByCreatedAtDesc(
                        normalizedIdentifier,
                        otpType,
                        otpPurpose
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "OTP verification record not found."
                        )
                );

        return otpVerification.isVerified();
    }

    private String generateSecureOtp() {

        int otp = secureRandom.nextInt(900000) + 100000;

        return String.valueOf(otp);
    }

    private String normalizeIdentifier(
            String identifier,
            OtpType otpType
    ) {

        if (identifier == null || identifier.isBlank()) {

            throw new RuntimeException(
                    "OTP identifier is required."
            );
        }

        String normalizedIdentifier = identifier.trim();

        if (otpType == OtpType.EMAIL) {

            normalizedIdentifier =
                    normalizedIdentifier.toLowerCase();
        }

        return normalizedIdentifier;
    }
}