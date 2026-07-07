package com.skylink.dto;

import com.skylink.entity.OtpPurpose;
import com.skylink.entity.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequest {

    @NotBlank(message = "OTP identifier is required.")
    @Email(message = "Enter a valid email address.")
    private String identifier;

    @NotNull(message = "OTP type is required.")
    private OtpType otpType;

    @NotNull(message = "OTP purpose is required.")
    private OtpPurpose otpPurpose;
}
