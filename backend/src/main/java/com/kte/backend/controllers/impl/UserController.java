package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UIUserController;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.services.authentication.UserService;
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

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            final Pageable pageable) {
        log.debug("Received request to get all users with pageable: {}", pageable);
        final PageResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    @PutMapping("/{user-id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("user-id") final String id,
            @RequestBody UserRequest request) {
        log.debug("Received request to update user with id: {} and request: {}", id, request);
        final UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.accepted().body(updatedUser);
    }

    @Override
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser() {
        log.debug("Received request to get current user");
        final User currentUser = userService.getCurrentLoggedInUser();
        return ResponseEntity.ok(currentUser);
    }
}
