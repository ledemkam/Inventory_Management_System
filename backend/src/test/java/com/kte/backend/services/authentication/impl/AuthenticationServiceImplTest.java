package com.kte.backend.services.authentication.impl;

import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.security.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("Should login user and return access token when credentials are valid")
    void should_login_user_when_credentials_are_valid() {
        final LoginRequest loginRequest = LoginRequest.builder()
                .username("johndoe")
                .password("plainPassword")
                .build();

        final User authenticatedUser = User.builder()
                .id("1")
                .username("johndoe")
                .role(UserRole.MANAGER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(jwtTokenService.generateAccessToken(authenticatedUser.getId(), authenticatedUser.getRole().name()))
                .thenReturn("generatedAccessToken");

        final LoginResponse actualResponse = authenticationService.login(loginRequest);

        assertEquals("generatedAccessToken", actualResponse.accessToken());
        assertEquals("Bearer", actualResponse.tokenType());
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password()));
        verify(jwtTokenService).generateAccessToken(authenticatedUser.getId(), authenticatedUser.getRole().name());
    }

    @Test
    @DisplayName("Should not login user when credentials are invalid")
    void should_not_login_user_when_credentials_are_invalid() {
        final LoginRequest loginRequest = LoginRequest.builder()
                .username("johndoe")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginRequest));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password()));
        verify(jwtTokenService, never()).generateAccessToken(anyString(), anyString());
    }
}
