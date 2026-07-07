package com.skylink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long id;

    private String fullName;

    private String email;

    private String mobileNumber;

    private String whatsappNumber;

    private String role;

    private boolean emailVerified;

    private boolean mobileVerified;

    private boolean whatsappVerified;
}