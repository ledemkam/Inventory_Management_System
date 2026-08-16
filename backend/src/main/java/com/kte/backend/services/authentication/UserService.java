package com.kte.backend.services.authentication;

import com.kte.backend.common.PageResponse;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse registerUser(final RegisterRequest registerRequest);

    PageResponse<UserResponse> getAllUsers(final Pageable pageable);

    User getCurrentLoggedInUser();

    UserResponse updateUser(final String id, final UserRequest userRequest);

    UserResponse deleteUser(final String id);

    PageResponse<TransactionResponse> getUserTransactions(final String id, final Pageable pageable);
}
