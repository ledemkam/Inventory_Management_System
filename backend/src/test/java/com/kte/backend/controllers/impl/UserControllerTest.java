package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.authentication.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("web layer test for UserController")
class UserControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return all users")
    void should_Return_get_All_Users() {
        final UserResponse userResponse = UserResponse.builder()
                .id("1")
                .username("testuser")
                .build();
        final PageResponse<UserResponse> userResponses = PageResponse.<UserResponse>builder()
                .content(List.of(userResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(userService.getAllUsers(any())).thenReturn(userResponses);

        // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
        // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
        // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
        // token validation so the filter grants the ADMIN authority the endpoint requires.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");

        restTestClient.get().uri("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("1")
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(10)
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    @DisplayName("should update a user")
    @WithMockUser(roles = "ADMIN")
    void should_Update_User() {
        final UserRequest updateRequest = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("Password123!")
                .role(UserRole.MANAGER)
                .build();

        final UserResponse updatedUser = UserResponse.builder()
                .id("1")
                .username(updateRequest.username())
                .email(updateRequest.email())
                .role(updateRequest.role())
                .build();

        when(userService.updateUser(eq("1"), any(UserRequest.class))).thenReturn(updatedUser);

        // See should_Return_get_All_Users: authentication is established per-request by
        // JwtAuthenticationFilter, so the JWT validation has to be mocked here too.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");

        restTestClient.put().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.username").isEqualTo("updateduser")
                .jsonPath("$.email").isEqualTo("updated@example.com");

        verify(userService, times(1)).updateUser(eq("1"), any(UserRequest.class));
    }

    @Test
    @DisplayName("should let a user update their own account")
    void should_Update_Own_Account() {
        final UserRequest updateRequest = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("Password123!")
                .build();

        final UserResponse updatedUser = UserResponse.builder()
                .id("1")
                .username(updateRequest.username())
                .email(updateRequest.email())
                .role(UserRole.USER)
                .build();

        when(userService.updateUser(eq("1"), any(UserRequest.class))).thenReturn(updatedUser);

        // The caller's JWT subject ("1") matches the target user-id, so a plain USER should
        // be allowed through even without ADMIN/MANAGER authority.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");

        restTestClient.put().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isAccepted();

        verify(userService, times(1)).updateUser(eq("1"), any(UserRequest.class));
    }

    @Test
    @DisplayName("should forbid a user from updating another account")
    void should_Forbid_Updating_Another_Account() {
        final UserRequest updateRequest = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("Password123!")
                .build();

        // The caller's JWT subject ("2") does not match the target user-id ("1"), and USER
        // carries no ADMIN/MANAGER authority, so the request must be rejected.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("2");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");

        restTestClient.put().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isForbidden();

        verify(userService, never()).updateUser(anyString(), any(UserRequest.class));
    }

    @Test
    @DisplayName("should return the current logged in user")
    @WithMockUser(roles = "ADMIN")
    void should_Return_Current_User() {
        final User currentUser = User.builder()
                .id("1")
                .username("testuser")
                .email("testuser@example.com")
                .role(UserRole.ADMIN)
                .build();

        final UserResponse currentUserResponse = UserResponse.builder()
                .id("1")
                .username("testuser")
                .email("testuser@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(userService.getCurrentLoggedInUser()).thenReturn(currentUser);
        when(userMapper.entityToDto(currentUser)).thenReturn(currentUserResponse);

        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");

        restTestClient.get().uri("/api/v1/users/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.username").isEqualTo("testuser");

        verify(userService, times(1)).getCurrentLoggedInUser();
    }

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    @DisplayName("should return a user's transactions")
    void should_Return_User_Transactions() {
        final TransactionResponse transactionResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .build();

        final PageResponse<TransactionResponse> transactionResponses = PageResponse.<TransactionResponse>builder()
                .content(List.of(transactionResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(userService.getUserTransactions(eq("1"), any())).thenReturn(transactionResponses);

        // getUserAndTransactions is granted to ADMIN and MANAGER (see hasAnyRole on the
        // controller), so this test exercises the MANAGER path specifically.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("2");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");

        restTestClient.get().uri("/api/v1/users/transactions/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("t1")
                .jsonPath("$.totalElements").isEqualTo(1);

        verify(userService, times(1)).getUserTransactions(eq("1"), any());
    }

    @Test
    @DisplayName("should delete a user")
    @WithMockUser(roles = "ADMIN")
    void should_Delete_User() {
        doNothing().when(userService).deleteUser("1");

        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");

        restTestClient.delete().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).deleteUser("1");
    }

    @Test
    @DisplayName("should let a user delete their own account")
    void should_Delete_Own_Account() {
        doNothing().when(userService).deleteUser("1");

        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");

        restTestClient.delete().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).deleteUser("1");
    }

    @Test
    @DisplayName("should forbid a user from deleting another account")
    void should_Forbid_Deleting_Another_Account() {
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("2");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");

        restTestClient.delete().uri("/api/v1/users/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isForbidden();

        verify(userService, never()).deleteUser(anyString());
    }
}
