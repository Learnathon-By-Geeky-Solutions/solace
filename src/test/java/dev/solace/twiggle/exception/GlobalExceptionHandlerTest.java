package dev.solace.twiggle.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
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

/**
 * Unit tests for the {@link GlobalExceptionHandler} class.
 */
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
        CustomException exception =
                new CustomException("Custom error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        ResponseEntity<Object> response = exceptionHandler.handleCustomException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Custom error", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatus());
        assertEquals(ErrorCode.INVALID_REQUEST.name(), error.getCode());
        assertEquals(ErrorCode.INVALID_REQUEST.getSuggestion(), error.getSuggestion());
        assertTrue(error.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnCorrectResponse() throws Exception {
        Method method = getClass().getDeclaredMethod("setUp");
        MethodParameter parameter = new MethodParameter(method, -1);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getGlobalErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Validation failed. Please check the provided data.", error.getMessage());
        assertEquals(ErrorCode.INVALID_REQUEST.name(), error.getCode());
        assertEquals(ErrorCode.INVALID_REQUEST.getSuggestion(), error.getSuggestion());
        assertTrue(error.getDetails().contains("field: defaultMessage"));
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnCorrectResponse() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("paramName");
        when(ex.getRequiredType()).thenAnswer(invocation -> Integer.class);

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("The parameter 'paramName' must be a valid Integer", error.getMessage());
        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE.name(), error.getCode());
        assertEquals(ErrorCode.INVALID_PARAMETER_TYPE.getSuggestion(), error.getSuggestion());
    }

    @Test
    void handleConstraintViolation_ShouldReturnCorrectResponse() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> mockViolation = mock(ConstraintViolation.class);
        jakarta.validation.Path mockPath = mock(jakarta.validation.Path.class);
        when(mockPath.toString()).thenReturn("fieldName");
        when(mockViolation.getPropertyPath()).thenReturn(mockPath);
        when(mockViolation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(mockViolation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolation(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("Validation constraints violated. Please check your input.", error.getMessage());
        assertEquals(ErrorCode.CONSTRAINT_VIOLATION.name(), error.getCode());
        assertEquals(ErrorCode.CONSTRAINT_VIOLATION.getSuggestion(), error.getSuggestion());
        assertTrue(error.getDetails().stream().anyMatch(detail -> detail.contains("fieldName: must not be null")));
    }

    @Test
    void handleMissingServletRequestParameter_ShouldReturnCorrectResponse() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");

        ResponseEntity<Object> response = exceptionHandler.handleMissingServletRequestParameter(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("The required parameter 'param' is missing", error.getMessage());
        assertEquals(ErrorCode.MISSING_PARAMETER.name(), error.getCode());
        assertEquals(ErrorCode.MISSING_PARAMETER.getSuggestion(), error.getSuggestion());
    }

    @Test
    void handleAccessDenied_ShouldReturnCorrectResponse() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Object> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("You don't have permission to access this resource", error.getMessage());
        assertEquals(ErrorCode.ACCESS_DENIED.name(), error.getCode());
        assertEquals(ErrorCode.ACCESS_DENIED.getSuggestion(), error.getSuggestion());
    }

    @Test
    void handleNoHandlerFound_ShouldReturnCorrectResponse() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", new HttpHeaders());

        ResponseEntity<Object> response =
                exceptionHandler.handleNoHandlerFoundException(ex, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("The requested resource '/test' was not found", error.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.name(), error.getCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getSuggestion(), error.getSuggestion());
    }

    @Test
    void handleHttpRequestMethodNotSupported_ShouldReturnCorrectResponse() {
        // Test with supported methods
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("POST", Collections.singletonList("GET"));

        ResponseEntity<Object> response = exceptionHandler.handleHttpRequestMethodNotSupported(
                ex, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals("The POST method is not supported. Supported methods are: GET", error.getMessage());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.name(), error.getCode());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getSuggestion(), error.getSuggestion());

        // Test with null supported methods
        HttpRequestMethodNotSupportedException exWithNull = new HttpRequestMethodNotSupportedException("POST", null);

        ResponseEntity<Object> responseWithNull = exceptionHandler.handleHttpRequestMethodNotSupported(
                exWithNull, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, webRequest);

        assertNotNull(responseWithNull);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseWithNull.getStatusCode());
        ApiErrorResponse errorWithNull = (ApiErrorResponse) responseWithNull.getBody();
        assertNotNull(errorWithNull);
        assertEquals(
                "The POST method is not supported. Supported methods are: No supported methods",
                errorWithNull.getMessage());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.name(), errorWithNull.getCode());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getSuggestion(), errorWithNull.getSuggestion());
    }

    @Test
    void handleHttpMediaTypeNotSupported_ShouldReturnCorrectResponse() {
        List<MediaType> supportedTypes = List.of(MediaType.APPLICATION_JSON);
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, supportedTypes);

        ResponseEntity<Object> response = exceptionHandler.handleHttpMediaTypeNotSupported(
                ex, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertTrue(error.getMessage().contains(MediaType.TEXT_PLAIN.toString()));
        assertTrue(error.getMessage().contains(MediaType.APPLICATION_JSON.toString()));
        assertEquals(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name(), error.getCode());
        assertEquals(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getSuggestion(), error.getSuggestion());
    }

    @Test
    void handleAllUncaughtException_ShouldReturnCorrectResponse() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Object> response = exceptionHandler.handleAllUncaughtException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiErrorResponse error = (ApiErrorResponse) response.getBody();
        assertNotNull(error);
        assertEquals(
                "An unexpected error occurred. Please try again later or contact support if the problem persists.",
                error.getMessage());
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), error.getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getSuggestion(), error.getSuggestion());
    }
}
