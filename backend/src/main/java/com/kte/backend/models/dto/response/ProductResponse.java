package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(

        String id,

        String name,

        String sku,

        BigDecimal price,

        Integer stockQuantity,

        String description,

        String imageUrl,

        LocalDateTime expiryDate,

        CategoryResponse category,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}