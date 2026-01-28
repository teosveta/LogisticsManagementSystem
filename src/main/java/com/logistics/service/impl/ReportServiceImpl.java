package com.logistics.service.impl;

import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.ReportService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ShipmentRepository shipmentRepository;

    public ReportServiceImpl(EmployeeRepository employeeRepository,
                             CustomerRepository customerRepository,
                             ShipmentRepository shipmentRepository) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public List<EmployeeResponse> getAllEmployeesReport() {
        logger.debug("Generating all employees report");

        return employeeRepository.findAll().stream()
                .map(EntityMapper::toEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getAllCustomersReport() {
        logger.debug("Generating all customers report");

        return customerRepository.findAll().stream()
                .map(EntityMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getAllShipmentsReport() {
        logger.debug("Generating all shipments report");

        return shipmentRepository.findAll().stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getShipmentsByEmployeeReport(Long employeeId) {
        logger.debug("Generating shipments report for employee ID: {}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        return shipmentRepository.findByRegisteredById(employeeId).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getPendingShipmentsReport() {
        logger.debug("Generating pending shipments report");

        return shipmentRepository.findAllPendingShipments().stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getShipmentsSentByCustomerReport(Long customerId) {
        logger.debug("Generating sent shipments report for customer ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return shipmentRepository.findBySenderId(customerId).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getShipmentsReceivedByCustomerReport(Long customerId) {
        logger.debug("Generating received shipments report for customer ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return shipmentRepository.findByRecipientId(customerId).stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Only DELIVERED shipments count as revenue - cancelled/pending shipments haven't been paid for.
     */
    @Override
    public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating revenue report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        BigDecimal totalRevenue = shipmentRepository.calculateRevenueBetweenDates(startDateTime, endDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long deliveredCount = shipmentRepository.findDeliveredShipmentsBetweenDates(startDateTime, endDateTime).size();

        logger.info("Revenue report: {} total from {} delivered shipments", totalRevenue, deliveredCount);

        return new RevenueResponse(startDate, endDate, totalRevenue, deliveredCount);
    }

    @Override
    public DashboardMetricsResponse getDashboardMetrics() {
        logger.debug("Generating dashboard metrics");

        long total = shipmentRepository.count();
        long pending = shipmentRepository.countInTransitShipments();
        long delivered = shipmentRepository.countByStatus(ShipmentStatus.DELIVERED);
        BigDecimal totalRevenue = shipmentRepository.calculateTotalRevenue();

        logger.debug("Dashboard metrics: total={}, pending={}, delivered={}, revenue={}",
                total, pending, delivered, totalRevenue);

        return new DashboardMetricsResponse(total, pending, delivered, totalRevenue);
    }

    @Override
    public CustomerMetricsResponse getCustomerMetrics(Long customerId) {
        logger.debug("Generating customer metrics for customer ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        long totalSent = shipmentRepository.countBySenderId(customerId);
        long totalReceived = shipmentRepository.countDeliveredByRecipientId(customerId);

        long inTransitSent = shipmentRepository.countInTransitBySenderId(customerId);
        long inTransitReceived = shipmentRepository.countInTransitByRecipientId(customerId);
        long inTransit = inTransitSent + inTransitReceived;

        BigDecimal totalSpent = shipmentRepository.calculateTotalSpentBySenderId(customerId);

        logger.debug("Customer metrics for {}: sent={}, received={}, inTransit={}, spent={}",
                customerId, totalSent, totalReceived, inTransit, totalSpent);

        return new CustomerMetricsResponse(totalSent, totalReceived, inTransit, totalSpent);
    }
}
