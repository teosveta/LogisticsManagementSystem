package com.logistics.controller;

import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.exception.UnauthorizedException;
import com.logistics.model.entity.Customer;
import com.logistics.repository.CustomerRepository;
import com.logistics.service.ReportService;
import com.logistics.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for report endpoints.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only report-related HTTP endpoints.
 * - Dependency Inversion (DIP): Depends on ReportService interface.
 *
 * Access Control:
 * - EMPLOYEE: Can access all reports
 * - CUSTOMER: Can only access their own sent/received shipment reports
 *
 * Report Endpoints:
 * - GET /api/reports/employees - All employees (Employee only)
 * - GET /api/reports/customers - All customers (Employee only)
 * - GET /api/reports/shipments - All shipments (Employee) / Own shipments (Customer)
 * - GET /api/reports/shipments/employee/{id} - Shipments by employee (Employee only)
 * - GET /api/reports/shipments/pending - Pending shipments (Employee only)
 * - GET /api/reports/shipments/customer/{id}/sent - Sent by customer
 * - GET /api/reports/shipments/customer/{id}/received - Received by customer
 * - GET /api/reports/revenue?startDate=...&endDate=... - Revenue report (Employee only)
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report generation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final ShipmentService shipmentService;
    private final CustomerRepository customerRepository;

    public ReportController(ReportService reportService,
                            ShipmentService shipmentService,
                            CustomerRepository customerRepository) {
        this.reportService = reportService;
        this.shipmentService = shipmentService;
        this.customerRepository = customerRepository;
    }

    /**
     * Gets all employees report (Employee only).
     */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "All employees report", description = "Lists all employees (Employee only)")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployeesReport() {
        logger.debug("Generating all employees report");
        List<EmployeeResponse> employees = reportService.getAllEmployeesReport();
        return ResponseEntity.ok(employees);
    }

    /**
     * Gets all customers report (Employee only).
     */
    @GetMapping("/customers")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "All customers report", description = "Lists all customers (Employee only)")
    public ResponseEntity<List<CustomerResponse>> getAllCustomersReport() {
        logger.debug("Generating all customers report");
        List<CustomerResponse> customers = reportService.getAllCustomersReport();
        return ResponseEntity.ok(customers);
    }

    /**
     * Gets all shipments report.
     * Employee sees ALL shipments.
     * Customer sees only their own shipments.
     */
    @GetMapping("/shipments")
    @Operation(summary = "All shipments report", description = "Employees see all. Customers see only their own.")
    public ResponseEntity<List<ShipmentResponse>> getAllShipmentsReport(Authentication authentication) {
        logger.debug("Generating shipments report for user: {}", authentication.getName());

        List<ShipmentResponse> shipments;

        if (isCustomer(authentication)) {
            // Customer: only their shipments
            Long customerId = getCustomerIdFromAuth(authentication);
            shipments = shipmentService.getShipmentsByCustomerId(customerId);
        } else {
            // Employee: all shipments
            shipments = reportService.getAllShipmentsReport();
        }

        return ResponseEntity.ok(shipments);
    }

    /**
     * Gets shipments registered by a specific employee (Employee only).
     */
    @GetMapping("/shipments/employee/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Shipments by employee", description = "Lists shipments registered by an employee (Employee only)")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByEmployee(@PathVariable Long employeeId) {
        logger.debug("Generating shipments report for employee ID: {}", employeeId);
        List<ShipmentResponse> shipments = reportService.getShipmentsByEmployeeReport(employeeId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Gets pending (non-delivered) shipments (Employee only).
     */
    @GetMapping("/shipments/pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Pending shipments", description = "Lists all non-delivered shipments (Employee only)")
    public ResponseEntity<List<ShipmentResponse>> getPendingShipmentsReport() {
        logger.debug("Generating pending shipments report");
        List<ShipmentResponse> shipments = reportService.getPendingShipmentsReport();
        return ResponseEntity.ok(shipments);
    }

    /**
     * Gets shipments sent by a specific customer.
     * Employee can view any customer's sent shipments.
     * Customer can only view their own sent shipments.
     */
    @GetMapping("/shipments/customer/{customerId}/sent")
    @Operation(summary = "Shipments sent by customer", description = "Lists shipments sent by a customer")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsSentByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        logger.debug("Generating sent shipments report for customer ID: {}", customerId);

        // If customer, verify they're accessing their own data
        if (isCustomer(authentication)) {
            Long authCustomerId = getCustomerIdFromAuth(authentication);
            if (!customerId.equals(authCustomerId)) {
                throw new UnauthorizedException("You can only view your own sent shipments");
            }
        }

        List<ShipmentResponse> shipments = reportService.getShipmentsSentByCustomerReport(customerId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Gets shipments received by a specific customer.
     * Employee can view any customer's received shipments.
     * Customer can only view their own received shipments.
     */
    @GetMapping("/shipments/customer/{customerId}/received")
    @Operation(summary = "Shipments received by customer", description = "Lists shipments received by a customer")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsReceivedByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        logger.debug("Generating received shipments report for customer ID: {}", customerId);

        // If customer, verify they're accessing their own data
        if (isCustomer(authentication)) {
            Long authCustomerId = getCustomerIdFromAuth(authentication);
            if (!customerId.equals(authCustomerId)) {
                throw new UnauthorizedException("You can only view your own received shipments");
            }
        }

        List<ShipmentResponse> shipments = reportService.getShipmentsReceivedByCustomerReport(customerId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Gets revenue report for a date range (Employee only).
     * Only counts DELIVERED shipments as revenue.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Revenue report", description = "Calculates total revenue from DELIVERED shipments (Employee only)")
    public ResponseEntity<RevenueResponse> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Generating revenue report from {} to {}", startDate, endDate);
        RevenueResponse revenue = reportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    /**
     * Checks if the authenticated user is a customer.
     */
    private boolean isCustomer(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    /**
     * Gets the customer ID from the authenticated user.
     */
    private Long getCustomerIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Customer not found for user: " + username));
        return customer.getId();
    }

    /**
     * Gets dashboard metrics for employee view.
     * Returns total shipments, pending, delivered, and total revenue in a single call.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Dashboard metrics", description = "Returns aggregated dashboard metrics (Employee only)")
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        logger.debug("Getting dashboard metrics");
        DashboardMetricsResponse metrics = reportService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Gets dashboard metrics for customer view.
     * Returns sent, received, in-transit counts and total spent.
     * Customers can only access their own metrics.
     */
    @GetMapping("/customer-metrics")
    @Operation(summary = "Customer metrics", description = "Returns customer dashboard metrics")
    public ResponseEntity<CustomerMetricsResponse> getCustomerMetrics(Authentication authentication) {
        logger.debug("Getting customer metrics for user: {}", authentication.getName());

        Long customerId;
        if (isCustomer(authentication)) {
            customerId = getCustomerIdFromAuth(authentication);
        } else {
            throw new UnauthorizedException("Only customers can access customer metrics");
        }

        CustomerMetricsResponse metrics = reportService.getCustomerMetrics(customerId);
        return ResponseEntity.ok(metrics);
    }
}
