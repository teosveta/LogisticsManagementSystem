package com.logistics.model.enums;

/**
 * Enumeration representing the lifecycle status of a shipment.
 *
 * SOLID Principle - Single Responsibility (SRP):
 * This enum has one responsibility: defining valid shipment states.
 * State transition logic is handled in the ShipmentService.
 *
 * Shipment lifecycle:
 * REGISTERED -> IN_TRANSIT -> DELIVERED
 *           \-> CANCELLED (can be cancelled before delivery)
 */
public enum ShipmentStatus {
    /**
     * Initial status when a shipment is created.
     * The shipment has been registered but not yet picked up for delivery.
     */
    REGISTERED,

    /**
     * Shipment is being transported to its destination.
     * Set when the courier picks up the shipment.
     */
    IN_TRANSIT,

    /**
     * Shipment has been successfully delivered to the recipient.
     * This is the final successful state.
     * Only DELIVERED shipments are counted in revenue reports.
     */
    DELIVERED,

    /**
     * Shipment has been cancelled.
     * Can occur before delivery for various reasons.
     * Cancelled shipments are not counted in revenue.
     */
    CANCELLED
}
