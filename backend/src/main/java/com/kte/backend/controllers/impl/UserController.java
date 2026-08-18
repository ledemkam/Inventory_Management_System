package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UIUserController;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.services.authentication.UserService;
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
    @GetMapping("/transactions/{user-id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<TransactionResponse>> getUserAndTransactions(
            @PathVariable("user-id") final String id,
            final Pageable pageable
    ) {
        log.debug("Received request to get user and transactions for user-id: {}", id);
        final PageResponse<TransactionResponse> userWithTransactions = userService.getUserTransactions(id, pageable);
        return ResponseEntity.ok(userWithTransactions);
    }

    @Override
    @DeleteMapping("/{user-id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("user-id") String id) {
        log.debug("Received request to delete user with user-id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
