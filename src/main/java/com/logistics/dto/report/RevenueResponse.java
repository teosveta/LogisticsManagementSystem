package com.logistics.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for revenue report responses.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This DTO handles only revenue report data.
 *
 * Note: Revenue uses BigDecimal for monetary precision.
 * Only DELIVERED shipments are counted in revenue calculations.
 */
public class RevenueResponse {

    /**
     * Start date of the report period.
     */
    private LocalDate startDate;

    /**
     * End date of the report period.
     */
    private LocalDate endDate;

    /**
     * Total revenue from DELIVERED shipments in the period.
     * Uses BigDecimal for monetary precision.
     */
    private BigDecimal totalRevenue;

    /**
     * Number of delivered shipments in the period.
     */
    private Long deliveredShipmentsCount;

    // Default constructor
    public RevenueResponse() {
    }

    public RevenueResponse(LocalDate startDate, LocalDate endDate, BigDecimal totalRevenue, Long deliveredShipmentsCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.deliveredShipmentsCount = deliveredShipmentsCount;
    }

    // Getters and Setters
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getDeliveredShipmentsCount() {
        return deliveredShipmentsCount;
    }

    public void setDeliveredShipmentsCount(Long deliveredShipmentsCount) {
        this.deliveredShipmentsCount = deliveredShipmentsCount;
    }
}
