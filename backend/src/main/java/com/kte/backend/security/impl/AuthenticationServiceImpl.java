package com.kte.backend.security.impl;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.models.entity.User;
import com.kte.backend.repository.UserRepository;
import com.kte.backend.security.AuthenticationService;
import com.kte.backend.security.BlogUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpirationMs;

    @Override
    public UserDetails authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public long getExpirationInSeconds() {
        return jwtExpirationMs / 1000L;
    }

    @Override
    public UserDetails validateToken(String token) {
        //  Parse claims once — avoid double-parsing JWTs.
        Claims claims = extractClaims(token);
        if (claims.getExpiration().before(new Date())) {
            throw new BadCredentialsException("JWT token has expired");
        }
        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            throw new BadCredentialsException("JWT token has no subject");
        }
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public UserDetails register(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EntityAlreadyExistsException("User with email " + email + " already exists");
        }
        User newUser = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        return new BlogUserDetails(savedUser);
    }

    //  Unique parsing method — used by validateToken()
    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid JWT token");
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
