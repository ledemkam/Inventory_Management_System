package com.kte.backend.models.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kte.backend.models.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;

    private String name;

    private String email;

    private String phoneNumber;

    private UserRole role;

    private LocalDateTime createdAt;

}