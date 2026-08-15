package com.kte.backend.security;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {
        log.info("JwtAuthentificationFilter called for request: {}", request.getRequestURI());
        if (request.getRequestURI().startsWith("/api/v1/auth/login")
                || request.getRequestURI().startsWith("/api/v1/auth/register")) {
            log.info("Skipping authentication for auth endpoint");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && jwtTokenService.validateToken(jwt)) {
                log.info("Valid JWT token found, setting authentication");
                final String userId = jwtTokenService.getUserIdFromTokEN(jwt);
                final String role = jwtTokenService.getRoleFromToken(jwt);


                //Create authentication token
                final SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(authority)
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authentication set for userId: {}, role: {}", userId, role);
            }
        } catch (final Exception e) {
            log.error("Error authenticating user: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);


    }


    private String getJwtFromRequest(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
