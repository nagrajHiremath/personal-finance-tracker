package com.finance.tracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails (401 Unauthorized).
 * Used for invalid credentials, missing/expired tokens.
 */
public class AuthenticationException extends CommonServiceException {

    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 0x0401; // 401 Unauthorized

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
        this.initCause(cause);
    }
}
