package com.kte.backend.config;


import com.kte.backend.user.security.JwtAuthenticationFilter;
import com.kte.backend.user.security.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
// proxyTargetClass = true forces CGLIB (class-based) proxies for @PreAuthorize-secured
// beans instead of JDK dynamic proxies. Controllers such as UserController implement an
// interface (UIUserController) that doesn't carry the @RequestMapping annotations; a JDK
// proxy would only expose that interface, silently dropping all route mappings.
@EnableMethodSecurity(proxyTargetClass = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create AuthenticationManager", e);
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenService jwtTokenService) {
        // JwtAuthenticationFilter is intentionally NOT a @Bean: if it were, Spring Boot/MockMvc
        // auto-registers any Filter-typed bean as a standalone servlet filter in addition to
        // wiring it here via addFilterBefore. That duplicate, out-of-band run executes before
        // FilterChainProxy's SecurityContextHolderFilter, which then resets the context and
        // (via OncePerRequestFilter's already-filtered guard) prevents the properly-placed
        // execution from running at all, silently dropping the authentication it sets.
        final JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenService);
        try {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll()
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-ui.html",
                                    "/swagger-resources/**",
                                    "/webjars/**"
                            ).permitAll()
                            .anyRequest().authenticated()
                    )
                    // CSRF protection defends against a browser automatically attaching ambient
                    // credentials (session cookies) to a forged cross-site request. It is not
                    // needed here: sessions are STATELESS (no JSESSIONID cookie is ever issued)
                    // and authentication is derived solely from the "Authorization: Bearer <jwt>"
                    // header (see JwtAuthenticationFilter), which a cross-site page cannot make
                    // the victim's browser send. No endpoint in this app sets or reads an auth
                    // cookie. CorsConfig also restricts allowed origins and disables credentialed
                    // CORS requests. Sonar hotspot java:S4502 reviewed and accepted as Safe for
                    // this reason; re-evaluate if JWT storage ever moves to a cookie.
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure security filter chain", e);
        }
    }
}
