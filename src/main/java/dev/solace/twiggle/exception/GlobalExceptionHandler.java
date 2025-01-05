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

    // Custom exceptions
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), ex.getStatus(), request);
    }

    // 400 BAD REQUEST Exceptions
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String error = ex.getName() + " should be of type "
                + Objects.requireNonNull(ex.getRequiredType()).getName();
        return buildErrorResponse(ex, error, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return buildErrorResponse(ex, "Constraint Violation", HttpStatus.BAD_REQUEST, request, errors);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        return buildErrorResponse(ex, error, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return buildErrorResponse(ex, "Malformed JSON request", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // 403 FORBIDDEN Exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, "Access denied", HttpStatus.FORBIDDEN, request);
    }

    // 404 NOT FOUND Exceptions
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return buildErrorResponse(ex, error, HttpStatus.NOT_FOUND, request);
    }

    // 405 METHOD NOT ALLOWED Exceptions
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

    // 415 UNSUPPORTED MEDIA TYPE Exceptions
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

    // 500 INTERNAL SERVER ERROR Exceptions
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
     * Builds the error response entity with the given parameters.
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception exception, String message, HttpStatus httpStatus, WebRequest request) {
        return buildErrorResponse(exception, message, httpStatus, request, new ArrayList<>());
    }

    /**
     * Builds the error response entity with the given parameters including error details.
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
