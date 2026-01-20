package com.logistics.exception;

/**
 * Exception thrown when data validation fails at the business logic level.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This exception handles data validation errors
 *   that occur in the service layer (beyond basic DTO validation).
 *
 * Maps to HTTP 400 Bad Request status code.
 *
 * Examples of when to use:
 * - Shipment has neither delivery address nor office (business rule violation)
 * - Shipment has BOTH delivery address AND office (mutually exclusive)
 * - Trying to update status of a cancelled shipment
 * - Duplicate registration number when creating company
 */
public class InvalidDataException extends RuntimeException {

    private final String field;

    /**
     * Constructs an InvalidDataException with a message.
     *
     * @param message description of the validation failure
     */
    public InvalidDataException(String message) {
        super(message);
        this.field = null;
    }

    /**
     * Constructs an InvalidDataException with a field name and message.
     *
     * @param field   the field that failed validation
     * @param message description of the validation failure
     */
    public InvalidDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
