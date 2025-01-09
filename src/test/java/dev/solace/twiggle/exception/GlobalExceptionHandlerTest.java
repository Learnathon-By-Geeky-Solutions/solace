package dev.solace.twiggle.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        AutoCloseable closeable = MockitoAnnotations.openMocks(this);
        try {
            exceptionHandler = new GlobalExceptionHandler();
            when(webRequest.getDescription(false)).thenReturn("uri=/test");
        } catch (Exception e) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // Ignore any exceptions during cleanup
            }
            throw new RuntimeException(e);
        }
    }

    @Test
    void handleCustomException_ShouldReturnCorrectResponse() {
        // Arrange
        CustomException exception = new CustomException("Custom error", HttpStatus.BAD_REQUEST);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleCustomException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Custom error", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
        assertTrue(error.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnCorrectResponse() {
        // Arrange
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getGlobalErrors()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Validation Failed", error.getMessage());
        assertTrue(error.getDetails().contains("field: defaultMessage"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnCorrectResponse() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("paramName");
        when(ex.getRequiredType()).thenAnswer(invocation -> Integer.class);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("paramName should be of type java.lang.Integer", error.getMessage());
    }

    @Test
    void handleConstraintViolation_ShouldReturnCorrectResponse() {
        // Arrange
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = mock(ConstraintViolation.class);

        // Create a mock Path that returns a known string representation
        jakarta.validation.Path mockPath = mock(jakarta.validation.Path.class);
        when(mockPath.toString()).thenReturn("fieldName");
        when(mockViolation.getPropertyPath()).thenReturn(mockPath);
        when(mockViolation.getMessage()).thenReturn("must not be null");

        // Create a new mutable set with our mock violation
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(mockViolation);

        // Create the exception with our violations
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolation(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Constraint Violation", error.getMessage());

        // Verify the details contain our violation
        List<String> details = error.getDetails();
        assertNotNull(details);
        assertTrue(details.stream().anyMatch(detail -> detail.contains("fieldName: must not be null")));
    }

    @Test
    void handleMissingServletRequestParameter_ShouldReturnCorrectResponse() {
        // Arrange
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMissingServletRequestParameter(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("param parameter is missing", error.getMessage());
    }

    @Test
    void handleAccessDenied_ShouldReturnCorrectResponse() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Access denied", error.getMessage());
    }

    @Test
    void handleNoHandlerFound_ShouldReturnCorrectResponse() {
        // Arrange
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", new HttpHeaders());

        // Act
        ResponseEntity<Object> response =
                exceptionHandler.handleNoHandlerFoundException(ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertTrue(error.getMessage().contains("No handler found for GET /test"));
    }

    @Test
    void handleHttpRequestMethodNotSupported_ShouldReturnCorrectResponse() {
        // Arrange
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("POST", Collections.singletonList("GET"));

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleHttpRequestMethodNotSupported(
                ex, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertTrue(error.getMessage().contains("POST method is not supported"));
    }

    @Test
    void handleHttpMediaTypeNotSupported_ShouldReturnCorrectResponse() {
        // Arrange
        List<MediaType> supportedMediaTypes = Collections.singletonList(MediaType.APPLICATION_JSON);
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException("Unsupported media type", supportedMediaTypes);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleHttpMediaTypeNotSupported(
                ex, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertNotNull(error.getMessage());
    }

    @Test
    void handleAllUncaughtException_ShouldReturnCorrectResponse() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllUncaughtException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("An unexpected error occurred", error.getMessage());
        assertTrue(error.getDetails().contains("Error: Unexpected error"));
    }
}
