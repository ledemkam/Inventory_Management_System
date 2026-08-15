package com.kte.backend.services.authentication;

import com.kte.backend.models.dto.request.LoginRequest;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;

public interface UserService {
    UserResponse registerUser(final RegisterRequest registerRequest);

    UserResponse loginUser(final LoginRequest loginRequest);

    UserResponse getAllUsers();

    User getCurrentLoggedInUser();

    UserResponse updateUser(final String id, final UserRequest userRequest);

    UserResponse deleteUser(final String id);

    UserResponse getUserTransactions(final String id);
}
