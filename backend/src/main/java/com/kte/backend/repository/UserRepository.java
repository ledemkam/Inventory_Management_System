package com.kte.backend.repository;


import com.kte.backend.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    boolean findByEmail(String email);

    boolean existsByUsername(String username);
}
