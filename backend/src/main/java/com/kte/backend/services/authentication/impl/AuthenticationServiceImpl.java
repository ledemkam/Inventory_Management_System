package com.kte.backend.services.authentication.impl;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.repository.UserRepository;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(final LoginRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        log.info("Authentication successful for user {}", request.username());
        final User user = (User) authentication.getPrincipal();

        final String token = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getRole().name());

        final String tokenType = "Bearer";

        log.info("User {} logged in successfully", user.getUsername());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType(tokenType)
                .build();
    }

    @Override
    public UserResponse registerUser(final RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new EntityAlreadyExistsException("User with username " + registerRequest.username() +
                    " already exists");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EntityAlreadyExistsException("User with email " + registerRequest.email() +
                    " already exists");
        }

        if (registerRequest.role() == null) {
            throw new IllegalArgumentException("Role must be provided");
        }

        final User user = userMapper.dtoToEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        final User savedUser = userRepository.save(user);

        log.info("User created successfully with id {}", savedUser.getId());
        return userMapper.entityToDto(savedUser);
    }

}
