package com.logistics.repository;

import com.logistics.model.entity.Shipment;
import com.logistics.model.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Shipment entity database operations.
 *
 * SOLID Principles Applied:
 * - Interface Segregation (ISP): Contains only shipment-specific query methods.
 * - Dependency Inversion (DIP): Services depend on this abstraction.
 * - Single Responsibility (SRP): This repository only handles data access for shipments.
 *   Revenue calculations and business logic are in dedicated services.
 *
 * Spring Data JPA provides the implementation automatically.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Finds all shipments registered by a specific employee.
     * Used for the /reports/shipments/employee/{id} endpoint.
     *
     * @param employeeId the registering employee's ID
     * @return list of shipments registered by the employee
     */
    List<Shipment> findByRegisteredById(Long employeeId);

    /**
     * Finds all shipments sent by a specific customer.
     * Used for the /reports/shipments/customer/{id}/sent endpoint.
     *
     * @param customerId the sender's customer ID
     * @return list of shipments sent by the customer
     */
    List<Shipment> findBySenderId(Long customerId);

    /**
     * Finds all shipments received by a specific customer.
     * Used for the /reports/shipments/customer/{id}/received endpoint.
     *
     * @param customerId the recipient's customer ID
     * @return list of shipments received by the customer
     */
    List<Shipment> findByRecipientId(Long customerId);

    /**
     * Finds all shipments where a customer is either sender or recipient.
     * Used for customer access control - customers can only see their own shipments.
     *
     * @param senderId    the sender's customer ID
     * @param recipientId the recipient's customer ID (same as senderId for single customer lookup)
     * @return list of shipments associated with the customer
     */
    @Query("SELECT s FROM Shipment s WHERE s.sender.id = :senderId OR s.recipient.id = :recipientId")
    List<Shipment> findBySenderIdOrRecipientId(@Param("senderId") Long senderId,
                                                @Param("recipientId") Long recipientId);

    /**
     * Finds all shipments with a specific status.
     *
     * @param status the shipment status to filter by
     * @return list of shipments with the specified status
     */
    List<Shipment> findByStatus(ShipmentStatus status);

    /**
     * Finds all shipments that are NOT delivered (pending shipments).
     * Used for the /reports/shipments/pending endpoint.
     *
     * @return list of non-delivered shipments
     */
    @Query("SELECT s FROM Shipment s WHERE s.status != 'DELIVERED'")
    List<Shipment> findAllPendingShipments();

    /**
     * Finds all delivered shipments within a date range.
     * Used for revenue calculation.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate   end of the date range (inclusive)
     * @return list of delivered shipments in the date range
     */
    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    List<Shipment> findDeliveredShipmentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculates total revenue (sum of prices) for delivered shipments in a date range.
     * Used for the /reports/revenue endpoint.
     * Only counts DELIVERED shipments as actual revenue.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate   end of the date range (inclusive)
     * @return total revenue as BigDecimal, or null if no shipments found
     */
    @Query("SELECT SUM(s.price) FROM Shipment s WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    BigDecimal calculateRevenueBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds all shipments delivered to a specific office.
     *
     * @param officeId the delivery office's ID
     * @return list of shipments delivered to the office
     */
    List<Shipment> findByDeliveryOfficeId(Long officeId);

    /**
     * Counts shipments by status.
     * Useful for dashboard statistics.
     *
     * @param status the shipment status to count
     * @return count of shipments with the specified status
     */
    long countByStatus(ShipmentStatus status);

    /**
     * Calculates total revenue from all shipments (sum of all prices).
     * Used for dashboard metrics.
     *
     * @return total revenue as BigDecimal, or null if no shipments found
     */
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s")
    BigDecimal calculateTotalRevenue();

    /**
     * Counts shipments that are in transit (REGISTERED or IN_TRANSIT).
     * Used for dashboard metrics.
     *
     * @return count of in-transit shipments
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'REGISTERED' OR s.status = 'IN_TRANSIT'")
    long countInTransitShipments();

    /**
     * Counts shipments sent by a customer that are in transit.
     *
     * @param senderId the sender's customer ID
     * @return count of in-transit shipments sent by the customer
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.sender.id = :senderId AND (s.status = 'REGISTERED' OR s.status = 'IN_TRANSIT')")
    long countInTransitBySenderId(@Param("senderId") Long senderId);

    /**
     * Counts shipments received by a customer that are in transit.
     *
     * @param recipientId the recipient's customer ID
     * @return count of in-transit shipments for the recipient
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.recipient.id = :recipientId AND (s.status = 'REGISTERED' OR s.status = 'IN_TRANSIT')")
    long countInTransitByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * Counts delivered shipments received by a customer.
     *
     * @param recipientId the recipient's customer ID
     * @return count of delivered shipments for the recipient
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.recipient.id = :recipientId AND s.status = 'DELIVERED'")
    long countDeliveredByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * Calculates total spent (sum of prices) for shipments sent by a customer.
     *
     * @param senderId the sender's customer ID
     * @return total spent as BigDecimal
     */
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.sender.id = :senderId")
    BigDecimal calculateTotalSpentBySenderId(@Param("senderId") Long senderId);

    /**
     * Counts shipments sent by a customer.
     *
     * @param senderId the sender's customer ID
     * @return count of shipments sent by the customer
     */
    long countBySenderId(Long senderId);
}
