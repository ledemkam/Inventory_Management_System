package com.kte.backend.services.authentication.impl;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    @DisplayName("Should register user when user does not exist")
    void should_register_User_when_user_does_not_exist() {
        final RegisterRequest registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("johndoe@example.com")
                .password("plainPassword")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final User userToSave = User.builder().username("johndoe").build();
        final User savedUser = User.builder().username("johndoe").build();
        final UserResponse expectedResponse = UserResponse.builder()
                .id("1")
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userMapper.dtoToEntity(registerRequest)).thenReturn(userToSave);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(userToSave)).thenReturn(savedUser);
        when(userMapper.entityToDto(savedUser)).thenReturn(expectedResponse);

        final UserResponse actualResponse = authenticationService.registerUser(registerRequest);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("encodedPassword", userToSave.getPassword());
        verify(userRepository).existsByUsername(registerRequest.username());
        verify(userRepository).existsByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(userToSave);
    }

    @Test
    @DisplayName("Should not register user when user already exists")
    void should_register_User_when_user_exist() {
        final RegisterRequest registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("johndoe@example.com")
                .password("plainPassword")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> authenticationService.registerUser(registerRequest));

        verify(userRepository).existsByUsername(registerRequest.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).entityToDto(any(User.class));
    }
}
