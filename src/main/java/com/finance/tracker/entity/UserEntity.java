package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "name")
    private String name;
    @Column(unique = true, nullable = false, name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
