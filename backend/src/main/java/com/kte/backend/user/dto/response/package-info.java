/**
 * Response DTOs are the {@code user} module's public read API - other modules (e.g.
 * {@code transaction}, which embeds a {@link com.kte.backend.user.dto.response.UserResponse} in
 * its own response) are allowed to depend on them.
 */
@org.springframework.modulith.NamedInterface
package com.kte.backend.user.dto.response;
