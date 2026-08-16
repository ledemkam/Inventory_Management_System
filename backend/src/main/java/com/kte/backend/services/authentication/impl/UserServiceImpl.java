package com.kte.backend.services.authentication.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.repository.UserRepository;
import com.kte.backend.services.authentication.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    public UserResponse registerUser(final RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new EntityAlreadyExistsException("User with username " + registerRequest.username() +
                    " already exists");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EntityAlreadyExistsException("User with email " + registerRequest.email() +
                    " already exists");
        }

        if (registerRequest.role() == null) {
            throw new IllegalArgumentException("Role must be provided");
        }

        final User user = userMapper.dtoToEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        final User savedUser = userRepository.save(user);

        log.info("User created successfully");
        return userMapper.entityToDto(savedUser);
    }

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

        final String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email " + userEmail));
    }

    @Override
    public UserResponse updateUser(final String id, final UserRequest userRequest) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        userMapper.updateEntityFromDto(userRequest, existingUser);
        if (userRequest.password() != null && !userRequest.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.password()));
        }
        User updatedUser = userRepository.save(existingUser);
        return userMapper.entityToDto(updatedUser);
    }

    @Override
    public UserResponse deleteUser(String id) {
        return null;
    }

    @Override
    public UserResponse getUserTransactions(String id) {
        return null;
    }
}
