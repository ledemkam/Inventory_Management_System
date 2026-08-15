package com.kte.backend.services.authentication.impl;

import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        final User user = (User) authentication.getPrincipal();

        final String token = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getRole().name());

        final String tokenType = "Bearer";

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType(tokenType)
                .build();
    }

}
