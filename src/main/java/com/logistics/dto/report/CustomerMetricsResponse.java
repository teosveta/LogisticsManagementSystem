package com.logistics.dto.report;

import java.math.BigDecimal;

/**
 * DTO for customer dashboard metrics response.
 *
 * Contains aggregated metrics for the customer dashboard:
 * - Total sent shipments
 * - Total received (delivered) shipments
 * - In-transit shipments count
 * - Total spent on shipments
 */
public class CustomerMetricsResponse {

    private long totalSent;
    private long totalReceived;
    private long inTransit;
    private BigDecimal totalSpent;

    public CustomerMetricsResponse() {
    }

    public CustomerMetricsResponse(long totalSent, long totalReceived,
                                   long inTransit, BigDecimal totalSpent) {
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
        this.inTransit = inTransit;
        this.totalSpent = totalSpent;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(long totalSent) {
        this.totalSent = totalSent;
    }

    public long getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(long totalReceived) {
        this.totalReceived = totalReceived;
    }

    public long getInTransit() {
        return inTransit;
    }

    public void setInTransit(long inTransit) {
        this.inTransit = inTransit;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
}
