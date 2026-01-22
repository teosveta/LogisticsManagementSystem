package com.logistics.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing pricing configuration stored in the database.
 *
 * Only ONE record should be active at a time (active = true).
 * This allows changing pricing without code deployment.
 *
 * Pricing Formula:
 * Total = basePrice + (weight × pricePerKg) + addressDeliveryFee (if address delivery)
 */
@Entity
@Table(name = "pricing_config")
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Base price applied to all shipments.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * Price per kilogram of shipment weight.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    /**
     * Additional fee for address delivery (office delivery = 0).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal addressDeliveryFee;

    /**
     * Only one pricing config should be active at a time.
     * When creating a new config, deactivate the old one first.
     */
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Default constructor
    public PricingConfig() {
    }

    // Constructor with all pricing fields
    public PricingConfig(BigDecimal basePrice, BigDecimal pricePerKg, BigDecimal addressDeliveryFee) {
        this.basePrice = basePrice;
        this.pricePerKg = pricePerKg;
        this.addressDeliveryFee = addressDeliveryFee;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(BigDecimal pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public BigDecimal getAddressDeliveryFee() {
        return addressDeliveryFee;
    }

    public void setAddressDeliveryFee(BigDecimal addressDeliveryFee) {
        this.addressDeliveryFee = addressDeliveryFee;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
