package com.kte.backend.user.security;

import com.kte.backend.exception.AuthenticationEntryPointException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenService Tests")
class JwtTokenServiceTest {

    private static final String SECRET = "unit-test-jwt-secret-key-with-enough-length-for-hs256";

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenService, "jwtExpiration", 3_600_000L);
        jwtTokenService.init();
    }

    @Test
    @DisplayName("Should generate a token and read back the user id and role from it")
    void should_Generate_And_Validate_Token_Successfully() {
        // When
        final String token = jwtTokenService.generateAccessToken("u1", "ADMIN");

        // Then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
        assertThat(jwtTokenService.getUserIdFromTokEN(token)).isEqualTo("u1");
        assertThat(jwtTokenService.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should throw when the token is malformed")
    void should_Throw_When_Token_Is_Malformed() {
        assertThatThrownBy(() -> jwtTokenService.validateToken("not-a-jwt-token"))
                .isInstanceOf(AuthenticationEntryPointException.class)
                .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("Should throw when the token is empty")
    void should_Throw_When_Token_Is_Empty() {
        assertThatThrownBy(() -> jwtTokenService.validateToken(""))
                .isInstanceOf(AuthenticationEntryPointException.class)
                .hasMessageContaining("empty or null");
    }

    @Test
    @DisplayName("Should throw when the token is expired")
    void should_Throw_When_Token_Is_Expired() {
        // Given
        final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
        final Date past = new Date(System.currentTimeMillis() - 10_000);
        final String expiredToken = Jwts.builder()
                .subject("u1")
                .claim("role", "ADMIN")
                .issuedAt(new Date(past.getTime() - 1_000))
                .expiration(past)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        // When / Then
        assertThatThrownBy(() -> jwtTokenService.validateToken(expiredToken))
                .isInstanceOf(AuthenticationEntryPointException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should throw when the token is signed with a different key")
    void should_Throw_When_Token_Signature_Is_Invalid() {
        // Given
        final SecretKey otherKey = Keys.hmacShaKeyFor("a-completely-different-unit-test-secret-key-value".getBytes());
        final String token = Jwts.builder()
                .subject("u1")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        // When / Then
        assertThatThrownBy(() -> jwtTokenService.validateToken(token))
                .isInstanceOf(AuthenticationEntryPointException.class)
                .hasMessageContaining("invalid");
    }
}
