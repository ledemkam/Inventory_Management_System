package com.kte.backend.controllers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.authentication.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@DisplayName("Web layer test for AuthenticationController")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationService authenticationService;

    // Required to satisfy SecurityConfig's JwtAuthenticationFilter bean, even though
    // /register and /login are permitAll and never reach JWT validation logic.
    @MockitoBean
    private JwtTokenService jwtTokenService;


    @Test
    @DisplayName("Should register a new user successfully")
    void should_Register_User() throws Exception {
        // Given
        final RegisterRequest registerRequest = RegisterRequest.builder()
                .username("john.doe")
                .email("john.doe@example.com")
                .password("Password123!")
                .phoneNumber("+237600000000")
                .role(UserRole.MANAGER)
                .build();

        final UserResponse expectedResponse = UserResponse.builder()
                .id("1")
                .username(registerRequest.username())
                .email(registerRequest.email())
                .phoneNumber(registerRequest.phoneNumber())
                .role(registerRequest.role())
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticationService.registerUser(any(RegisterRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.username").value(expectedResponse.username()))
                .andExpect(jsonPath("$.email").value(expectedResponse.email()))
                .andExpect(jsonPath("$.role").value(expectedResponse.role().name()));

        verify(authenticationService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("should login a user")
    void should_Login() throws Exception {
        //Given
        final LoginRequest loginRequest = LoginRequest.builder()
                .username("ledemkam")
                .password("Password123!")
                .build();

        final LoginResponse expectedResponse = LoginResponse.builder()
                .accessToken("dummy-jwt-token")
                .tokenType("Bearer")
                .build();

        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(expectedResponse.accessToken()))
                .andExpect(jsonPath("$.tokenType").value(expectedResponse.tokenType()));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }
}
