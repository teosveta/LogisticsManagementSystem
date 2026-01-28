package com.logistics.exception;

/**
 * Thrown when business validation fails. Maps to HTTP 400.
 */
public class InvalidDataException extends RuntimeException {

    private final String field;

    public InvalidDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
