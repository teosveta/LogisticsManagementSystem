package com.logistics.util;

import com.logistics.dto.company.CompanyResponse;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.model.entity.*;

/**
 * Converts entities to DTOs. Keeps entity internals separate from API responses.
 */
public final class EntityMapper {

    private EntityMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

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

    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setUserId(employee.getUser().getId());
        response.setUsername(employee.getUser().getUsername());
        response.setName(employee.getUser().getUsername());
        response.setEmail(employee.getUser().getEmail());

        if (employee.getCompany() != null) {
            response.setCompanyId(employee.getCompany().getId());
            response.setCompanyName(employee.getCompany().getName());
        }
        response.setEmployeeType(employee.getEmployeeType());

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

    public static CustomerResponse toCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setUserId(customer.getUser().getId());
        response.setUsername(customer.getUser().getUsername());
        response.setName(customer.getUser().getUsername());
        response.setEmail(customer.getUser().getEmail());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    public static ShipmentResponse toShipmentResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());

        response.setSenderId(shipment.getSender().getId());
        response.setSenderName(shipment.getSender().getUser().getUsername());
        response.setSenderEmail(shipment.getSender().getUser().getEmail());

        response.setRecipientId(shipment.getRecipient().getId());
        response.setRecipientName(shipment.getRecipient().getUser().getUsername());
        response.setRecipientEmail(shipment.getRecipient().getUser().getEmail());
        response.setReceiverName(shipment.getRecipient().getUser().getUsername());

        response.setRegisteredById(shipment.getRegisteredBy().getId());
        response.setRegisteredByName(shipment.getRegisteredBy().getUser().getUsername());

        if (shipment.getOriginOffice() != null) {
            response.setOriginOfficeId(shipment.getOriginOffice().getId());
            response.setOriginOfficeName(shipment.getOriginOffice().getName());
        }

        response.setDeliveryAddress(shipment.getDeliveryAddress());
        response.setDeliverToAddress(shipment.isAddressDelivery());
        if (shipment.getDeliveryOffice() != null) {
            response.setDeliveryOfficeId(shipment.getDeliveryOffice().getId());
            response.setDeliveryOfficeName(shipment.getDeliveryOffice().getName());
            response.setDestinationOfficeName(shipment.getDeliveryOffice().getName());
        }
        response.setDeliveryDestination(shipment.getDeliveryDestination());

        response.setWeight(shipment.getWeight());
        response.setPrice(shipment.getPrice());
        response.setStatus(shipment.getStatus());

        response.setRegisteredAt(shipment.getRegisteredAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setUpdatedAt(shipment.getUpdatedAt());

        return response;
    }
}
