package com.kte.backend.security;

import com.kte.backend.exception.AuthenticationEntryPointException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        log.info("JWT secret key initialized successfully");
    }

    public String generateAccessToken(
            @NotNull final String userId,
            final String role
    ) {
        final Date now = new Date();
        final Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String getUserIdFromTokEN(final String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getRoleFromToken(final String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (final ExpiredJwtException e) {

            log.warn("JWT token is expired: {}", e.getMessage());
            throw new AuthenticationEntryPointException("JWT token is expired", e);
        } catch (final UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new AuthenticationEntryPointException("JWT token is unsupported", e);
        } catch (final MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new AuthenticationEntryPointException("JWT token is malformed", e);
        } catch (final SecurityException e) {
            log.warn("JWT token  is invalid: {}", e.getMessage());
            throw new AuthenticationEntryPointException("JWT token  is invalid", e);
        } catch (final IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
            throw new AuthenticationEntryPointException("JWT token is empty or null", e);
        }
    }

    private Claims getClaimsFromToken(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}