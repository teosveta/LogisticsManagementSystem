package com.logistics.service.impl;

import com.logistics.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of PricingService that loads configuration from application.properties.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class ONLY handles pricing calculations.
 *   It doesn't persist data or validate shipments - those are other services' jobs.
 * - Open/Closed (OCP): Configuration is external (application.properties), so pricing
 *   can be changed without modifying code. New pricing strategies would be NEW classes.
 * - Dependency Inversion (DIP): Configuration values are injected via @Value,
 *   decoupling from hardcoded values.
 *
 * Pricing Formula:
 * Total = Base Price + (Weight × Price per kg) + Delivery Type Fee
 *
 * Example calculations:
 * - 5kg to office:  5.00 + (5 × 2.00) + 0.00  = 15.00
 * - 5kg to address: 5.00 + (5 × 2.00) + 10.00 = 25.00
 */
@Service
public class PricingServiceImpl implements PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    /**
     * Base price for all shipments.
     * Loaded from pricing.base-price in application.properties.
     * Default: 5.00
     */
    private final BigDecimal basePrice;

    /**
     * Price per kilogram of weight.
     * Loaded from pricing.price-per-kg in application.properties.
     * Default: 2.00
     */
    private final BigDecimal pricePerKg;

    /**
     * Additional fee for address delivery.
     * Office delivery has no fee (0.00).
     * Loaded from pricing.address-delivery-fee in application.properties.
     * Default: 10.00
     */
    private final BigDecimal addressDeliveryFee;

    /**
     * Constructor that loads pricing configuration from application.properties.
     * Uses @Value for Dependency Inversion - values come from external config.
     *
     * @param basePrice          base price from config (default 5.00)
     * @param pricePerKg         price per kg from config (default 2.00)
     * @param addressDeliveryFee address fee from config (default 10.00)
     */
    public PricingServiceImpl(
            @Value("${pricing.base-price:5.00}") BigDecimal basePrice,
            @Value("${pricing.price-per-kg:2.00}") BigDecimal pricePerKg,
            @Value("${pricing.address-delivery-fee:10.00}") BigDecimal addressDeliveryFee) {

        this.basePrice = basePrice;
        this.pricePerKg = pricePerKg;
        this.addressDeliveryFee = addressDeliveryFee;

        logger.info("PricingService initialized with: basePrice={}, pricePerKg={}, addressDeliveryFee={}",
                basePrice, pricePerKg, addressDeliveryFee);
    }

    /**
     * {@inheritDoc}
     *
     * Calculates total price using the formula:
     * Total = Base Price + (Weight × Price per kg) + Delivery Type Fee
     *
     * Uses BigDecimal arithmetic with HALF_UP rounding for monetary precision.
     */
    @Override
    public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
        // Step 1: Start with base price
        BigDecimal total = basePrice;

        // Step 2: Add weight-based cost (weight × price per kg)
        BigDecimal weightCost = weight.multiply(pricePerKg);
        total = total.add(weightCost);

        // Step 3: Add delivery type fee (0 for office, addressDeliveryFee for address)
        if (!isOfficeDelivery) {
            total = total.add(addressDeliveryFee);
        }

        // Round to 2 decimal places for currency
        total = total.setScale(2, RoundingMode.HALF_UP);

        logger.debug("Price calculated: weight={}, isOfficeDelivery={}, total={}",
                weight, isOfficeDelivery, total);

        return total;
    }

    @Override
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    @Override
    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }

    @Override
    public BigDecimal getAddressDeliveryFee() {
        return addressDeliveryFee;
    }
}
