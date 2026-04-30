package com.finance.tracker.controller;

import com.finance.tracker.dto.LoginRequest;
import com.finance.tracker.dto.LoginResponse;
import com.finance.tracker.dto.UserRequest;
import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.exception.AuthenticationException;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.service.JwtService;
import com.finance.tracker.service.UserService;
import com.finance.tracker.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * REST controller for user authentication and registration endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with provided credentials and role.
     *
     * @param userRequest the user registration request containing name, email, password, and role
     * @return ResponseEntity with success message
     * @throws CommonServiceException if user already exists
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> registerUser(
            @RequestBody @Valid UserRequest userRequest) throws CommonServiceException {

        log.info("User registration attempt for email: {}", userRequest.getEmail());

        UserEntity user = userService.createUser(userRequest);

        log.info("User registered successfully: {} with role: {}", user.getEmail(), user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.<String>builder()
                        .success(true)
                        .timestamp(System.currentTimeMillis())
                        .message("User successfully registered with role: " + user.getRole().getAuthority())
                        .build());
    }

    /**
     * Authenticates user with email and password, returns JWT token.
     *
     * @param loginRequest the login credentials
     * @return ResponseEntity with LoginResponse containing JWT token and user details
     * @throws AuthenticationException if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest loginRequest) throws AuthenticationException {

        log.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get authenticated user details
            UserEntity user = userService.getUserByEmail(loginRequest.getEmail());

            // Generate JWT token with user info and role
            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    user.getUserId(),
                    user.getRole()
            );

            // Build login response
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().getAuthority())
                    .expiresIn(jwtService.getJwtExpiration() / 1000) // Convert to seconds
                    .build();

            log.info("User logged in successfully: {} with role: {}", user.getEmail(), user.getRole());

            return ResponseEntity.ok()
                    .body(BaseResponse.<LoginResponse>builder()
                            .success(true)
                            .timestamp(System.currentTimeMillis())
                            .message("Login successful")
                            .payload(loginResponse)
                            .build());

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {} - Invalid credentials", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", loginRequest.getEmail(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }
}
