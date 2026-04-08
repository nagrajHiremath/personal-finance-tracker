package com.finance.tracker.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
public class CommonServiceException extends Exception {
    private static final long serialVersionUID = 943000511917553590L;
    private final HttpStatus httpStatus;
    private final Integer errorCode;

    public CommonServiceException(String message,
            HttpStatus httpStatus, Integer errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

}
