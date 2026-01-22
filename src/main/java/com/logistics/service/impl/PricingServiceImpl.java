package com.logistics.service.impl;

import com.logistics.exception.InvalidDataException;
import com.logistics.model.entity.PricingConfig;
import com.logistics.repository.PricingConfigRepository;
import com.logistics.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of PricingService that loads configuration from the DATABASE.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class ONLY handles pricing calculations.
 *   It doesn't persist data or validate shipments - those are other services' jobs.
 * - Open/Closed (OCP): Configuration is stored in database, so pricing
 *   can be changed without modifying code or redeploying.
 * - Dependency Inversion (DIP): Depends on PricingConfigRepository interface.
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

    private final PricingConfigRepository pricingConfigRepository;

    public PricingServiceImpl(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
        logger.info("PricingService initialized - will load pricing from database");
    }

    /**
     * Gets the currently active pricing configuration from the database.
     *
     * @return the active pricing configuration
     * @throws InvalidDataException if no active configuration exists
     */
    private PricingConfig getActiveConfig() {
        return pricingConfigRepository.findByActiveTrue()
                .orElseThrow(() -> new InvalidDataException(
                        "No active pricing configuration found. Please configure pricing in the database."));
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
    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
        PricingConfig config = getActiveConfig();

        // Step 1: Start with base price
        BigDecimal total = config.getBasePrice();

        // Step 2: Add weight-based cost (weight × price per kg)
        BigDecimal weightCost = weight.multiply(config.getPricePerKg());
        total = total.add(weightCost);

        // Step 3: Add delivery type fee (0 for office, addressDeliveryFee for address)
        if (!isOfficeDelivery) {
            total = total.add(config.getAddressDeliveryFee());
        }

        // Round to 2 decimal places for currency
        total = total.setScale(2, RoundingMode.HALF_UP);

        logger.debug("Price calculated: weight={}, isOfficeDelivery={}, total={}",
                weight, isOfficeDelivery, total);

        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBasePrice() {
        return getActiveConfig().getBasePrice();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPricePerKg() {
        return getActiveConfig().getPricePerKg();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAddressDeliveryFee() {
        return getActiveConfig().getAddressDeliveryFee();
    }

    /**
     * Gets the full active pricing configuration.
     * Used by PricingController for the admin UI.
     *
     * @return the active pricing configuration
     */
    @Transactional(readOnly = true)
    public PricingConfig getActivePricingConfig() {
        return getActiveConfig();
    }

    /**
     * Updates the pricing configuration.
     * Deactivates the old config and creates a new active one.
     *
     * @param basePrice         new base price
     * @param pricePerKg        new price per kg
     * @param addressDeliveryFee new address delivery fee
     * @return the new active pricing configuration
     */
    @Transactional
    public PricingConfig updatePricingConfig(BigDecimal basePrice, BigDecimal pricePerKg,
                                              BigDecimal addressDeliveryFee) {
        logger.info("Updating pricing config: basePrice={}, pricePerKg={}, addressDeliveryFee={}",
                basePrice, pricePerKg, addressDeliveryFee);

        // Validate inputs
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Base price must be non-negative");
        }
        if (pricePerKg == null || pricePerKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Price per kg must be non-negative");
        }
        if (addressDeliveryFee == null || addressDeliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Address delivery fee must be non-negative");
        }

        // Deactivate all existing configs
        pricingConfigRepository.deactivateAll();

        // Create new active config
        PricingConfig newConfig = new PricingConfig(basePrice, pricePerKg, addressDeliveryFee);
        PricingConfig saved = pricingConfigRepository.save(newConfig);

        logger.info("Pricing config updated successfully, new config ID: {}", saved.getId());
        return saved;
    }
}
