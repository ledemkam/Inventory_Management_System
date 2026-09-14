package com.kte.backend.user.security;

import com.kte.backend.user.User;
import com.kte.backend.user.UserRole;
import com.kte.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should load the user when the username exists")
    void should_Load_User_By_Username_When_Found() {
        // Given
        final User user = User.builder().id("u1").username("eric").role(UserRole.ADMIN).build();
        when(userRepository.findByUsername("eric")).thenReturn(Optional.of(user));

        // When
        final UserDetails result = customUserDetailsService.loadUserByUsername("eric");

        // Then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw when the username does not exist")
    void should_Throw_When_Username_Not_Found() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
