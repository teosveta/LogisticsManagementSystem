package com.logistics.model.entity;

import com.logistics.model.enums.ShipmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a shipment in the logistics system.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This entity only manages shipment data.
 *   Price calculation is delegated to PricingService (not embedded here).
 *   Status transitions are validated in ShipmentService.
 * - Open/Closed (OCP): New shipment types or pricing strategies can be
 *   added through new services without modifying this entity.
 *
 * A Shipment:
 * - Has a sender (Customer) and recipient (Customer)
 * - Is registered by an Employee
 * - Has either a delivery address OR a delivery office (not both)
 * - Has a weight (BigDecimal, in kg)
 * - Has a calculated price (BigDecimal)
 * - Has a status tracking its lifecycle
 *
 * IMPORTANT: Both weight and price use BigDecimal for precision.
 * NEVER use double or float for monetary values!
 */
@Entity
@Table(name = "shipments")
public class Shipment {

    /**
     * Unique identifier for the shipment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-one relationship: the customer sending the shipment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Sender is required")
    private Customer sender;

    /**
     * Many-to-one relationship: the customer receiving the shipment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @NotNull(message = "Recipient is required")
    private Customer recipient;

    /**
     * Many-to-one relationship: the employee who registered the shipment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id", nullable = false)
    @NotNull(message = "Registering employee is required")
    private Employee registeredBy;

    /**
     * Delivery address for address-based deliveries.
     * Mutually exclusive with deliveryOffice.
     * If set, an additional address delivery fee applies.
     */
    @Column(name = "delivery_address")
    private String deliveryAddress;

    /**
     * Many-to-one relationship: office for office-based deliveries.
     * Mutually exclusive with deliveryAddress.
     * Office delivery has no additional fee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_office_id")
    private Office deliveryOffice;

    /**
     * Weight of the shipment in kilograms.
     * Uses BigDecimal for precision in pricing calculations.
     */
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    /**
     * Calculated price for the shipment.
     * Uses BigDecimal for monetary precision.
     * Formula: Base Price + (Weight × Price per kg) + Delivery Type Fee
     */
    @NotNull(message = "Price is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Current status of the shipment.
     * Tracks the shipment through its lifecycle.
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    /**
     * Timestamp when the shipment was registered.
     */
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    /**
     * Timestamp when the shipment was delivered.
     * Only set when status changes to DELIVERED.
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * Timestamp of last update to the shipment.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Default status is REGISTERED when first created
        if (this.status == null) {
            this.status = ShipmentStatus.REGISTERED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public Shipment() {
    }

    /**
     * Constructs a Shipment with required fields.
     *
     * @param sender       the sending customer
     * @param recipient    the receiving customer
     * @param registeredBy the employee registering the shipment
     * @param weight       the weight in kg (BigDecimal)
     * @param price        the calculated price (BigDecimal)
     */
    public Shipment(Customer sender, Customer recipient, Employee registeredBy,
                    BigDecimal weight, BigDecimal price) {
        this.sender = sender;
        this.recipient = recipient;
        this.registeredBy = registeredBy;
        this.weight = weight;
        this.price = price;
        this.status = ShipmentStatus.REGISTERED;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getSender() {
        return sender;
    }

    public void setSender(Customer sender) {
        this.sender = sender;
    }

    public Customer getRecipient() {
        return recipient;
    }

    public void setRecipient(Customer recipient) {
        this.recipient = recipient;
    }

    public Employee getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(Employee registeredBy) {
        this.registeredBy = registeredBy;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Office getDeliveryOffice() {
        return deliveryOffice;
    }

    public void setDeliveryOffice(Office deliveryOffice) {
        this.deliveryOffice = deliveryOffice;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Checks if this shipment is delivered to an office (vs. an address).
     *
     * @return true if delivery destination is an office
     */
    public boolean isOfficeDelivery() {
        return deliveryOffice != null;
    }

    /**
     * Checks if this shipment is delivered to an address (vs. an office).
     *
     * @return true if delivery destination is an address
     */
    public boolean isAddressDelivery() {
        return deliveryAddress != null && !deliveryAddress.isBlank();
    }

    /**
     * Gets the delivery destination as a string (either address or office name).
     *
     * @return the delivery destination description
     */
    public String getDeliveryDestination() {
        if (isOfficeDelivery()) {
            return "Office: " + deliveryOffice.getName() + " - " + deliveryOffice.getFullAddress();
        } else if (isAddressDelivery()) {
            return "Address: " + deliveryAddress;
        }
        return "Unknown";
    }

    /**
     * Checks if the shipment has been delivered.
     *
     * @return true if status is DELIVERED
     */
    public boolean isDelivered() {
        return ShipmentStatus.DELIVERED.equals(this.status);
    }

    /**
     * Checks if the shipment has been cancelled.
     *
     * @return true if status is CANCELLED
     */
    public boolean isCancelled() {
        return ShipmentStatus.CANCELLED.equals(this.status);
    }
}
