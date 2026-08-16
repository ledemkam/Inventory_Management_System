package com.kte.backend.repository;


import com.kte.backend.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String adminUsername);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
