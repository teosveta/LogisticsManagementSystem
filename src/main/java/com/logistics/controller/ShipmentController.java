package com.logistics.controller;

import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.exception.UnauthorizedException;
import com.logistics.model.entity.Customer;
import com.logistics.model.enums.Role;
import com.logistics.repository.CustomerRepository;
import com.logistics.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for shipment management.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only shipment-related HTTP endpoints.
 * - Dependency Inversion (DIP): Depends on ShipmentService interface.
 *
 * Access Control:
 * - EMPLOYEE: Can view ALL shipments, create/edit/delete shipments, update status
 * - CUSTOMER: Can ONLY view shipments where they are sender OR recipient
 */
@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "Shipment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentController.class);

    private final ShipmentService shipmentService;
    private final CustomerRepository customerRepository;

    public ShipmentController(ShipmentService shipmentService, CustomerRepository customerRepository) {
        this.shipmentService = shipmentService;
        this.customerRepository = customerRepository;
    }

    /**
     * Registers a new shipment (Employee only).
     * Price is calculated automatically based on weight and delivery type.
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Register shipment", description = "Registers a new shipment (Employee only). Price calculated automatically.")
    public ResponseEntity<ShipmentResponse> registerShipment(
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {

        String employeeUsername = authentication.getName();
        logger.info("Registering shipment by employee: {}", employeeUsername);

        ShipmentResponse response = shipmentService.registerShipment(request, employeeUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a shipment by ID.
     * Employees can view any shipment.
     * Customers can only view shipments where they are sender or recipient.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID", description = "Retrieves a shipment. Customers can only see their own shipments.")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable Long id,
            Authentication authentication) {

        ShipmentResponse shipment = shipmentService.getShipmentById(id);

        // If customer, verify they have access to this shipment
        if (isCustomer(authentication)) {
            Long customerId = getCustomerIdFromAuth(authentication);
            if (!shipment.getSenderId().equals(customerId) &&
                    !shipment.getRecipientId().equals(customerId)) {
                throw new UnauthorizedException("You can only view shipments where you are sender or recipient");
            }
        }

        return ResponseEntity.ok(shipment);
    }

    /**
     * Gets all shipments.
     * Employees see ALL shipments.
     * Customers see only their own (filtered by service).
     */
    @GetMapping
    @Operation(summary = "Get all shipments", description = "Employees see all. Customers see only their own.")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(Authentication authentication) {
        logger.debug("Fetching shipments for user: {}", authentication.getName());

        List<ShipmentResponse> shipments;

        if (isCustomer(authentication)) {
            // Customer: only their shipments
            Long customerId = getCustomerIdFromAuth(authentication);
            shipments = shipmentService.getShipmentsByCustomerId(customerId);
        } else {
            // Employee: all shipments
            shipments = shipmentService.getAllShipments();
        }

        return ResponseEntity.ok(shipments);
    }

    /**
     * Updates shipment status (Employee only).
     * Valid transitions: REGISTERED -> IN_TRANSIT -> DELIVERED (or CANCELLED)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update shipment status", description = "Updates shipment status (Employee only)")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {

        logger.info("Updating status of shipment ID: {} to: {}", id, request.getStatus());
        ShipmentResponse response = shipmentService.updateShipmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a shipment (Employee only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update shipment", description = "Updates shipment details (Employee only)")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {

        String employeeUsername = authentication.getName();
        logger.info("Updating shipment ID: {} by employee: {}", id, employeeUsername);

        ShipmentResponse response = shipmentService.updateShipment(id, request, employeeUsername);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a shipment (Employee only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete shipment", description = "Deletes a shipment (Employee only)")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        logger.info("Deleting shipment with ID: {}", id);
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if the authenticated user is a customer.
     */
    private boolean isCustomer(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    /**
     * Gets the customer ID from the authenticated user.
     */
    private Long getCustomerIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Customer not found for user: " + username));
        return customer.getId();
    }
}
