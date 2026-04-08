package com.finance.tracker.exception;

import com.finance.tracker.util.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Component
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<BaseResponse<String>> handleTransactionException(TransactionException e) {
        log.error("TransactionException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    @ExceptionHandler(CommonServiceException.class)
    public ResponseEntity<BaseResponse<String>> handleCommonServiceException(CommonServiceException e) {
        log.error("CommonServiceException: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGenericException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.<String>builder()
                        .success(false)
                        .message("Something went wrong")
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

}
