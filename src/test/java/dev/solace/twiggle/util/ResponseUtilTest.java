package dev.solace.twiggle.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.solace.twiggle.dto.ApiResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseUtilTest {

    @Test
    void success_ShouldReturnResponseEntityWithOkStatus() {
        // Arrange
        String message = "Success message";
        String data = "Test data";

        // Act
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getStatus());
        assertEquals(message, body.getMessage());
        assertEquals(data, body.getData());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void success_ShouldHandleNullData() {
        // Arrange
        String message = "Success with null data";

        // Act
        ResponseEntity<ApiResponse<Object>> response = ResponseUtil.success(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getStatus());
        assertEquals(message, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void created_ShouldReturnResponseEntityWithCreatedStatus() {
        // Arrange
        String message = "Created message";
        Long data = 123L;

        // Act
        ResponseEntity<ApiResponse<Long>> response = ResponseUtil.created(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<Long> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.CREATED.value(), body.getStatus());
        assertEquals(message, body.getMessage());
        assertEquals(data, body.getData());
        assertNotNull(body.getTimestamp());
        assertTrue(body.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void created_ShouldHandleNullData() {
        // Arrange
        String message = "Created with null data";

        // Act
        ResponseEntity<ApiResponse<Object>> response = ResponseUtil.created(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.CREATED.value(), body.getStatus());
        assertEquals(message, body.getMessage());
        assertNull(body.getData());
    }
}
