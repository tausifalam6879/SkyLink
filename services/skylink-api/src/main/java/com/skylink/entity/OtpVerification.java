package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType otpType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose otpPurpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private int failedAttempts = 0;

    @Builder.Default
    private int resendCount = 0;

    private LocalDateTime lastSentAt;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}