package com.logistics.service;

import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.*;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService.
 * Tests report generation logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User employeeUser;
    private User customerUser;
    private Employee employee;
    private Customer customer;
    private Company company;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        // Setup test user for employee
        employeeUser = new User();
        employeeUser.setId(1L);
        employeeUser.setUsername("employee");
        employeeUser.setEmail("employee@test.com");
        employeeUser.setRole(Role.EMPLOYEE);

        // Setup test user for customer
        customerUser = new User();
        customerUser.setId(2L);
        customerUser.setUsername("customer");
        customerUser.setEmail("customer@test.com");
        customerUser.setRole(Role.CUSTOMER);

        // Setup test company
        company = new Company();
        company.setId(1L);
        company.setName("Test Logistics");
        company.setRegistrationNumber("TEST123");
        company.setAddress("123 Company St");

        // Setup test employee
        employee = new Employee();
        employee.setId(1L);
        employee.setUser(employeeUser);
        employee.setCompany(company);
        employee.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employee.setHireDate(LocalDate.now());
        employee.setSalary(new BigDecimal("50000.00"));

        // Setup test customer
        customer = new Customer(customerUser);
        customer.setId(1L);
        customer.setPhone("1234567890");
        customer.setAddress("123 Customer St");

        // Setup test shipment
        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setSender(customer);
        shipment.setRecipient(customer);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(new BigDecimal("5.00"));
        shipment.setPrice(new BigDecimal("25.00"));
        shipment.setDeliveryAddress("789 Delivery Rd");
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getAllEmployeesReport Tests")
    class GetAllEmployeesReportTests {

        @Test
        @DisplayName("Should return all employees")
        void getAllEmployeesReport_ReturnsAllEmployees() {
            // Arrange
            when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee));

            // Act
            List<EmployeeResponse> result = reportService.getAllEmployeesReport();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no employees exist")
        void getAllEmployeesReport_NoEmployees_ReturnsEmptyList() {
            // Arrange
            when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<EmployeeResponse> result = reportService.getAllEmployeesReport();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllCustomersReport Tests")
    class GetAllCustomersReportTests {

        @Test
        @DisplayName("Should return all customers")
        void getAllCustomersReport_ReturnsAllCustomers() {
            // Arrange
            when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));

            // Act
            List<CustomerResponse> result = reportService.getAllCustomersReport();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(customerRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no customers exist")
        void getAllCustomersReport_NoCustomers_ReturnsEmptyList() {
            // Arrange
            when(customerRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<CustomerResponse> result = reportService.getAllCustomersReport();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllShipmentsReport Tests")
    class GetAllShipmentsReportTests {

        @Test
        @DisplayName("Should return all shipments")
        void getAllShipmentsReport_ReturnsAllShipments() {
            // Arrange
            when(shipmentRepository.findAll()).thenReturn(Arrays.asList(shipment));

            // Act
            List<ShipmentResponse> result = reportService.getAllShipmentsReport();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(shipmentRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no shipments exist")
        void getAllShipmentsReport_NoShipments_ReturnsEmptyList() {
            // Arrange
            when(shipmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<ShipmentResponse> result = reportService.getAllShipmentsReport();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getShipmentsByEmployeeReport Tests")
    class GetShipmentsByEmployeeReportTests {

        @Test
        @DisplayName("Should return shipments for existing employee")
        void getShipmentsByEmployeeReport_EmployeeExists_ReturnsShipments() {
            // Arrange
            when(employeeRepository.existsById(1L)).thenReturn(true);
            when(shipmentRepository.findByRegisteredById(1L)).thenReturn(Arrays.asList(shipment));

            // Act
            List<ShipmentResponse> result = reportService.getShipmentsByEmployeeReport(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when employee not found")
        void getShipmentsByEmployeeReport_EmployeeNotFound_ThrowsException() {
            // Arrange
            when(employeeRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> reportService.getShipmentsByEmployeeReport(999L));
        }
    }

    @Nested
    @DisplayName("getPendingShipmentsReport Tests")
    class GetPendingShipmentsReportTests {

        @Test
        @DisplayName("Should return pending shipments")
        void getPendingShipmentsReport_ReturnsPendingShipments() {
            // Arrange
            Shipment pendingShipment = new Shipment();
            pendingShipment.setId(2L);
            pendingShipment.setSender(customer);
            pendingShipment.setRecipient(customer);
            pendingShipment.setRegisteredBy(employee);
            pendingShipment.setWeight(new BigDecimal("3.00"));
            pendingShipment.setPrice(new BigDecimal("15.00"));
            pendingShipment.setDeliveryAddress("456 Pending St");
            pendingShipment.setStatus(ShipmentStatus.REGISTERED);

            when(shipmentRepository.findAllPendingShipments()).thenReturn(Arrays.asList(pendingShipment));

            // Act
            List<ShipmentResponse> result = reportService.getPendingShipmentsReport();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getShipmentsSentByCustomerReport Tests")
    class GetShipmentsSentByCustomerReportTests {

        @Test
        @DisplayName("Should return shipments sent by customer")
        void getShipmentsSentByCustomerReport_CustomerExists_ReturnsShipments() {
            // Arrange
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(shipmentRepository.findBySenderId(1L)).thenReturn(Arrays.asList(shipment));

            // Act
            List<ShipmentResponse> result = reportService.getShipmentsSentByCustomerReport(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void getShipmentsSentByCustomerReport_CustomerNotFound_ThrowsException() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> reportService.getShipmentsSentByCustomerReport(999L));
        }
    }

    @Nested
    @DisplayName("getShipmentsReceivedByCustomerReport Tests")
    class GetShipmentsReceivedByCustomerReportTests {

        @Test
        @DisplayName("Should return shipments received by customer")
        void getShipmentsReceivedByCustomerReport_CustomerExists_ReturnsShipments() {
            // Arrange
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(shipmentRepository.findByRecipientId(1L)).thenReturn(Arrays.asList(shipment));

            // Act
            List<ShipmentResponse> result = reportService.getShipmentsReceivedByCustomerReport(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void getShipmentsReceivedByCustomerReport_CustomerNotFound_ThrowsException() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> reportService.getShipmentsReceivedByCustomerReport(999L));
        }
    }

    @Nested
    @DisplayName("getRevenueReport Tests")
    class GetRevenueReportTests {

        @Test
        @DisplayName("Should return revenue report with valid data")
        void getRevenueReport_ValidData_ReturnsRevenueReport() {
            // Arrange
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            BigDecimal totalRevenue = new BigDecimal("100.00");

            when(shipmentRepository.calculateRevenueBetweenDates(any(), any())).thenReturn(totalRevenue);
            when(shipmentRepository.findDeliveredShipmentsBetweenDates(any(), any()))
                    .thenReturn(Arrays.asList(shipment));

            // Act
            RevenueResponse result = reportService.getRevenueReport(startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(startDate, result.getStartDate());
            assertEquals(endDate, result.getEndDate());
            assertEquals(totalRevenue, result.getTotalRevenue());
            assertEquals(1, result.getDeliveredShipmentsCount());
        }

        @Test
        @DisplayName("Should return zero revenue when no delivered shipments")
        void getRevenueReport_NoDeliveredShipments_ReturnsZeroRevenue() {
            // Arrange
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(shipmentRepository.calculateRevenueBetweenDates(any(), any())).thenReturn(null);
            when(shipmentRepository.findDeliveredShipmentsBetweenDates(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            RevenueResponse result = reportService.getRevenueReport(startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
            assertEquals(0, result.getDeliveredShipmentsCount());
        }
    }

    @Nested
    @DisplayName("getDashboardMetrics Tests")
    class GetDashboardMetricsTests {

        @Test
        @DisplayName("Should return dashboard metrics")
        void getDashboardMetrics_ReturnsDashboardMetrics() {
            // Arrange
            when(shipmentRepository.count()).thenReturn(10L);
            when(shipmentRepository.countInTransitShipments()).thenReturn(3L);
            when(shipmentRepository.countByStatus(ShipmentStatus.DELIVERED)).thenReturn(5L);
            when(shipmentRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("500.00"));

            // Act
            DashboardMetricsResponse result = reportService.getDashboardMetrics();

            // Assert
            assertNotNull(result);
            assertEquals(10L, result.getTotalShipments());
            assertEquals(3L, result.getPendingShipments());
            assertEquals(5L, result.getDeliveredShipments());
            assertEquals(new BigDecimal("500.00"), result.getTotalRevenue());
        }
    }

    @Nested
    @DisplayName("getCustomerMetrics Tests")
    class GetCustomerMetricsTests {

        @Test
        @DisplayName("Should return customer metrics")
        void getCustomerMetrics_CustomerExists_ReturnsMetrics() {
            // Arrange
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(shipmentRepository.countBySenderId(1L)).thenReturn(5L);
            when(shipmentRepository.countDeliveredByRecipientId(1L)).thenReturn(3L);
            when(shipmentRepository.countInTransitBySenderId(1L)).thenReturn(1L);
            when(shipmentRepository.countInTransitByRecipientId(1L)).thenReturn(1L);
            when(shipmentRepository.calculateTotalSpentBySenderId(1L)).thenReturn(new BigDecimal("150.00"));

            // Act
            CustomerMetricsResponse result = reportService.getCustomerMetrics(1L);

            // Assert
            assertNotNull(result);
            assertEquals(5L, result.getTotalSent());
            assertEquals(3L, result.getTotalReceived());
            assertEquals(2L, result.getInTransit());
            assertEquals(new BigDecimal("150.00"), result.getTotalSpent());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void getCustomerMetrics_CustomerNotFound_ThrowsException() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> reportService.getCustomerMetrics(999L));
        }
    }
}
