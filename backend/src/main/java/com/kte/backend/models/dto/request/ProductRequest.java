package com.kte.backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Sku is required")
        String sku,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be zero or positive")
        Integer stockQuantity,

        String description,

        String imageUrl,

        LocalDateTime expiryDate,

        @NotBlank(message = "Category id is required")
        String categoryId
) {
}