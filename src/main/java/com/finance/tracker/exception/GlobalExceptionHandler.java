package com.finance.tracker.exception;

import com.finance.tracker.util.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles different types of exceptions and returns appropriate HTTP responses.
 */
@Component
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles TransactionException (400 Bad Request).
     */
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<BaseResponse<String>> handleTransactionException(
            TransactionException e, WebRequest request) {
        log.error("TransactionException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * Handles AuthenticationException (401 Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthenticationException(
            AuthenticationException e, WebRequest request) {
        log.warn("AuthenticationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * Handles AuthorizationException (403 Forbidden).
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthorizationException(
            AuthorizationException e, WebRequest request) {
        log.warn("AuthorizationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * Handles Spring Security AccessDeniedException (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleAccessDeniedException(
            AccessDeniedException e, WebRequest request) {
        log.warn("AccessDeniedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message("Access denied: " + e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * Handles CommonServiceException.
     */
    @ExceptionHandler(CommonServiceException.class)
    public ResponseEntity<BaseResponse<String>> handleCommonServiceException(
            CommonServiceException e, WebRequest request) {
        log.error("CommonServiceException: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getHttpStatus())
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * Handles generic exceptions (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGenericException(
            Exception e, WebRequest request) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message("An unexpected error occurred. Please try again later.")
                        .timestamp(System.currentTimeMillis())
                        .build());
    }
}
