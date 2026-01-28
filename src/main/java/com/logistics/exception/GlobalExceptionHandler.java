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
 * Centralizes exception handling for consistent API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        logger.warn("Validation failed for request to {}", request.getRequestURI());

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

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
