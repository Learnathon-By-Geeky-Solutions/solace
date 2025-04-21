package dev.solace.twiggle.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.solace.twiggle.dto.ApiResponse;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for the {@link ResponseUtil} class.
 */
class ResponseUtilTest {

    @Test
    @DisplayName("Private constructor should throw exception when invoked via reflection")
    void constructor_ShouldThrowException() {
        InvocationTargetException thrownException = assertThrows(
                InvocationTargetException.class,
                () -> {
                    var constructor = ResponseUtil.class.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    constructor.newInstance();
                },
                "Expected InvocationTargetException when calling private constructor via reflection");

        Throwable cause = thrownException.getCause();
        assertNotNull(cause, "InvocationTargetException should have a cause");
        assertTrue(cause instanceof AssertionError, "Cause should be an AssertionError");
        assertEquals("Utility class should not be instantiated", cause.getMessage());
    }

    @Test
    @DisplayName("success method should return a 200 OK response with data")
    void success_ShouldReturnResponseWith200Status() {
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
        assertEquals(200, body.getStatus());
        assertEquals(message, body.getMessage());
        assertEquals(data, body.getData());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("success method should return a 200 OK response with null data")
    void success_WithNullData_ShouldReturnResponseWith200Status() {
        // Arrange
        String message = "Success message";

        // Act
        ResponseEntity<ApiResponse<Object>> response = ResponseUtil.success(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals(message, body.getMessage());
        assertNull(body.getData());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("created method should return a 201 Created response with data")
    void created_ShouldReturnResponseWith201Status() {
        // Arrange
        String message = "Created message";
        String data = "Test data";

        // Act
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.created(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(201, body.getStatus());
        assertEquals(message, body.getMessage());
        assertEquals(data, body.getData());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("created method should return a 201 Created response with null data")
    void created_WithNullData_ShouldReturnResponseWith201Status() {
        // Arrange
        String message = "Created message";

        // Act
        ResponseEntity<ApiResponse<Object>> response = ResponseUtil.created(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(201, body.getStatus());
        assertEquals(message, body.getMessage());
        assertNull(body.getData());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("buildResponse should throw NullPointerException when message is null")
    void buildResponse_WithNullMessage_ShouldThrowException() {
        InvocationTargetException thrownException = assertThrows(
                InvocationTargetException.class,
                () -> {
                    var method = ResponseUtil.class.getDeclaredMethod(
                            "buildResponse", String.class, Object.class, HttpStatus.class);
                    method.setAccessible(true);
                    method.invoke(null, null, "data", HttpStatus.OK);
                },
                "Expected InvocationTargetException when calling private method via reflection");

        Throwable cause = thrownException.getCause();
        assertNotNull(cause, "InvocationTargetException should have a cause");
        assertTrue(cause instanceof NullPointerException, "Cause should be a NullPointerException");
        assertEquals("message must not be null", cause.getMessage());
    }
}
