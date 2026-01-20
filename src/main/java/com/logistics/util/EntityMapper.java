package com.logistics.util;

import com.logistics.dto.company.CompanyResponse;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.model.entity.*;

/**
 * Utility class for converting entities to DTOs.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class has ONE job - mapping entities to DTOs.
 *   It doesn't contain business logic, validation, or persistence code.
 * - Open/Closed (OCP): New mapping methods can be added without modifying existing ones.
 *
 * Why use a mapper?
 * 1. Separates concerns - entities handle persistence, DTOs handle API communication
 * 2. Prevents exposing internal entity relationships directly
 * 3. Allows customizing what data is exposed in responses
 * 4. Makes it easy to change DTO structure without affecting entities
 *
 * Note: For larger projects, consider using MapStruct for automatic mapping.
 */
public final class EntityMapper {

    // Private constructor - utility class should not be instantiated
    private EntityMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a Company entity to CompanyResponse DTO.
     *
     * @param company the entity to convert
     * @return the DTO representation
     */
    public static CompanyResponse toCompanyResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setRegistrationNumber(company.getRegistrationNumber());
        response.setAddress(company.getAddress());
        response.setPhone(company.getPhone());
        response.setEmail(company.getEmail());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }

    /**
     * Converts an Office entity to OfficeResponse DTO.
     * Includes company name for display without exposing full company entity.
     *
     * @param office the entity to convert
     * @return the DTO representation
     */
    public static OfficeResponse toOfficeResponse(Office office) {
        OfficeResponse response = new OfficeResponse();
        response.setId(office.getId());
        response.setCompanyId(office.getCompany().getId());
        response.setCompanyName(office.getCompany().getName());
        response.setName(office.getName());
        response.setAddress(office.getAddress());
        response.setCity(office.getCity());
        response.setCountry(office.getCountry());
        response.setPhone(office.getPhone());
        response.setFullAddress(office.getFullAddress());
        response.setCreatedAt(office.getCreatedAt());
        response.setUpdatedAt(office.getUpdatedAt());
        return response;
    }

    /**
     * Converts an Employee entity to EmployeeResponse DTO.
     * Includes user and company info for display.
     *
     * @param employee the entity to convert
     * @return the DTO representation
     */
    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setUserId(employee.getUser().getId());
        response.setUsername(employee.getUser().getUsername());
        response.setEmail(employee.getUser().getEmail());
        response.setCompanyId(employee.getCompany().getId());
        response.setCompanyName(employee.getCompany().getName());
        response.setEmployeeType(employee.getEmployeeType());

        // Office may be null for couriers
        if (employee.getOffice() != null) {
            response.setOfficeId(employee.getOffice().getId());
            response.setOfficeName(employee.getOffice().getName());
        }

        response.setHireDate(employee.getHireDate());
        response.setSalary(employee.getSalary());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    /**
     * Converts a Customer entity to CustomerResponse DTO.
     * Includes user info for display.
     *
     * @param customer the entity to convert
     * @return the DTO representation
     */
    public static CustomerResponse toCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setUserId(customer.getUser().getId());
        response.setUsername(customer.getUser().getUsername());
        response.setEmail(customer.getUser().getEmail());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    /**
     * Converts a Shipment entity to ShipmentResponse DTO.
     * Includes sender, recipient, and employee info for display.
     * Includes calculated delivery destination description.
     *
     * @param shipment the entity to convert
     * @return the DTO representation
     */
    public static ShipmentResponse toShipmentResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());

        // Sender info
        response.setSenderId(shipment.getSender().getId());
        response.setSenderName(shipment.getSender().getUser().getUsername());
        response.setSenderEmail(shipment.getSender().getUser().getEmail());

        // Recipient info
        response.setRecipientId(shipment.getRecipient().getId());
        response.setRecipientName(shipment.getRecipient().getUser().getUsername());
        response.setRecipientEmail(shipment.getRecipient().getUser().getEmail());

        // Registered by employee info
        response.setRegisteredById(shipment.getRegisteredBy().getId());
        response.setRegisteredByName(shipment.getRegisteredBy().getUser().getUsername());

        // Delivery destination
        response.setDeliveryAddress(shipment.getDeliveryAddress());
        if (shipment.getDeliveryOffice() != null) {
            response.setDeliveryOfficeId(shipment.getDeliveryOffice().getId());
            response.setDeliveryOfficeName(shipment.getDeliveryOffice().getName());
        }
        response.setDeliveryDestination(shipment.getDeliveryDestination());

        // Shipment details
        response.setWeight(shipment.getWeight());
        response.setPrice(shipment.getPrice());
        response.setStatus(shipment.getStatus());

        // Timestamps
        response.setRegisteredAt(shipment.getRegisteredAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setUpdatedAt(shipment.getUpdatedAt());

        return response;
    }
}
