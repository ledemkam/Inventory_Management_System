package com.kte.backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record TransactionRequest(

        @NotBlank(message = "Product id is required")
        String productId,

        @Positive(message = "Quantity must be positive")
        Integer quantity,

        String supplierId,

        String description
) {

}