package com.kte.backend.user.services;

import com.kte.backend.common.PageResponse;
import com.kte.backend.user.internal.dto.request.UserRequest;
import com.kte.backend.user.dto.response.UserResponse;
import com.kte.backend.user.User;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PageResponse<UserResponse> getAllUsers(final Pageable pageable);

    User getCurrentLoggedInUser();

    UserResponse updateUser(final String id, final UserRequest userRequest);

    void deleteUser(final String id);
}
