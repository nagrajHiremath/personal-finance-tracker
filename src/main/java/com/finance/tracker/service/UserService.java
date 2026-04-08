package com.finance.tracker.service;

import com.finance.tracker.dto.UserRequest;
import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.repository.UserRepository;
import io.jsonwebtoken.CompressionException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRequest userRequest) throws CommonServiceException {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new CommonServiceException(
                    "User already exist",
                    HttpStatus.NOT_FOUND,
                    0x1771);
        }

        UserEntity user = UserEntity.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(user);
    }

}
