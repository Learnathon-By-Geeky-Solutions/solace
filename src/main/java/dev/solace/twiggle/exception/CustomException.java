package dev.solace.twiggle.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;

    /**
     * Constructs a new CustomException with a specific error message and HTTP status.
     *
     * @param message A descriptive error message explaining the cause of the exception
     * @param status The HTTP status code associated with this exception
     */
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
