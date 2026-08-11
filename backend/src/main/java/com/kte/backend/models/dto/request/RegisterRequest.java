package com.kte.backend.models.dto.request;

import com.kte.backend.models.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record RegisterRequest(

        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "PhoneNumber is required")
        String phoneNumber,
        UserRole role) {

}