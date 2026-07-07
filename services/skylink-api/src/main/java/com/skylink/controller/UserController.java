package com.skylink.controller;

import com.skylink.dto.RegisterRequest;
import com.skylink.dto.RegisterResponse;
import com.skylink.dto.UserProfileResponse;
import com.skylink.entity.User;
import com.skylink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid
            @RequestBody RegisterRequest request
    ) {

        User user = userService.registerUser(request);

        return RegisterResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .whatsappNumber(user.getWhatsappNumber())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .whatsappVerified(user.isWhatsappVerified())
                .build();
    }

    @GetMapping("/test")
    public String testProtectedApi() {

        return "SkyLink protected API accessed successfully.";
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userService.findByEmail(email);

        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .whatsappNumber(user.getWhatsappNumber())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .whatsappVerified(user.isWhatsappVerified())
                .build();
    }
}
