package com.logistics.controller;

import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.exception.UnauthorizedException;
import com.logistics.service.CustomerService;
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
 * Employees can manage all shipments. Customers can only view their own.
 */
@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "Shipment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentController.class);

    private final ShipmentService shipmentService;
    private final CustomerService customerService;

    public ShipmentController(ShipmentService shipmentService, CustomerService customerService) {
        this.shipmentService = shipmentService;
        this.customerService = customerService;
    }

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

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID", description = "Retrieves a shipment. Customers can only see their own shipments.")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable Long id,
            Authentication authentication) {

        ShipmentResponse shipment = shipmentService.getShipmentById(id);

        if (isCustomer(authentication)) {
            Long customerId = getCustomerIdFromAuth(authentication);
            if (!shipment.getSenderId().equals(customerId) &&
                    !shipment.getRecipientId().equals(customerId)) {
                throw new UnauthorizedException("You can only view shipments where you are sender or recipient");
            }
        }

        return ResponseEntity.ok(shipment);
    }

    @GetMapping
    @Operation(summary = "Get all shipments", description = "Employees see all. Customers see only their own.")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(Authentication authentication) {
        logger.debug("Fetching shipments for user: {}", authentication.getName());

        List<ShipmentResponse> shipments;
        if (isCustomer(authentication)) {
            Long customerId = getCustomerIdFromAuth(authentication);
            shipments = shipmentService.getShipmentsByCustomerId(customerId);
        } else {
            shipments = shipmentService.getAllShipments();
        }

        return ResponseEntity.ok(shipments);
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete shipment", description = "Deletes a shipment (Employee only)")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        logger.info("Deleting shipment with ID: {}", id);
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isCustomer(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private Long getCustomerIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        try {
            return customerService.getCustomerIdByUsername(username);
        } catch (Exception e) {
            throw new UnauthorizedException("Customer not found for user: " + username);
        }
    }
}
