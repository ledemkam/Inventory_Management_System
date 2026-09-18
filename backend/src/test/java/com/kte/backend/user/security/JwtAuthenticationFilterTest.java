package com.kte.backend.user.security;

import com.kte.backend.exception.AuthenticationEntryPointException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should skip authentication for the login endpoint")
    void should_Skip_Authentication_For_Login_Endpoint() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("Should skip authentication for the register endpoint")
    void should_Skip_Authentication_For_Register_Endpoint() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("Should continue the chain without setting authentication when there is no token")
    void should_Continue_Chain_When_No_Token_Present() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should set authentication and normalize the role when the token is valid")
    void should_Set_Authentication_When_Token_Valid() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenService.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN("valid-token")).thenReturn("u1");
        when(jwtTokenService.getRoleFromToken("valid-token")).thenReturn("ADMIN");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("u1");
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not double-prefix a role that already starts with ROLE_")
    void should_Not_Double_Prefix_Role_Already_Prefixed() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenService.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN("valid-token")).thenReturn("u1");
        when(jwtTokenService.getRoleFromToken("valid-token")).thenReturn("ROLE_MANAGER");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_MANAGER");
    }

    @Test
    @DisplayName("Should not treat a header without the Bearer prefix as a token")
    void should_Ignore_Header_Without_Bearer_Prefix() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verifyNoInteractions(jwtTokenService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 401 and stop the chain when the role claim is missing")
    void should_Return_401_When_Role_Claim_Missing() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenService.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN("valid-token")).thenReturn("u1");
        when(jwtTokenService.getRoleFromToken("valid-token")).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should return 401 and stop the chain when the token is invalid")
    void should_Return_401_When_Token_Validation_Fails() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/product");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtTokenService.validateToken("bad-token"))
                .thenThrow(new AuthenticationEntryPointException("JWT token is malformed"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }
}
