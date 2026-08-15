package com.kte.backend.services.authentication.impl;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.mapper.UserMapper;
import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import com.kte.backend.repository.UserRepository;
import com.kte.backend.services.authentication.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        if (userRepository.findByEmail(registerRequest.email())) {
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
}
