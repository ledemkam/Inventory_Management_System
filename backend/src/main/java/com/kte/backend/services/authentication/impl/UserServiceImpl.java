package com.kte.backend.services.authentication.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.mapper.TransactionMapper;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.UserRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

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

        userMapper.updateEntityFromDto(userRequest, existingUser);
        if (userRequest.password() != null && !userRequest.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.password()));
        }
        User updatedUser = userRepository.save(existingUser);
        log.info("User with id {} updated successfully", id);
        return userMapper.entityToDto(updatedUser);
    }

    @Override
    public PageResponse<TransactionResponse> getUserTransactions(final String id, final Pageable pageable) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id " + id);
        }

        log.debug("Fetching transactions for user with id: {}", id);
        return PageResponse.of(transactionRepository.findAllByUser_Id(id, pageable)
                .map(transactionMapper::entityToDto));
    }

    @Override
    public void deleteUser(final String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        log.info("User with id {} deleted successfully", id);
        userRepository.delete(existingUser);
    }


}
