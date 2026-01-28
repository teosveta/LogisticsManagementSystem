package com.logistics.service;

import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.model.enums.ShipmentStatus;

import java.util.List;

/**
 * Service interface for Shipment operations.
 */
public interface ShipmentService {

    /**
     * Registers a new shipment (Employee only).
     * Automatically calculates the price using PricingService.
     *
     * @param request     the shipment data
     * @param employeeUsername the username of the employee registering the shipment
     * @return the created shipment response
     */
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);

    /**
     * Retrieves a shipment by ID.
     *
     * @param id the shipment ID
     * @return the shipment response
     */
    ShipmentResponse getShipmentById(Long id);

    /**
     * Retrieves all shipments (Employee only).
     *
     * @return list of all shipments
     */
    List<ShipmentResponse> getAllShipments();

    /**
     * Retrieves shipments for a specific customer.
     * Returns only shipments where the customer is sender OR recipient.
     *
     * @param customerId the customer ID
     * @return list of shipments for the customer
     */
    List<ShipmentResponse> getShipmentsByCustomerId(Long customerId);

    /**
     * Updates shipment status (Employee only).
     *
     * @param id      the shipment ID
     * @param request the status update request
     * @return the updated shipment response
     */
    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);

    /**
     * Updates a shipment.
     *
     * @param id      the shipment ID
     * @param request the updated shipment data
     * @return the updated shipment response
     */
    ShipmentResponse updateShipment(Long id, ShipmentRequest request, String employeeUsername);

    /**
     * Deletes a shipment.
     *
     * @param id the shipment ID
     */
    void deleteShipment(Long id);

    /**
     * Gets shipments by status.
     *
     * @param status the status to filter by
     * @return list of shipments with the specified status
     */
    List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status);
}
