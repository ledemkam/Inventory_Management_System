/**
 * JWT/security infrastructure of the {@code user} module. Declared as a
 * {@link org.springframework.modulith.NamedInterface} so the root {@code config} module can wire
 * {@link com.kte.backend.user.security.JwtAuthenticationFilter} and
 * {@link com.kte.backend.user.security.JwtTokenService} into the security filter chain without
 * that being flagged as reaching into the module's internals.
 */
@org.springframework.modulith.NamedInterface
package com.kte.backend.user.security;
