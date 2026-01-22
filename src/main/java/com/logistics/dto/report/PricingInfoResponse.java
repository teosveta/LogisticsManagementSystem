package com.logistics.dto.report;

import java.math.BigDecimal;

/**
 * DTO for pricing configuration response.
 *
 * Returns the current pricing configuration from the backend.
 * Frontend should use this instead of hardcoding pricing values.
 */
public class PricingInfoResponse {

    private BigDecimal basePrice;
    private BigDecimal pricePerKg;
    private BigDecimal addressDeliveryFee;

    public PricingInfoResponse() {
    }

    public PricingInfoResponse(BigDecimal basePrice, BigDecimal pricePerKg,
                               BigDecimal addressDeliveryFee) {
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
