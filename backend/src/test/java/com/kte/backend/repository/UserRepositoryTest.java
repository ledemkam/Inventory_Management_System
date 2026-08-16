package com.kte.backend.repository;

import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    PageRequest pageRequest = PageRequest.of(0, 10);


    @Test
    @DisplayName("Find user by email")
    void findByEmail_ReturnsUser_WhenEmailExists() {
        //Given
        User user = userRepository.save(
                User.builder()
                        .username("Jane Doe")
                        .email("jane.doe@example.com")
                        .password("encoded-password")
                        .role(UserRole.ADMIN)
                        .build()
        );

        //When
        boolean found = userRepository.existsByEmail("jane.doe@example.com");

        //Then
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Return false when no user matches the given email")
    void findByEmail_ReturnsFalse_WhenEmailDoesNotExist() {
        //When
        boolean found = userRepository.existsByEmail("missing@example.com");

        //Then
        assertThat(found).isFalse();
    }

    @Test
    @DisplayName("Find all users")
    void find_All_users() {
        //Given
        User user1 = User.builder()
                .username("Jane Doe")
                .email("jane.doe@example.com")
                .password("encoded-password")
                .role(UserRole.ADMIN)
                .build();
        User user2 = User.builder()
                .username("John Smith")
                .email("john.smith@example.com")
                .password("encoded-password")
                .role(UserRole.MANAGER)
                .build();
        userRepository.save(user1);
        userRepository.save(user2);

        //When
        Page<User> users = userRepository.findAll(pageRequest);

        //Then
        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("Save a user")
    void save_user() {
        //Given
        User user = User.builder()
                .username("Jane Doe")
                .email("jane.doe@example.com")
                .password("encoded-password")
                .role(UserRole.ADMIN)
                .build();

        //When
        User savedUser = userRepository.save(user);

        //Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    @DisplayName("Delete a user by ID")
    void delete_user_by_id() {
        //Given
        User user = User.builder()
                .username("Jane Doe")
                .email("jane.doe@example.com")
                .password("encoded-password")
                .role(UserRole.ADMIN)
                .build();
        User savedUser = userRepository.save(user);

        //When
        userRepository.deleteById(savedUser.getId());

        //Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }
}