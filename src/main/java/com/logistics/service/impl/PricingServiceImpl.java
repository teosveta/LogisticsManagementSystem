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
 * Calculates shipment prices using database-stored configuration.
 * Formula: BasePrice + (Weight × PricePerKg) + DeliveryFee (if address delivery)
 */
@Service
public class PricingServiceImpl implements PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    private final PricingConfigRepository pricingConfigRepository;

    public PricingServiceImpl(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
        logger.info("PricingService initialized - will load pricing from database");
    }

    private PricingConfig getActiveConfig() {
        return pricingConfigRepository.findByActiveTrue()
                .orElseThrow(() -> new InvalidDataException(
                        "No active pricing configuration found. Please configure pricing in the database."));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
        PricingConfig config = getActiveConfig();

        BigDecimal total = config.getBasePrice();
        total = total.add(weight.multiply(config.getPricePerKg()));

        if (!isOfficeDelivery) {
            total = total.add(config.getAddressDeliveryFee());
        }

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

    @Transactional(readOnly = true)
    public PricingConfig getActivePricingConfig() {
        return getActiveConfig();
    }

    @Transactional
    public PricingConfig updatePricingConfig(BigDecimal basePrice, BigDecimal pricePerKg,
                                              BigDecimal addressDeliveryFee) {
        logger.info("Updating pricing config: basePrice={}, pricePerKg={}, addressDeliveryFee={}",
                basePrice, pricePerKg, addressDeliveryFee);

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Base price must be non-negative");
        }
        if (pricePerKg == null || pricePerKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Price per kg must be non-negative");
        }
        if (addressDeliveryFee == null || addressDeliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Address delivery fee must be non-negative");
        }

        pricingConfigRepository.deactivateAll();

        PricingConfig newConfig = new PricingConfig(basePrice, pricePerKg, addressDeliveryFee);
        PricingConfig saved = pricingConfigRepository.save(newConfig);

        logger.info("Pricing config updated successfully, new config ID: {}", saved.getId());
        return saved;
    }
}
