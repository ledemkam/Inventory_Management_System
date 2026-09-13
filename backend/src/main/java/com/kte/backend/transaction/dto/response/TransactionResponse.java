package com.kte.backend.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.catalog.dto.response.ProductResponse;
import com.kte.backend.catalog.dto.response.SupplierResponse;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.user.dto.response.UserResponse;
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