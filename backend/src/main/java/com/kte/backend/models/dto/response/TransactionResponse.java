package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(

        String id,

        Integer totalProducts,

        BigDecimal totalPrice,

        TransactionType transactionType,

        TransactionStatus status,

        String description,

        UserResponse user,

        ProductResponse product,

        SupplierResponse supplier,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}