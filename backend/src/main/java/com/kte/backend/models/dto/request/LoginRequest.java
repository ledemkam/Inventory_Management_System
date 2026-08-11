package com.kte.backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record LoginRequest(

        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password
) {

}