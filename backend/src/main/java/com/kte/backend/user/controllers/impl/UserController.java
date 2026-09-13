package com.kte.backend.user.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.user.controllers.UIUserController;
import com.kte.backend.user.mapper.UserMapper;
import com.kte.backend.user.dto.request.UserRequest;
import com.kte.backend.user.dto.response.UserResponse;
import com.kte.backend.user.User;
import com.kte.backend.user.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController implements UIUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            final Pageable pageable) {
        log.debug("Received request to get all users with pageable: {}", pageable);
        final PageResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    @PutMapping("/{user-id}")
    // ADMIN/MANAGER can update any account; anyone else can only update their own
    // (the JWT principal name is the authenticated user's id - see JwtAuthenticationFilter).
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == authentication.name")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("user-id") final String id,
            @RequestBody @Valid UserRequest request) {
        log.debug("Received request to update user with user-id: {} and request: {}", id, request);
        final UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.accepted().body(updatedUser);
    }

    @Override
    @GetMapping("/current")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Received request to get current user");
        final User currentUser = userService.getCurrentLoggedInUser();
        return ResponseEntity.ok(userMapper.entityToDto(currentUser));
    }

    @Override
    @DeleteMapping("/{user-id}")
    // ADMIN/MANAGER can delete any account; anyone else can only delete their own.
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == authentication.name")
    public ResponseEntity<Void> deleteUser(@PathVariable("user-id") String id) {
        log.debug("Received request to delete user with user-id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
