package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.models.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;

    private UserRole role;

    private String expirationTime;

    private UserResponse user;

}