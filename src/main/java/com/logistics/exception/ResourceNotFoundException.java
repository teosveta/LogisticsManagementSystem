package com.logistics.exception;

/**
 * Exception thrown when a requested resource is not found in the database.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This exception handles only the "not found" scenario.
 *   Each exception type handles a specific error condition.
 * - Open/Closed (OCP): New exception types can be added without modifying existing ones.
 *
 * Maps to HTTP 404 Not Found status code.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a ResourceNotFoundException with details about the missing resource.
     *
     * @param resourceName the type of resource (e.g., "User", "Shipment")
     * @param fieldName    the field used to search (e.g., "id", "username")
     * @param fieldValue   the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructs a ResourceNotFoundException with a custom message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
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
