package com.logistics.exception;

/**
 * Exception thrown when a user attempts an action they are not authorized to perform.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This exception handles only authorization failures.
 *   Authentication failures (invalid credentials) are handled separately.
 *
 * Maps to HTTP 403 Forbidden status code.
 * Use this when a user IS authenticated but LACKS permission for the action.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs an UnauthorizedException with a message.
     *
     * @param message description of why the action is unauthorized
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs an UnauthorizedException with a message and cause.
     *
     * @param message description of why the action is unauthorized
     * @param cause   the underlying cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
