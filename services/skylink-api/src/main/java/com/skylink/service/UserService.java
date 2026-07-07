package com.skylink.service;

import com.skylink.dto.RegisterRequest;
import com.skylink.entity.AuthProvider;
import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import com.skylink.entity.User;
import com.skylink.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {

        String normalizedEmail = request.getEmail() == null
                ? null
                : request.getEmail().trim().toLowerCase();

        if (normalizedEmail == null || normalizedEmail.isBlank()) {

            throw new RuntimeException(
                    "Email is required."
            );
        }

        String fullName = normalizeOptionalText(
                request.getFullName()
        );

        if (fullName == null) {

            throw new RuntimeException(
                    "Full name is required."
            );
        }

        if (userRepository.existsByEmail(normalizedEmail)) {

            throw new RuntimeException(
                    "Email already exists."
            );
        }

        String mobileNumber = normalizeOptionalText(
                request.getMobileNumber()
        );

        String whatsappNumber = normalizeOptionalText(
                request.getWhatsappNumber()
        );

        if (mobileNumber != null
                && userRepository.existsByMobileNumber(mobileNumber)) {

            throw new RuntimeException(
                    "Mobile number already exists."
            );
        }

        String rawPassword = request.getPassword();
        boolean hasPassword = rawPassword != null
                && !rawPassword.isBlank();

        boolean emailOtpVerified = false;

        if (!hasPassword) {

            try {
                emailOtpVerified = otpService.isOtpVerified(
                        normalizedEmail,
                        OtpType.EMAIL,
                        OtpPurpose.REGISTRATION
                );
            } catch (RuntimeException exception) {
                throw new RuntimeException(
                        "Password or verified email OTP is required."
                );
            }

            if (!emailOtpVerified) {

                throw new RuntimeException(
                        "Password or verified email OTP is required."
                );
            }
        }

        User user = User.builder()
                .fullName(fullName)
                .email(normalizedEmail)
                .mobileNumber(mobileNumber)
                .whatsappNumber(whatsappNumber)
                .password(
                        hasPassword
                                ? passwordEncoder.encode(rawPassword)
                                : null
                )
                .role("USER")
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(emailOtpVerified)
                .build();

        return userRepository.save(user);
    }

    private String normalizeOptionalText(String value) {

        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        return normalizedValue.isBlank()
                ? null
                : normalizedValue;
    }

    public User findByEmail(String email) {

        String normalizedEmail = email
                .trim()
                .toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(
                        () -> new RuntimeException(
                                "User not found."
                        )
                );
    }

    public void validatePasswordResetUser(String email) {

        findByEmail(email);
    }

    @Transactional
    public void resetPassword(
            String email,
            String newPassword
    ) {

        String normalizedEmail = email
                .trim()
                .toLowerCase();

        User user = findByEmail(normalizedEmail);

        boolean passwordResetOtpVerified =
                otpService.isOtpVerified(
                        normalizedEmail,
                        OtpType.EMAIL,
                        OtpPurpose.PASSWORD_RESET
                );

        if (!passwordResetOtpVerified) {

            throw new RuntimeException(
                    "Password reset OTP verification is required."
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }
}
