package com.logistics.dto.pricing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO for pricing configuration update requests.
 */
public class PricingConfigRequest {

    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be zero or positive")
    private BigDecimal basePrice;

    @NotNull(message = "Price per kg is required")
    @PositiveOrZero(message = "Price per kg must be zero or positive")
    private BigDecimal pricePerKg;

    @NotNull(message = "Address delivery fee is required")
    @PositiveOrZero(message = "Address delivery fee must be zero or positive")
    private BigDecimal addressDeliveryFee;

    public PricingConfigRequest() {
    }

    public PricingConfigRequest(BigDecimal basePrice, BigDecimal pricePerKg, BigDecimal addressDeliveryFee) {
        this.basePrice = basePrice;
        this.pricePerKg = pricePerKg;
        this.addressDeliveryFee = addressDeliveryFee;
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
}
