package com.kte.backend.services.authentication;

import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(final LoginRequest request);
}
