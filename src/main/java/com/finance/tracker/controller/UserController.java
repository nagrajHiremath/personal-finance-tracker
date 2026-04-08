package com.finance.tracker.controller;

import com.finance.tracker.dto.LoginRequest;
import com.finance.tracker.dto.TransactionResponse;
import com.finance.tracker.dto.UserRequest;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.service.JwtService;
import com.finance.tracker.service.UserService;
import com.finance.tracker.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> createUser(@RequestBody @Valid UserRequest userRequest) throws CommonServiceException {

        userService.createUser(userRequest);

        return ResponseEntity.ok(
                BaseResponse.<String>builder()
                        .success(true)
                        .timestamp(new Date().getTime())
                        .message("User successfully registered")
                        .build());
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String authToken = jwtService.generateToken(request.getEmail());

        return authToken;
    }

}
