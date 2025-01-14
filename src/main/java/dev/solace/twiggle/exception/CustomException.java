package dev.solace.twiggle.exception;

import java.util.Objects;
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
        super(Objects.requireNonNull(message, "message must not be null"));
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public CustomException(String message, HttpStatus status, ErrorCode errorCode) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
