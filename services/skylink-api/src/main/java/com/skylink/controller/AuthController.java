package com.skylink.controller;

import com.skylink.dto.AuthResponse;
import com.skylink.dto.ForgotPasswordRequest;
import com.skylink.dto.LoginRequest;
import com.skylink.dto.ResetPasswordRequest;
import com.skylink.dto.VerifyOtpRequest;
import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import com.skylink.entity.User;
import com.skylink.service.EmailService;
import com.skylink.service.OtpService;
import com.skylink.service.UserService;
import com.skylink.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            OtpService otpService,
            EmailService emailService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid
            @RequestBody LoginRequest request
    ) {

        User user = userService.findByEmail(
                request.getEmail()
        );

        if (!user.isAccountEnabled()) {

            throw new RuntimeException(
                    "User account is disabled."
            );
        }

        if (user.getPassword() == null
                || request.getPassword() == null
                || !passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid email or password."
            );
        }

        String token = jwtUtil.generateToken(
                user.getEmail()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @PostMapping("/login/otp/send")
    public ResponseEntity<String> sendLoginOtp(
            @Valid
            @RequestBody ForgotPasswordRequest request
    ) {

        User user = userService.findByEmail(
                request.getEmail()
        );

        if (!user.isAccountEnabled()) {

            throw new RuntimeException(
                    "User account is disabled."
            );
        }

        String otpCode = otpService.generateOtp(
                request.getEmail(),
                OtpType.EMAIL,
                OtpPurpose.LOGIN
        );

        emailService.sendOtpEmail(
                request.getEmail(),
                otpCode,
                OtpPurpose.LOGIN
        );

        return ResponseEntity.ok(
                "Login OTP sent successfully."
        );
    }

    @PostMapping("/login/otp/verify")
    public AuthResponse loginWithOtp(
            @Valid
            @RequestBody VerifyOtpRequest request
    ) {

        otpService.verifyOtp(
                request.getIdentifier(),
                request.getOtpCode(),
                OtpType.EMAIL,
                OtpPurpose.LOGIN
        );

        User user = userService.findByEmail(
                request.getIdentifier()
        );

        if (!user.isAccountEnabled()) {

            throw new RuntimeException(
                    "User account is disabled."
            );
        }

        String token = jwtUtil.generateToken(
                user.getEmail()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid
            @RequestBody ForgotPasswordRequest request
    ) {

        userService.validatePasswordResetUser(
                request.getEmail()
        );

        String otpCode = otpService.generateOtp(
                request.getEmail(),
                OtpType.EMAIL,
                OtpPurpose.PASSWORD_RESET
        );

        emailService.sendOtpEmail(
                request.getEmail(),
                otpCode,
                OtpPurpose.PASSWORD_RESET
        );

        return ResponseEntity.ok(
                "Password reset OTP sent successfully."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid
            @RequestBody ResetPasswordRequest request
    ) {

        userService.resetPassword(
                request.getEmail(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Password reset successfully."
        );
    }
}
