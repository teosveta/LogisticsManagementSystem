package com.logistics.dto.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for pricing configuration response.
 */
public class PricingConfigResponse {

    private Long id;
    private BigDecimal basePrice;
    private BigDecimal pricePerKg;
    private BigDecimal addressDeliveryFee;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PricingConfigResponse() {
    }

    public PricingConfigResponse(Long id, BigDecimal basePrice, BigDecimal pricePerKg,
                                  BigDecimal addressDeliveryFee, Boolean active,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.basePrice = basePrice;
        this.pricePerKg = pricePerKg;
        this.addressDeliveryFee = addressDeliveryFee;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
