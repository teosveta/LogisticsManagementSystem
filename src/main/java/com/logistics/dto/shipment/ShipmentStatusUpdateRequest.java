package com.logistics.dto.shipment;

import com.logistics.model.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for shipment status update requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This DTO handles only status updates.
 *   It's separate from ShipmentRequest because updating status is a
 *   distinct operation from creating/updating shipment details.
 * - Interface Segregation (ISP): Instead of one large DTO that handles
 *   all shipment operations, we have focused DTOs for each operation type.
 */
public class ShipmentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    // Default constructor
    public ShipmentStatusUpdateRequest() {
    }

    public ShipmentStatusUpdateRequest(ShipmentStatus status) {
        this.status = status;
    }

    // Getters and Setters
    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
}
