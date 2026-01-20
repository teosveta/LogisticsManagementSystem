package com.logistics.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This exception handles only duplicate resource scenarios.
 *
 * Maps to HTTP 409 Conflict status code.
 *
 * Examples of when to use:
 * - Username already exists during registration
 * - Email already exists during registration
 * - Company registration number already exists
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a DuplicateResourceException with details about the duplicate.
     *
     * @param resourceName the type of resource (e.g., "User")
     * @param fieldName    the field that must be unique (e.g., "username")
     * @param fieldValue   the duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
