package com.kte.backend.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Product id is required")
    private String productId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String supplierId;

    private String description;

}