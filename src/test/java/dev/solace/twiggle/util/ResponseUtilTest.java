package dev.solace.twiggle.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.solace.twiggle.dto.ApiResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for the {@link ResponseUtil} class.
 */
class ResponseUtilTest {

    @Test
    void constructor_ShouldThrowAssertionError() throws Exception {
        Constructor<ResponseUtil> constructor = ResponseUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
        assertEquals(
                "Utility class should not be instantiated", exception.getCause().getMessage());
    }

    @Test
    void success_ShouldReturnResponseEntityWithOkStatus() {
        // Arrange
        String message = "Success message";
        String data = "Test data";
        LocalDateTime beforeTest = LocalDateTime.now(ZoneOffset.UTC);

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

        // Verify timestamp is between test start and now + 1 second
        LocalDateTime responseTime = body.getTimestamp();
        assertTrue(
                responseTime.isAfter(beforeTest) || responseTime.isEqual(beforeTest),
                "Response timestamp should not be before test start time");
        assertTrue(
                responseTime.isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)),
                "Response timestamp should be before current time + 1 second");
    }

    @Test
    void success_ShouldHandleNullData() {
        // Arrange
        String message = "Success with null data";
        LocalDateTime beforeTest = LocalDateTime.now(ZoneOffset.UTC);

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

        // Verify timestamp
        LocalDateTime responseTime = body.getTimestamp();
        assertTrue(responseTime.isAfter(beforeTest) || responseTime.isEqual(beforeTest));
        assertTrue(responseTime.isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
    }

    @Test
    void created_ShouldReturnResponseEntityWithCreatedStatus() {
        // Arrange
        String message = "Created message";
        Long data = 123L;
        LocalDateTime beforeTest = LocalDateTime.now(ZoneOffset.UTC);

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

        // Verify timestamp
        LocalDateTime responseTime = body.getTimestamp();
        assertTrue(responseTime.isAfter(beforeTest) || responseTime.isEqual(beforeTest));
        assertTrue(responseTime.isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
    }

    @Test
    void created_ShouldHandleNullData() {
        // Arrange
        String message = "Created with null data";
        LocalDateTime beforeTest = LocalDateTime.now(ZoneOffset.UTC);

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

        // Verify timestamp
        LocalDateTime responseTime = body.getTimestamp();
        assertTrue(responseTime.isAfter(beforeTest) || responseTime.isEqual(beforeTest));
        assertTrue(responseTime.isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
    }
}
