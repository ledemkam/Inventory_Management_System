package com.kte.backend.models.dto.request;

import com.kte.backend.models.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        UserRole role
) {
}
