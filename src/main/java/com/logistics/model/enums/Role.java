package com.logistics.model.enums;

/**
 * Enumeration representing user roles in the system.
 *
 * Role-based access control:
 * - EMPLOYEE: Can manage shipments, view all data, access all reports
 * - CUSTOMER: Can only view their own shipments (sent or received)
 */
public enum Role {
    /**
     * Employee role - full access to shipment management and all reports.
     * Employees can be either couriers or office staff.
     */
    EMPLOYEE,

    /**
     * Customer role - limited access to view only their own shipments.
     * Customers can send and receive shipments but cannot manage system data.
     */
    CUSTOMER
}
