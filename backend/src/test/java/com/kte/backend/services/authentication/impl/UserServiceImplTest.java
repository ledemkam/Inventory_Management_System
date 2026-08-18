package com.kte.backend.services.authentication.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.mapper.TransactionMapper;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.repository.TransactionRepository;
import com.kte.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return all users")
    void should_Return_All_Users() {
        //Given
        final User userEntity1 = User.builder()
                .id("1")
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final User userEntity2 = User.builder()
                .id("2")
                .username("janedoe")
                .email("janedoe@example.com")
                .phoneNumber("0987654321")
                .role(UserRole.ADMIN)
                .build();

        final UserResponse expectedResponse1 = UserResponse.builder()
                .id("1")
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final UserResponse expectedResponse2 = UserResponse.builder()
                .id("2")
                .username("janedoe")
                .email("janedoe@example.com")
                .phoneNumber("0987654321")
                .role(UserRole.ADMIN)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(
                List.of(userEntity1, userEntity2),
                pageable,
                2
        );

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.entityToDto(userEntity1)).thenReturn(expectedResponse1);
        when(userMapper.entityToDto(userEntity2)).thenReturn(expectedResponse2);

        // When
        PageResponse<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("page", 0)
                .hasFieldOrPropertyWithValue("size", 10)
                .hasFieldOrPropertyWithValue("totalElements", 2)
                .hasFieldOrPropertyWithValue("totalPages", 1)
                .hasFieldOrPropertyWithValue("hasNext", false)
                .hasFieldOrPropertyWithValue("hasPrevious", false)
                .hasFieldOrPropertyWithValue("isFirst", true)
                .hasFieldOrPropertyWithValue("isLast", true);

        assertThat(result.getContent())
                .isNotNull()
                .hasSize(2)
                .containsExactly(expectedResponse1, expectedResponse2);


    }

    @Test
    @DisplayName("Should return the current logged-in user")
    void should_Return_TheCurrent_Logged_In_User() {
        // Given
        final String userId = "1";
        final User expectedUser = User.builder()
                .id(userId)
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);

        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        final User actualUser = userService.getCurrentLoggedInUser();

        // Then
        assertEquals(expectedUser, actualUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return updated user")
    void should_Return_Updated_User() {
        // Given
        final String userId = "1";
        final UserRequest userRequest = UserRequest.builder()
                .username("johndoe")
                .email("johndoe@example.com")
                .password("newPassword")
                .role(UserRole.MANAGER)
                .build();

        final User existingUser = User.builder()
                .id(userId)
                .username("johndoe")
                .email("johndoe@example.com")
                .password("oldEncodedPassword")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final User savedUser = User.builder()
                .id(userId)
                .username("johndoe")
                .email("johndoe@example.com")
                .password("newEncodedPassword")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(userRequest.password())).thenReturn("newEncodedPassword");
        when(userRepository.save(existingUser)).thenReturn(savedUser);
        when(userMapper.entityToDto(savedUser)).thenReturn(expectedResponse);

        // When
        final UserResponse actualResponse = userService.updateUser(userId, userRequest);

        // Then
        assertEquals(expectedResponse, actualResponse);
        assertEquals("newEncodedPassword", existingUser.getPassword());
        verify(userRepository).findById(userId);
        verify(userMapper).updateEntityFromDto(userRequest, existingUser);
        verify(passwordEncoder).encode(userRequest.password());
        verify(userRepository).save(existingUser);
    }


    @Test
    @DisplayName("Should return user transactions")
    void should_Return_User_Transactions() {
        // Given
        final String userId = "1";
        final Pageable pageable = PageRequest.of(0, 10);

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.findAllByUser_Id(userId, pageable)).thenReturn(transactionPage);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final PageResponse<TransactionResponse> result = userService.getUserTransactions(userId, pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("page", 0)
                .hasFieldOrPropertyWithValue("size", 10)
                .hasFieldOrPropertyWithValue("totalElements", 1);

        assertThat(result.getContent())
                .isNotNull()
                .hasSize(1)
                .containsExactly(expectedResponse);

        verify(userRepository).existsById(userId);
        verify(transactionRepository).findAllByUser_Id(userId, pageable);
    }

    @Test
    @DisplayName("Should throw when fetching transactions for a non-existing user")
    void should_Throw_When_Fetching_Transactions_For_No_Existing_User() {
        // Given
        final String userId = "unknown";
        final Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        // When / Then
        assertThrows(EntityNotFoundException.class,
                () -> userService.getUserTransactions(userId, pageable));

        verify(userRepository).existsById(userId);
        verify(transactionRepository, never()).findAllByUser_Id(anyString(), any(Pageable.class));
    }


    @Test
    @DisplayName("Should delete user when user exists")
    void should_Delete_User_when_User_Exists() {
        //Given
        final User userEntity = User.builder()
                .id("1")
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        doNothing().when(userRepository).delete(userEntity);

        // When
        userService.deleteUser(userEntity.getId());

        // Then

        verify(userRepository).findById(userEntity.getId());
        verify(userRepository).delete(userEntity);
    }

    @Test
    @DisplayName("Should throw when deleting a no-existing user")
    void should_Throw_When_Deleting_A_No_Existing_User() {
        //Given
        final User userEntity = User.builder()
                .id("1")
                .username("johndoe")
                .email("johndoe@example.com")
                .phoneNumber("1234567890")
                .role(UserRole.MANAGER)
                .build();

        final String userId = userEntity.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}