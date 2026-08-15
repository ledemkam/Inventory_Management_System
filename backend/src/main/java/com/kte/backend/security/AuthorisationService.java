package com.kte.backend.security;

import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.UserRole;
import com.kte.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorisationService {
    private final UserRepository userRepository;

    /**
     * Checks if the currently authenticated user holds the given role.
     */
    public boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String expectedAuthority = "ROLE_" + role.name();
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expectedAuthority::equals);
    }

    /**
     * Checks if the current user has the ADMIN role.
     */
    public boolean isCurrentUserAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Checks if the current user has the MANAGER role.
     */
    public boolean isCurrentUserManager() {
        return hasRole(UserRole.MANAGER);
    }

    /**
     * Checks if the current user is Admin OR Manager (elevated privileges).
     */
    public boolean isCurrentUserAdminOrManager() {
        return isCurrentUserAdmin() || isCurrentUserManager();
    }

    /**
     * Checks if the user can access/modify a resource.
     * Admins and Managers can access any resource; other users only their own.
     *
     * @param currentUserId   ID from connected user
     * @param resourceOwnerId ID of the resource owner
     */
    public boolean canAccessResource(Long currentUserId, Long resourceOwnerId) {
        boolean allowed = isCurrentUserAdminOrManager() || currentUserId.equals(resourceOwnerId);
        if (!allowed) {
            log.warn("User {} denied access to resource owned by {}", currentUserId, resourceOwnerId);
        }
        return allowed;
    }

    /**
     * Retrieve connected user (avoid circular dependency)
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempt to resolve current user without an authenticated context");
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user {} not found in database", email);
                    return new EntityNotFoundException("Authenticated user not found");
                });
    }
}
