package dev.solace.twiggle.exception;

import lombok.Getter;

/**
 * Enumeration of error codes used across the application.
 * Each error code includes a user-friendly suggestion for resolution.
 */
@Getter
public enum ErrorCode {
    // Client Validation Errors
    INVALID_REQUEST("Please review the validation errors and correct your request."),
    INVALID_PARAMETER_TYPE("Please ensure the parameter value matches the required type."),
    CONSTRAINT_VIOLATION("Please check the input constraints in the API documentation."),
    MISSING_PARAMETER("Please include all required parameters as specified in the documentation."),
    MALFORMED_JSON("Please verify the JSON syntax and data types in your request."),
    INVALID_ARGUMENT("Please check the argument values against the API specifications."),
    UNSUPPORTED_MEDIA_TYPE("Please use one of the supported media types for this endpoint."),
    // Authentication & Authorization Errors
    ACCESS_DENIED("Please ensure you have the necessary permissions or authenticate properly."),
    // Resource & Method Errors
    RESOURCE_NOT_FOUND("Please verify the requested resource exists and the URL is correct."),
    METHOD_NOT_ALLOWED("Please use one of the supported HTTP methods for this endpoint."),
    // System Errors
    INTERNAL_ERROR("Please try again later or contact support if the issue persists."),
    RATE_LIMIT_EXCEEDED(
            "Please wait and try your request again later. Contact support if you need a higher rate limit.");

    private final String suggestion;

    ErrorCode(String suggestion) {
        this.suggestion = suggestion;
    }
}
