package com.logistics.controller;

import com.logistics.dto.pricing.PricingConfigRequest;
import com.logistics.dto.pricing.PricingConfigResponse;
import com.logistics.dto.report.PricingInfoResponse;
import com.logistics.model.entity.PricingConfig;
import com.logistics.service.PricingService;
import com.logistics.service.impl.PricingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for pricing configuration endpoints.
 *
 * Provides:
 * - Public endpoint to get current pricing (for shipment forms)
 * - Admin endpoints to view and update pricing configuration (Employee only)
 */
@RestController
@RequestMapping("/api/pricing")
@Tag(name = "Pricing", description = "Pricing configuration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);

    private final PricingServiceImpl pricingService;

    public PricingController(PricingServiceImpl pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Gets the current pricing information.
     * Used by shipment forms to display pricing preview.
     * Accessible by all authenticated users.
     */
    @GetMapping
    @Operation(summary = "Get pricing info", description = "Returns current pricing configuration values")
    public ResponseEntity<PricingInfoResponse> getPricingInfo() {
        logger.debug("Getting pricing info");

        PricingInfoResponse response = new PricingInfoResponse(
                pricingService.getBasePrice(),
                pricingService.getPricePerKg(),
                pricingService.getAddressDeliveryFee()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Gets the full pricing configuration with metadata.
     * Employee only - for admin UI.
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get pricing config", description = "Returns full pricing configuration (Employee only)")
    public ResponseEntity<PricingConfigResponse> getPricingConfig() {
        logger.debug("Getting full pricing config");

        PricingConfig config = pricingService.getActivePricingConfig();

        PricingConfigResponse response = new PricingConfigResponse(
                config.getId(),
                config.getBasePrice(),
                config.getPricePerKg(),
                config.getAddressDeliveryFee(),
                config.getActive(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the pricing configuration.
     * Employee only - creates a new active config and deactivates the old one.
     */
    @PutMapping("/config")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update pricing config", description = "Updates pricing configuration (Employee only)")
    public ResponseEntity<PricingConfigResponse> updatePricingConfig(
            @Valid @RequestBody PricingConfigRequest request) {

        logger.info("Updating pricing config: basePrice={}, pricePerKg={}, addressDeliveryFee={}",
                request.getBasePrice(), request.getPricePerKg(), request.getAddressDeliveryFee());

        PricingConfig updated = pricingService.updatePricingConfig(
                request.getBasePrice(),
                request.getPricePerKg(),
                request.getAddressDeliveryFee()
        );

        PricingConfigResponse response = new PricingConfigResponse(
                updated.getId(),
                updated.getBasePrice(),
                updated.getPricePerKg(),
                updated.getAddressDeliveryFee(),
                updated.getActive(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
