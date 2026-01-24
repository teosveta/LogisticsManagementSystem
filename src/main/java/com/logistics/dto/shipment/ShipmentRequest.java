package com.logistics.dto.shipment;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for shipment creation requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only shipment input data transfer.
 *   Price calculation is NOT done here - that's delegated to PricingService
 *   following SRP (this DTO just carries data, not business logic).
 *
 * Validation Rules:
 * - Either deliveryAddress OR deliveryOfficeId must be provided (not both, not neither)
 * - This validation is done in the service layer, not here, as it's business logic
 *
 * Note: Weight uses BigDecimal for precision in pricing calculations.
 */
public class ShipmentRequest {

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    /**
     * Origin office ID where the shipment is being registered.
     * Optional - if not provided, uses the registering employee's office.
     */
    private Long originOfficeId;

    /**
     * Delivery address for address-based deliveries.
     * Mutually exclusive with deliveryOfficeId.
     */
    @Size(max = 255, message = "Delivery address must not exceed 255 characters")
    private String deliveryAddress;

    /**
     * Office ID for office-based deliveries.
     * Mutually exclusive with deliveryAddress.
     */
    private Long deliveryOfficeId;

    /**
     * Shipment weight in kilograms.
     * Uses BigDecimal for precise pricing calculations.
     * Must be between 0.01 and 10000 kg.
     */
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "10000.00", message = "Weight cannot exceed 10000 kg")
    private BigDecimal weight;

    // Default constructor
    public ShipmentRequest() {
    }

    // Getters and Setters
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getOriginOfficeId() {
        return originOfficeId;
    }

    public void setOriginOfficeId(Long originOfficeId) {
        this.originOfficeId = originOfficeId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Long getDeliveryOfficeId() {
        return deliveryOfficeId;
    }

    public void setDeliveryOfficeId(Long deliveryOfficeId) {
        this.deliveryOfficeId = deliveryOfficeId;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    /**
     * Checks if this request specifies office delivery.
     *
     * @return true if deliveryOfficeId is set
     */
    public boolean isOfficeDelivery() {
        return deliveryOfficeId != null;
    }

    /**
     * Checks if this request specifies address delivery.
     *
     * @return true if deliveryAddress is set and not blank
     */
    public boolean isAddressDelivery() {
        return deliveryAddress != null && !deliveryAddress.isBlank();
    }
}
