package com.logistics.model.enums;

/**
 * Enumeration representing types of employees in the logistics company.
 *
 * SOLID Principle - Single Responsibility (SRP):
 * This enum has one responsibility: categorizing employee types.
 * Business rules about what each type can do are handled in service classes.
 *
 * Employee types:
 * - COURIER: Delivers shipments to customers
 * - OFFICE_STAFF: Serves customers at office locations, registers shipments
 */
public enum EmployeeType {
    /**
     * Courier employees deliver shipments.
     * They may or may not be assigned to a specific office.
     */
    COURIER,

    /**
     * Office staff employees serve customers at physical locations.
     * They are always assigned to a specific office.
     */
    OFFICE_STAFF
}
