package com.kte.backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
public record CategoryRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}