package com.skylink.controller;

import com.skylink.dto.SendOtpRequest;
import com.skylink.dto.VerifyOtpRequest;
import com.skylink.entity.OtpType;
import com.skylink.service.EmailService;
import com.skylink.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin("*")
public class OtpController {

    private final OtpService otpService;

    private final EmailService emailService;

    public OtpController(
            OtpService otpService,
            EmailService emailService
    ) {
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(
            @Valid
            @RequestBody SendOtpRequest request
    ) {

        if (request.getOtpType() != OtpType.EMAIL) {

            return ResponseEntity.badRequest()
                    .body(
                            "This endpoint currently supports EMAIL OTP delivery."
                    );
        }

        String otpCode = otpService.generateOtp(
                request.getIdentifier(),
                request.getOtpType(),
                request.getOtpPurpose()
        );

        emailService.sendOtpEmail(
                request.getIdentifier(),
                otpCode,
                request.getOtpPurpose()
        );

        return ResponseEntity.ok(
                "OTP sent successfully."
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(
            @Valid
            @RequestBody VerifyOtpRequest request
    ) {

        otpService.verifyOtp(
                request.getIdentifier(),
                request.getOtpCode(),
                request.getOtpType(),
                request.getOtpPurpose()
        );

        return ResponseEntity.ok(
                "OTP verified successfully."
        );
    }
}
