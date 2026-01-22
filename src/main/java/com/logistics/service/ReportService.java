package com.logistics.service;

import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for generating reports.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This interface only handles report generation.
 *   It doesn't handle CRUD operations - those are in dedicated services.
 * - Interface Segregation (ISP): Contains only report-related methods.
 *   CRUD operations remain in their respective service interfaces.
 * - Dependency Inversion (DIP): Controllers depend on this interface.
 *
 * Available Reports:
 * 1. All employees
 * 2. All customers
 * 3. All shipments (with role-based filtering)
 * 4. Shipments by specific employee
 * 5. Pending shipments (not delivered)
 * 6. Shipments sent by customer
 * 7. Shipments received by customer
 * 8. Revenue report for date range
 */
public interface ReportService {

    /**
     * Gets all employees report.
     *
     * @return list of all employees
     */
    List<EmployeeResponse> getAllEmployeesReport();

    /**
     * Gets all customers report.
     *
     * @return list of all customers
     */
    List<CustomerResponse> getAllCustomersReport();

    /**
     * Gets all shipments report.
     * Access control: Employees see all, Customers see only their own.
     *
     * @return list of all shipments
     */
    List<ShipmentResponse> getAllShipmentsReport();

    /**
     * Gets shipments registered by a specific employee.
     *
     * @param employeeId the employee ID
     * @return list of shipments registered by the employee
     */
    List<ShipmentResponse> getShipmentsByEmployeeReport(Long employeeId);

    /**
     * Gets all pending (non-delivered) shipments.
     * Includes REGISTERED, IN_TRANSIT, and any status that's not DELIVERED.
     *
     * @return list of pending shipments
     */
    List<ShipmentResponse> getPendingShipmentsReport();

    /**
     * Gets shipments sent by a specific customer.
     *
     * @param customerId the customer ID
     * @return list of shipments where customer is sender
     */
    List<ShipmentResponse> getShipmentsSentByCustomerReport(Long customerId);

    /**
     * Gets shipments received by a specific customer.
     *
     * @param customerId the customer ID
     * @return list of shipments where customer is recipient
     */
    List<ShipmentResponse> getShipmentsReceivedByCustomerReport(Long customerId);

    /**
     * Calculates total revenue for a date range.
     * IMPORTANT: Only counts DELIVERED shipments - cancelled or pending don't count as revenue.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate   end of the date range (inclusive)
     * @return revenue report with total and count
     */
    RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate);

    /**
     * Gets dashboard metrics for employee view.
     * Includes total shipments, pending, delivered, and total revenue.
     *
     * @return dashboard metrics
     */
    DashboardMetricsResponse getDashboardMetrics();

    /**
     * Gets dashboard metrics for a specific customer.
     * Includes sent count, received count, in-transit count, and total spent.
     *
     * @param customerId the customer ID
     * @return customer metrics
     */
    CustomerMetricsResponse getCustomerMetrics(Long customerId);
}
