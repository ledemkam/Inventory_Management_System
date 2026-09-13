package com.kte.backend.user.internal.controllers.impl;

import com.kte.backend.config.SecurityConfig;
import com.kte.backend.user.internal.dto.request.LoginRequest;
import com.kte.backend.user.internal.dto.request.RegisterRequest;
import com.kte.backend.user.dto.response.LoginResponse;
import com.kte.backend.user.dto.response.UserResponse;
import com.kte.backend.user.UserRole;
import com.kte.backend.user.security.JwtTokenService;
import com.kte.backend.user.services.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("Web layer test for AuthenticationController")
class AuthenticationControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private AuthenticationService authenticationService;

    // Required to satisfy SecurityConfig's JwtAuthenticationFilter bean, even though
    // /register and /login are permitAll and never reach JWT validation logic.
    @MockitoBean
    private JwtTokenService jwtTokenService;


    @Test
    @DisplayName("Should register a new user successfully")
    void should_Register_User() {
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
        restTestClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(expectedResponse.id())
                .jsonPath("$.username").isEqualTo(expectedResponse.username())
                .jsonPath("$.email").isEqualTo(expectedResponse.email())
                .jsonPath("$.role").isEqualTo(expectedResponse.role().name());

        verify(authenticationService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("should login a user")
    void should_Login() {
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
        restTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo(expectedResponse.accessToken())
                .jsonPath("$.tokenType").isEqualTo(expectedResponse.tokenType());

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }
}
