package com.kte.backend.services.authentication;

import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.LoginResponse;
import com.kte.backend.models.dto.response.UserResponse;

public interface AuthenticationService {
    LoginResponse login(final LoginRequest request);

    UserResponse registerUser(final RegisterRequest registerRequest);
}
