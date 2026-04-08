package com.finance.tracker.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
