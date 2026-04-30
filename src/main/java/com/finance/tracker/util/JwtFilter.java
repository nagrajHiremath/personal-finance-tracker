package com.finance.tracker.util;

import com.finance.tracker.service.JwtService;
import com.finance.tracker.exception.AuthenticationException;
import com.finance.tracker.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT authentication filter that processes Bearer tokens.
 * Extracts user information and roles from JWT and sets up SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                processJwtToken(token);
            }
        } catch (Exception e) {
            log.error("Error processing JWT filter", e);
        } finally {
            try {
                chain.doFilter(request, response);
            } catch (Exception e) {
                log.error("Error in filter chain", e);
            }
        }
    }

    /**
     * Processes and validates JWT token, extracting user info and roles.
     *
     * @param token the JWT token
     */
    private void processJwtToken(String token) {
        try {
            // Validate token
            jwtService.validateToken(token);

            // Extract user information
            String username = jwtService.extractUsername(token);
            String roleString = jwtService.extractRole(token);

            // Convert role string to UserRole enum
            UserRole role = UserRole.fromAuthority(roleString);

            // Create authorities list with the role
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(role);

            // Create authentication token
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("User authenticated successfully: {} with role: {}", username, role.getAuthority());

        } catch (AuthenticationException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role in JWT token", e);
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Unexpected error processing JWT token", e);
            SecurityContextHolder.clearContext();
        }
    }
}
