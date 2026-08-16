package com.kte.backend.services.authentication.impl;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

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

        final UserResponse actualResponse = userService.registerUser(registerRequest);

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

        assertThrows(EntityAlreadyExistsException.class, () -> userService.registerUser(registerRequest));

        verify(userRepository).existsByUsername(registerRequest.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).entityToDto(any(User.class));
    }

    @Test
    @DisplayName("Should return all users")
    void getAllUsers() {
    }

    @Test
    @DisplayName("Should return the current logged-in user")
    void getCurrentLoggedInUser() {
    }

    @Test
    @DisplayName("Should update user details")
    void updateUser() {
    }

    @Test
    @DisplayName("Should delete user")
    void deleteUser() {
    }

    @Test
    @DisplayName("Should return user transactions")
    void getUserTransactions() {
    }
}