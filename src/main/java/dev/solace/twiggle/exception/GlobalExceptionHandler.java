package dev.solace.twiggle.exception;

import io.micrometer.common.lang.NonNullApi;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
 */
@Slf4j
@RestControllerAdvice
@NonNullApi
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), ex.getStatus(), ex.getErrorCode(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> validationErrors = new ArrayList<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.add(error.getField() + ": " + error.getDefaultMessage()));
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> validationErrors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

        return buildErrorResponse(
                ex,
                "Validation failed. Please check the provided data.",
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST,
                request,
                validationErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format(
                "The parameter '%s' must be a valid %s",
                ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());
        return buildErrorResponse(ex, message, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER_TYPE, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> violations = new ArrayList<>();
        ex.getConstraintViolations()
                .forEach(violation -> violations.add(violation.getPropertyPath() + ": " + violation.getMessage()));

        return buildErrorResponse(
                ex,
                "Validation constraints violated. Please check your input.",
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONSTRAINT_VIOLATION,
                request,
                violations);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = String.format("The required parameter '%s' is missing", ex.getParameterName());
        return buildErrorResponse(ex, message, HttpStatus.BAD_REQUEST, ErrorCode.MISSING_PARAMETER, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = String.format("The requested resource '%s' was not found", ex.getRequestURL());
        return buildErrorResponse(ex, message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String supportedMethods = Optional.ofNullable(ex.getSupportedHttpMethods())
                .map(methods ->
                        String.join(", ", methods.stream().map(Object::toString).toList()))
                .orElse("No supported methods");
        String message = String.format(
                "The %s method is not supported. Supported methods are: %s", ex.getMethod(), supportedMethods);
        return buildErrorResponse(ex, message, HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String supportedTypes = String.join(
                ", ",
                ex.getSupportedMediaTypes().stream().map(Objects::toString).toList());
        String message = String.format(
                "The media type %s is not supported. Supported types are: %s", ex.getContentType(), supportedTypes);
        return buildErrorResponse(
                ex, message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(
                ex,
                "An unexpected error occurred. Please try again later or contact support if the problem persists.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                request);
    }

    @ExceptionHandler(io.github.resilience4j.ratelimiter.RequestNotPermitted.class)
    public ResponseEntity<Object> handleRequestNotPermitted(
            io.github.resilience4j.ratelimiter.RequestNotPermitted ex, WebRequest request) {
        return buildErrorResponse(
                ex,
                "Too many requests. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS,
                ErrorCode.RATE_LIMIT_EXCEEDED,
                request);
    }

    private ResponseEntity<Object> buildErrorResponse(
            Exception exception, String message, HttpStatus httpStatus, ErrorCode errorCode, WebRequest request) {
        return buildErrorResponse(exception, message, httpStatus, errorCode, request, new ArrayList<>());
    }

    private ResponseEntity<Object> buildErrorResponse(
            Exception exception,
            String message,
            HttpStatus httpStatus,
            ErrorCode errorCode,
            WebRequest request,
            List<String> details) {
        log.error("Exception occurred:", exception);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .code(errorCode.name())
                .message(message)
                .path(request.getDescription(false))
                .details(details)
                .suggestion(errorCode.getSuggestion())
                .build();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }
}
