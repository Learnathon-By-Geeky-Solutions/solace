package dev.solace.twiggle.exception;

import io.micrometer.common.lang.NonNullApi;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the application.
 * Handles various types of exceptions and converts them to appropriate API responses.
 */
@Slf4j
@RestControllerAdvice
@NonNullApi
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles custom exceptions thrown within the application.
     *
     * @param ex The custom exception that was thrown
     * @param request The web request during which the exception occurred
     * @return A ResponseEntity containing the error details with appropriate HTTP status
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), ex.getStatus(), request);
    }

    /**
     * Handles validation errors when method arguments fail validation constraints.
     *
     * This method is triggered when a method receives invalid arguments, typically during request validation.
     * It collects and aggregates field-level and global validation errors from the binding result.
     *
     * @param ex The exception containing validation errors
     * @param headers HTTP headers of the request
     * @param status HTTP status code
     * @param request The current web request
     * @return A ResponseEntity with detailed validation error messages and BAD_REQUEST status
     *
     * @see MethodArgumentNotValidException
     * @see BindingResult
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<String> errors = new ArrayList<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

        return buildErrorResponse(ex, "Validation Failed", HttpStatus.BAD_REQUEST, request, errors);
    }

    /**
     * Handles method argument type mismatch exceptions, converting them into a structured API error response.
     *
     * @param ex The {@link MethodArgumentTypeMismatchException} that was thrown when a method argument could not be converted to the expected type
     * @param request The current web request context
     * @return A {@link ResponseEntity} with a BAD REQUEST status and an error message detailing the type mismatch
     *
     * @see MethodArgumentTypeMismatchException
     * @see HttpStatus#BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String error = ex.getName() + " should be of type "
                + Objects.requireNonNull(ex.getRequiredType()).getName();
        return buildErrorResponse(ex, error, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles constraint violation exceptions during request validation.
     *
     * @param ex The constraint violation exception containing validation errors
     * @param request The current web request context
     * @return A ResponseEntity with detailed validation error messages and BAD_REQUEST status
     *
     * @see ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return buildErrorResponse(ex, "Constraint Violation", HttpStatus.BAD_REQUEST, request, errors);
    }

    /**
     * Handles exceptions when a required servlet request parameter is missing.
     *
     * @param ex      The {@link MissingServletRequestParameterException} thrown when a required parameter is not provided
     * @param headers The HTTP headers of the request
     * @param status  The HTTP status code
     * @param request The web request context
     * @return A {@link ResponseEntity} with a detailed error response for the missing parameter
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        return buildErrorResponse(ex, error, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles exceptions when the HTTP request body cannot be parsed or is malformed.
     *
     * This method is triggered when the server cannot read or deserialize the incoming JSON request,
     * typically due to invalid JSON syntax or incompatible data types.
     *
     * @param ex The {@link HttpMessageNotReadableException} thrown when the request body is unreadable
     * @param headers The HTTP headers of the request
     * @param status The HTTP status code of the request
     * @param request The current web request
     * @return A {@link ResponseEntity} with a BAD_REQUEST status and an error message describing the issue
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return buildErrorResponse(ex, "Malformed JSON request", HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles IllegalArgumentException by creating a BAD REQUEST response with the exception message.
     *
     * @param ex The IllegalArgumentException that was thrown
     * @param request The current web request context
     * @return A ResponseEntity with error details and BAD REQUEST (400) status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles AccessDeniedException by generating a 403 Forbidden error response.
     *
     * This method is triggered when a user attempts to access a resource or perform an action
     * without sufficient permissions or authentication.
     *
     * @param ex The AccessDeniedException that was thrown
     * @param request The current web request context
     * @return A ResponseEntity with a 403 Forbidden status and a generic access denied message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, "Access denied", HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles cases where no handler is found for a specific HTTP request.
     *
     * This method is triggered when the application cannot find a matching handler
     * for a given HTTP method and URL. It creates a detailed error response
     * indicating the unhandled request's method and URL.
     *
     * @param ex The {@link NoHandlerFoundException} thrown when no handler is found
     * @param headers The HTTP headers of the request
     * @param status The HTTP status code of the request
     * @param request The current web request
     * @return A {@link ResponseEntity} with a 404 NOT FOUND status and error details
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return buildErrorResponse(ex, error, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles HTTP requests with unsupported methods by generating a detailed error response.
     *
     * @param ex The exception thrown when an unsupported HTTP method is used
     * @param headers The HTTP headers of the request
     * @param status The HTTP status code
     * @param request The web request that triggered the exception
     * @return A ResponseEntity containing an error response with details about supported methods
     *
     * @throws NullPointerException if the supported HTTP methods are null
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        StringBuilder error = new StringBuilder();
        error.append(ex.getMethod());
        error.append(" method is not supported for this request. Supported methods are ");
        Objects.requireNonNull(ex.getSupportedHttpMethods())
                .forEach(t -> error.append(t).append(" "));

        return buildErrorResponse(ex, error.toString(), HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    /**
     * Handles exceptions related to unsupported media types in HTTP requests.
     *
     * This method is called when a client sends a request with a media type that is not supported
     * by the server. It constructs a detailed error message listing the unsupported media type
     * and the media types that are actually supported.
     *
     * @param ex The {@link HttpMediaTypeNotSupportedException} thrown when an unsupported media type is encountered
     * @param headers The HTTP headers of the request
     * @param status The HTTP status code
     * @param request The current web request
     * @return A {@link ResponseEntity} with a detailed error response and UNSUPPORTED_MEDIA_TYPE (415) status
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        StringBuilder error = new StringBuilder();
        error.append(ex.getContentType());
        error.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> error.append(t).append(", "));

        return buildErrorResponse(
                ex, error.substring(0, error.length() - 2), HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
    }

    /**
     * Handles all uncaught exceptions in the application, providing a comprehensive error response.
     *
     * This method is the catch-all exception handler for any unhandled exceptions, logging the error
     * and constructing a detailed error response with limited stack trace information for debugging.
     *
     * @param ex The unexpected exception that was thrown and not caught by other handlers
     * @param request The web request during which the exception occurred
     * @return A ResponseEntity containing error details with a 500 Internal Server Error status
     *
     * @see ResponseEntity
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unknown error occurred", ex);

        List<String> details = new ArrayList<>();
        details.add("Error: " + ex.getMessage());

        // Add stack trace elements for debugging (first 5 elements)
        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            details.add("Stack trace:");
            for (int i = 0; i < Math.min(5, ex.getStackTrace().length); i++) {
                details.add(ex.getStackTrace()[i].toString());
            }
        }

        return buildErrorResponse(
                ex, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request, details);
    }

    /**
     * Constructs a basic error response entity with default empty error details.
     *
     * @param exception The exception that triggered the error response
     * @param message A descriptive error message explaining the cause of the error
     * @param httpStatus The HTTP status code to be returned with the response
     * @param request The web request that resulted in the error
     * @return A ResponseEntity containing the structured error response
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception exception, String message, HttpStatus httpStatus, WebRequest request) {
        return buildErrorResponse(exception, message, httpStatus, request, new ArrayList<>());
    }

    /**
     * Constructs a detailed error response entity with comprehensive error information.
     *
     * @param exception The original exception that triggered the error response
     * @param message A descriptive error message explaining the cause of the error
     * @param httpStatus The HTTP status code to be returned with the response
     * @param request The web request that led to the error
     * @param errors A list of additional error details or validation messages
     * @return A ResponseEntity containing an ApiErrorResponse with full error context
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception exception, String message, HttpStatus httpStatus, WebRequest request, List<String> errors) {

        errors.add("Exception: " + exception.getClass().getName());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false))
                .details(errors)
                .build();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}
