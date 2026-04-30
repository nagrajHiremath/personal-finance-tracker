package com.finance.tracker.service;

import com.finance.tracker.entity.enums.UserRole;
import com.finance.tracker.exception.AuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service for JWT token generation and validation.
 * Includes user email and role in the token claims.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration; // Default: 1 hour in milliseconds

    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_CLAIM = "userId";

    /**
     * Generates a JWT token for the given user with role information.
     *
     * @param userEmail the user's email
     * @param userId the user's ID
     * @param role the user's role
     * @return the JWT token
     */
    public String generateToken(String userEmail, Long userId, UserRole role) {
        long expirationTime = System.currentTimeMillis() + jwtExpiration;

        try {
            return Jwts.builder()
                    .setSubject(userEmail)
                    .claim(USER_ID_CLAIM, userId)
                    .claim(ROLE_CLAIM, role.getAuthority())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(expirationTime))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", userEmail, e);
            throw new RuntimeException("Failed to generate authentication token", e);
        }
    }

    /**
     * Extracts the username/email from the JWT token.
     *
     * @param token the JWT token
     * @return the username (email)
     * @throws AuthenticationException if token is invalid or expired
     */
    public String extractUsername(String token) throws AuthenticationException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired");
            throw new AuthenticationException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token");
            throw new AuthenticationException("Unsupported token format");
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token");
            throw new AuthenticationException("Invalid token");
        } catch (SignatureException e) {
            log.warn("JWT token signature is invalid");
            throw new AuthenticationException("Invalid token signature");
        } catch (Exception e) {
            log.warn("Error extracting username from token", e);
            throw new AuthenticationException("Failed to validate token");
        }
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     * @throws AuthenticationException if token is invalid
     */
    public Long extractUserId(String token) throws AuthenticationException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(USER_ID_CLAIM, Long.class);
        } catch (JwtException e) {
            throw new AuthenticationException("Failed to extract user ID from token");
        }
    }

    /**
     * Extracts the user role from the JWT token.
     *
     * @param token the JWT token
     * @return the user role as string
     * @throws AuthenticationException if token is invalid
     */
    public String extractRole(String token) throws AuthenticationException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(ROLE_CLAIM, String.class);
        } catch (JwtException e) {
            throw new AuthenticationException("Failed to extract role from token");
        }
    }

    /**
     * Validates the JWT token.
     *
     * @param token the JWT token
     * @return true if token is valid
     * @throws AuthenticationException if token is invalid
     */
    public boolean validateToken(String token) throws AuthenticationException {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired");
            throw new AuthenticationException("Token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }

    /**
     * Gets the JWT token expiration time in milliseconds.
     *
     * @return expiration time
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
