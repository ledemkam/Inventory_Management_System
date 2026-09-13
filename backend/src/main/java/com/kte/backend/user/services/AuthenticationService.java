package com.kte.backend.user.services;

import com.kte.backend.user.dto.request.LoginRequest;
import com.kte.backend.user.dto.request.RegisterRequest;
import com.kte.backend.user.dto.response.LoginResponse;
import com.kte.backend.user.dto.response.UserResponse;

public interface AuthenticationService {
    LoginResponse login(final LoginRequest request);

    UserResponse registerUser(final RegisterRequest registerRequest);
}
