package com.logistics.service;

import java.math.BigDecimal;

/**
 * Service interface for shipment pricing calculations.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This interface has ONE responsibility - pricing.
 *   It doesn't handle shipment creation, validation, or persistence.
 * - Interface Segregation (ISP): Small, focused interface with only pricing methods.
 *   Clients only need to know about pricing, not other shipment operations.
 * - Dependency Inversion (DIP): ShipmentService depends on this interface, not on
 *   a concrete implementation. This allows swapping pricing strategies easily.
 * - Open/Closed (OCP): New pricing strategies (e.g., volume-based, member discounts)
 *   can be implemented without modifying existing code - just create a new implementation.
 *
 * All monetary values use BigDecimal for precision.
 */
public interface PricingService {

    /**
     * Calculates the total price for a shipment.
     *
     * Formula: Base Price + (Weight × Price per kg) + Delivery Type Fee
     *
     * @param weight          the shipment weight in kilograms (BigDecimal for precision)
     * @param isOfficeDelivery true if delivered to office (no fee), false for address delivery
     * @return the calculated price as BigDecimal
     */
    BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery);

    /**
     * Gets the current base price for all shipments.
     *
     * @return the base price as BigDecimal
     */
    BigDecimal getBasePrice();

    /**
     * Gets the current price per kilogram.
     *
     * @return the price per kg as BigDecimal
     */
    BigDecimal getPricePerKg();

    /**
     * Gets the additional fee for address delivery.
     * Office delivery has no additional fee.
     *
     * @return the address delivery fee as BigDecimal
     */
    BigDecimal getAddressDeliveryFee();
}
