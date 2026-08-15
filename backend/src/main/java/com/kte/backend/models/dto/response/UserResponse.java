package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.models.enums.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(

        String id,

        String username,

        String email,

        String phoneNumber,

        UserRole role,

        LocalDateTime createdAt

) {
}