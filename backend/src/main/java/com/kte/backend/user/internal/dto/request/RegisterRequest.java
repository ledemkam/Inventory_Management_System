package com.kte.backend.user.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;


@Builder
public record RegisterRequest(

        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "PhoneNumber is required")
        String phoneNumber
) {
}