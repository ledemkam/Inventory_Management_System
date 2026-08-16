package com.kte.backend.controllers.impl;

import com.kte.backend.controllers.UIAuthenticationController;
import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.services.authentication.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthenticationController implements UIAuthenticationController {

    private final AuthenticationService authenticationService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid
            @RequestBody final RegisterRequest registerRequest) {
        final UserResponse userResponse = authenticationService.registerUser(registerRequest);
        log.info("User registered successfully: {}", userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginResponse loginResponse = authenticationService.login(request);
        log.info("User logged in successfully: {}", loginResponse);
        return ResponseEntity.ok(loginResponse);
    }


}
