package com.logistics.dto.shipment;

import com.logistics.model.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for shipment status update requests.
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
