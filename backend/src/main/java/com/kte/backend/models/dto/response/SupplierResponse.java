package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SupplierResponse(

        String id,

        String name,

        String address,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}