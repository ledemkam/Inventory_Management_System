package com.kte.backend.user.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.user.mapper.UserMapper;
import com.kte.backend.user.dto.request.UserRequest;
import com.kte.backend.user.dto.response.UserResponse;
import com.kte.backend.user.User;
import com.kte.backend.user.repository.UserRepository;
import com.kte.backend.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserResponse> getAllUsers(final Pageable pageable) {
        log.debug("Fetching users with paging: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());
        return PageResponse.of(userRepository.findAll(pageable).map(userMapper::entityToDto));
    }

    @Override
    public User getCurrentLoggedInUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new EntityNotFoundException("No authenticated user found");
        }

        final String userId = authentication.getName();

        log.debug("Fetching current logged-in user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));
    }

    @Override
    public UserResponse updateUser(final String id, final UserRequest userRequest) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        // The controller's @PreAuthorize lets a user reach this method for their own account
        // even without ADMIN/MANAGER authority (self-service profile edits). Without this guard
        // that same self-service call could smuggle in a role change and self-promote to ADMIN.
        if (userRequest.role() != null && !callerCanAssignRoles()) {
            throw new AccessDeniedException("Only ADMIN or MANAGER can change a user's role");
        }

        userMapper.updateEntityFromDto(userRequest, existingUser);
        if (userRequest.password() != null && !userRequest.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.password()));
        }
        User updatedUser = userRepository.save(existingUser);
        log.info("User with id {} updated successfully", id);
        return userMapper.entityToDto(updatedUser);
    }

    private boolean callerCanAssignRoles() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_MANAGER"));
    }

    @Override
    public void deleteUser(final String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        log.info("User with id {} deleted successfully", id);
        userRepository.delete(existingUser);
    }


}
