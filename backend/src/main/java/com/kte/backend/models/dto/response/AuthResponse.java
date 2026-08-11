package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.models.enums.UserRole;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(

        String token,

        UserRole role,

        String expirationTime,

        UserResponse user

) {
}