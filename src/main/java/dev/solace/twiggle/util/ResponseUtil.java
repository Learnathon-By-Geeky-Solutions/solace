package dev.solace.twiggle.util;

import dev.solace.twiggle.dto.ApiResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    /**
     * Generates a successful API response with HTTP status 200 (OK).
     *
     * @param message A descriptive message about the successful operation
     * @param data The payload/result of the operation, can be of any type
     * @return A ResponseEntity containing an ApiResponse with OK status
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a standardized HTTP 201 (Created) response with a custom message and data payload.
     *
     * @param message A descriptive message about the created resource
     * @param data The payload or created resource to be included in the response
     * @return A ResponseEntity containing an ApiResponse with HTTP status 201
     *
     * @param <T> The type of data being returned in the response
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
