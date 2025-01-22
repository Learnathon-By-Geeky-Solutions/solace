package dev.solace.twiggle.util;

import dev.solace.twiggle.dto.ApiResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

    private ResponseUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static <T> ApiResponse<T> buildResponse(String message, T data, HttpStatus status) {
        Objects.requireNonNull(message, "message must not be null");
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        ApiResponse<T> response = buildResponse(message, data, HttpStatus.OK);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        ApiResponse<T> response = buildResponse(message, data, HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
