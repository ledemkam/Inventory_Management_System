package com.kte.backend.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;


@Builder
public record CategoryRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}