package com.kte.backend.user;

import com.kte.backend.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends AbstractEntity implements UserDetails {


    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring's hasRole()/hasAnyRole() expect a "ROLE_" prefix (see JwtAuthenticationFilter,
        // which normalizes the JWT role claim the same way). This isn't currently exercised for
        // authorization decisions - AuthenticationServiceImpl.login() reads role.name() directly
        // rather than authentication.getAuthorities() - but keeping it prefixed avoids a latent
        // mismatch if that ever changes.
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }


}
