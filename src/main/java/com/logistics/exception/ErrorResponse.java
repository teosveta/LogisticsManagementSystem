package com.logistics.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for API errors.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class only handles error response formatting.
 *   It doesn't decide what error occurred - that's the exception's job.
 *
 * Provides consistent error response format across all API endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Short error description (e.g., "Not Found", "Bad Request").
     */
    private String error;

    /**
     * Detailed error message.
     */
    private String message;

    /**
     * Request path that caused the error.
     */
    private String path;

    /**
     * Additional field-level validation errors (for 400 Bad Request).
     * Maps field names to error messages.
     */
    private Map<String, String> validationErrors;

    // Default constructor
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
