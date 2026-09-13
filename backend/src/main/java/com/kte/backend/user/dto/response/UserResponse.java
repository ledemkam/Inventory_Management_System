package com.kte.backend.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.user.UserRole;
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