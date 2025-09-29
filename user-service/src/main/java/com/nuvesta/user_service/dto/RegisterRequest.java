package com.nuvesta.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) { }
