package com.finance.tracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authorization fails (403 Forbidden).
 * Used when user lacks required permissions/roles.
 */
public class AuthorizationException extends CommonServiceException {

    private static final long serialVersionUID = 1L;
    private static final int ERROR_CODE = 0x0403; // 403 Forbidden

    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
        this.initCause(cause);
    }
}
