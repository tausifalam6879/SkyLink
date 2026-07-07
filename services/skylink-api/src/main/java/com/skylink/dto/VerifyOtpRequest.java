package com.skylink.dto;

import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "OTP identifier is required.")
    @Email(message = "Enter a valid email address.")
    private String identifier;

    @NotBlank(message = "OTP code is required.")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6 digit code.")
    private String otpCode;

    @NotNull(message = "OTP type is required.")
    private OtpType otpType;

    @NotNull(message = "OTP purpose is required.")
    private OtpPurpose otpPurpose;
}
