package com.logistics.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class has one job - handling exceptions and
 *   converting them to appropriate HTTP responses. Business logic stays in services.
 * - Open/Closed (OCP): New exception handlers can be added without modifying existing ones.
 *   Each @ExceptionHandler method is independent.
 * - Dependency Inversion (DIP): We depend on the abstract Exception types, not concrete
 *   implementations from other layers.
 *
 * This handler ensures consistent error response format across all API endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException (HTTP 404).
     * Thrown when a requested entity doesn't exist in the database.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles UnauthorizedException (HTTP 403).
     * Thrown when a user lacks permission for an action.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 403 status
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {

        logger.warn("Unauthorized access attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles InvalidDataException (HTTP 400).
     * Thrown when business validation fails in the service layer.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataException(
            InvalidDataException ex, HttpServletRequest request) {

        logger.warn("Invalid data: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        // If there's a specific field, add it to validation errors
        if (ex.getField() != null) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put(ex.getField(), ex.getMessage());
            errorResponse.setValidationErrors(validationErrors);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles DuplicateResourceException (HTTP 409).
     * Thrown when trying to create a resource that already exists.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 409 status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {

        logger.warn("Duplicate resource: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles validation errors from @Valid annotated request bodies (HTTP 400).
     * Collects all field-level validation errors into a map.
     *
     * @param ex      the exception containing validation errors
     * @param request the HTTP request
     * @return error response with 400 status and field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        logger.warn("Validation failed for request to {}", request.getRequestURI());

        // Collect all field validation errors
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have validation errors",
                request.getRequestURI()
        );
        errorResponse.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles authentication failures (HTTP 401).
     * Thrown when login credentials are invalid.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 401 status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        logger.warn("Authentication failed: Bad credentials");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles general authentication exceptions (HTTP 401).
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 401 status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        logger.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles Spring Security access denied (HTTP 403).
     * Thrown by @PreAuthorize when user lacks required role.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        logger.warn("Access denied to {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions (HTTP 500).
     * Catches any exception not handled by specific handlers above.
     * Logs the full stack trace for debugging.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Log full stack trace for unexpected errors
        logger.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
