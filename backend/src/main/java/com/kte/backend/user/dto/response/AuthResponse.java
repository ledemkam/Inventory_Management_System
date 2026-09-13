package com.kte.backend.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.user.UserRole;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(

        String id,

        String token,

        UserRole role,

        String expirationTime,

        UserResponse user

) {
}