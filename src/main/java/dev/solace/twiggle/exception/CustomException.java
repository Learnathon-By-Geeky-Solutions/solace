package dev.solace.twiggle.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for custom application exceptions.
 */
@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorCode;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public CustomException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
