package com.kte.backend.user.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;


@Builder
public record LoginRequest(

        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password
) {

}