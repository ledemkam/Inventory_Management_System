package com.kte.backend.transaction.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;


@Builder
public record TransactionRequest(

        @NotBlank(message = "Product id is required")
        String productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        String supplierId,

        String description
) {

}