package com.logistics.controller;

import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.exception.UnauthorizedException;
import com.logistics.service.CustomerService;
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
 * Employees can access all reports. Customers can only view their own shipments.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report generation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final ShipmentService shipmentService;
    private final CustomerService customerService;

    public ReportController(ReportService reportService,
                            ShipmentService shipmentService,
                            CustomerService customerService) {
        this.reportService = reportService;
        this.shipmentService = shipmentService;
        this.customerService = customerService;
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "All employees report", description = "Lists all employees (Employee only)")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployeesReport() {
        logger.debug("Generating all employees report");
        List<EmployeeResponse> employees = reportService.getAllEmployeesReport();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "All customers report", description = "Lists all customers (Employee only)")
    public ResponseEntity<List<CustomerResponse>> getAllCustomersReport() {
        logger.debug("Generating all customers report");
        List<CustomerResponse> customers = reportService.getAllCustomersReport();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/shipments")
    @Operation(summary = "All shipments report", description = "Employees see all. Customers see only their own.")
    public ResponseEntity<List<ShipmentResponse>> getAllShipmentsReport(Authentication authentication) {
        logger.debug("Generating shipments report for user: {}", authentication.getName());

        List<ShipmentResponse> shipments;
        if (isCustomer(authentication)) {
            Long customerId = getCustomerIdFromAuth(authentication);
            shipments = shipmentService.getShipmentsByCustomerId(customerId);
        } else {
            shipments = reportService.getAllShipmentsReport();
        }

        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/shipments/employee/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Shipments by employee", description = "Lists shipments registered by an employee (Employee only)")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByEmployee(@PathVariable Long employeeId) {
        logger.debug("Generating shipments report for employee ID: {}", employeeId);
        List<ShipmentResponse> shipments = reportService.getShipmentsByEmployeeReport(employeeId);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/shipments/pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Pending shipments", description = "Lists all non-delivered shipments (Employee only)")
    public ResponseEntity<List<ShipmentResponse>> getPendingShipmentsReport() {
        logger.debug("Generating pending shipments report");
        List<ShipmentResponse> shipments = reportService.getPendingShipmentsReport();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/shipments/customer/{customerId}/sent")
    @Operation(summary = "Shipments sent by customer", description = "Lists shipments sent by a customer")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsSentByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        logger.debug("Generating sent shipments report for customer ID: {}", customerId);

        if (isCustomer(authentication)) {
            Long authCustomerId = getCustomerIdFromAuth(authentication);
            if (!customerId.equals(authCustomerId)) {
                throw new UnauthorizedException("You can only view your own sent shipments");
            }
        }

        List<ShipmentResponse> shipments = reportService.getShipmentsSentByCustomerReport(customerId);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/shipments/customer/{customerId}/received")
    @Operation(summary = "Shipments received by customer", description = "Lists shipments received by a customer")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsReceivedByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        logger.debug("Generating received shipments report for customer ID: {}", customerId);

        if (isCustomer(authentication)) {
            Long authCustomerId = getCustomerIdFromAuth(authentication);
            if (!customerId.equals(authCustomerId)) {
                throw new UnauthorizedException("You can only view your own received shipments");
            }
        }

        List<ShipmentResponse> shipments = reportService.getShipmentsReceivedByCustomerReport(customerId);
        return ResponseEntity.ok(shipments);
    }

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

    private boolean isCustomer(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private Long getCustomerIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        try {
            return customerService.getCustomerIdByUsername(username);
        } catch (Exception e) {
            throw new UnauthorizedException("Customer not found for user: " + username);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Dashboard metrics", description = "Returns aggregated dashboard metrics (Employee only)")
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        logger.debug("Getting dashboard metrics");
        DashboardMetricsResponse metrics = reportService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

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
