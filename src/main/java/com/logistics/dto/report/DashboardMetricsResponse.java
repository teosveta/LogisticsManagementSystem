package com.logistics.dto.report;

import java.math.BigDecimal;

/**
 * DTO for employee dashboard metrics response.
 *
 * Contains aggregated metrics for the employee dashboard:
 * - Total shipments count
 * - Pending shipments count (REGISTERED or IN_TRANSIT)
 * - Delivered shipments count
 * - Total revenue from all shipments
 */
public class DashboardMetricsResponse {

    private long totalShipments;
    private long pendingShipments;
    private long deliveredShipments;
    private BigDecimal totalRevenue;

    public DashboardMetricsResponse() {
    }

    public DashboardMetricsResponse(long totalShipments, long pendingShipments,
                                    long deliveredShipments, BigDecimal totalRevenue) {
        this.totalShipments = totalShipments;
        this.pendingShipments = pendingShipments;
        this.deliveredShipments = deliveredShipments;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalShipments() {
        return totalShipments;
    }

    public void setTotalShipments(long totalShipments) {
        this.totalShipments = totalShipments;
    }

    public long getPendingShipments() {
        return pendingShipments;
    }

    public void setPendingShipments(long pendingShipments) {
        this.pendingShipments = pendingShipments;
    }

    public long getDeliveredShipments() {
        return deliveredShipments;
    }

    public void setDeliveredShipments(long deliveredShipments) {
        this.deliveredShipments = deliveredShipments;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
