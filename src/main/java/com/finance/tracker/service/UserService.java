package com.finance.tracker.service;

import com.finance.tracker.dto.UserRequest;
import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.entity.enums.UserRole;
import com.finance.tracker.exception.AuthenticationException;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for user management operations.
 * Handles user registration, authentication, and role management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with the provided details.
     * Assigns the specified role or defaults to USER role.
     *
     * @param userRequest the user registration request
     * @throws CommonServiceException if user already exists
     */
    public UserEntity createUser(UserRequest userRequest) throws CommonServiceException {
        log.info("Creating user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("User already exists with email: {}", userRequest.getEmail());
            throw new CommonServiceException(
                    "User already exists with this email",
                    HttpStatus.CONFLICT,
                    0x1709); // 409 Conflict
        }

        UserEntity user = UserEntity.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .role(userRequest.getRole() != null ? userRequest.getRole() : UserRole.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        UserEntity savedUser = userRepository.save(user);
        log.info("User created successfully with email: {}", userRequest.getEmail());

        return savedUser;
    }

    /**
     * Retrieves user by email address.
     *
     * @param email the user email
     * @return the user entity
     * @throws AuthenticationException if user not found
     */
    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) throws AuthenticationException {
        log.debug("Retrieving user with email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }

    /**
     * Validates user credentials for authentication.
     *
     * @param email the user email
     * @param password the raw password to validate
     * @return the authenticated user entity
     * @throws AuthenticationException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public UserEntity validateCredentials(String email, String password) throws AuthenticationException {
        log.debug("Validating credentials for email: {}", email);

        UserEntity user = getUserByEmail(email);

        if (!user.getIsActive()) {
            log.warn("Attempt to authenticate inactive user: {}", email);
            throw new AuthenticationException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", email);
            throw new AuthenticationException("Invalid email or password");
        }

        return user;
    }

    /**
     * Retrieves user by ID.
     *
     * @param userId the user ID
     * @return the user entity
     * @throws AuthenticationException if user not found
     */
    @Transactional(readOnly = true)
    public UserEntity getUserById(Long userId) throws AuthenticationException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found with ID: " + userId));
    }
}
