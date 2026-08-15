package com.kte.backend.config;

import com.kte.backend.models.entity.User;
import com.kte.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "user@test.com";
            userRepository.findByEmail(email).ifPresentOrElse(
                    user -> {
                    }, // user already exists, do nothing
                    () -> {
                        User newUser = User.builder()
                                .name("Test User")
                                .email(email)
                                .password(passwordEncoder.encode("defaultPassword123"))
                                .createdAt(LocalDateTime.now())
                                .build();
                        userRepository.save(newUser);
                        log.info("user is created: {}", email);
                    }
            );
        };
    }
}
