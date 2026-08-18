package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.authentication.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("web layer test for UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return all users")
    void should_Return_get_All_Users() throws Exception {
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

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("1")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void updateUser() {
    }

    @Test
    void getCurrentUser() {
    }

    @Test
    void getUserAndTransactions() {
    }

    @Test
    void deleteUser() {
    }
}
