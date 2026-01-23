package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.dto.report.CustomerMetricsResponse;
import com.logistics.dto.report.DashboardMetricsResponse;
import com.logistics.dto.report.RevenueResponse;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.exception.UnauthorizedException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.User;
import com.logistics.model.enums.EmployeeType;
import com.logistics.model.enums.Role;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.repository.CustomerRepository;
import com.logistics.service.ReportService;
import com.logistics.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ReportController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private ShipmentService shipmentService;

    @MockBean
    private CustomerRepository customerRepository;

    private EmployeeResponse employeeResponse;
    private CustomerResponse customerResponse;
    private ShipmentResponse shipmentResponse;
    private DashboardMetricsResponse dashboardMetrics;
    private CustomerMetricsResponse customerMetrics;
    private RevenueResponse revenueResponse;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Setup employee response
        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setUserId(1L);
        employeeResponse.setUsername("john.employee");
        employeeResponse.setName("john.employee");
        employeeResponse.setEmail("john@test.com");
        employeeResponse.setCompanyId(1L);
        employeeResponse.setCompanyName("Test Logistics");
        employeeResponse.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employeeResponse.setHireDate(LocalDate.now());
        employeeResponse.setSalary(new BigDecimal("50000.00"));

        // Setup customer response
        customerResponse = new CustomerResponse();
        customerResponse.setId(1L);
        customerResponse.setUserId(2L);
        customerResponse.setUsername("jane.customer");
        customerResponse.setName("jane.customer");
        customerResponse.setEmail("jane@test.com");
        customerResponse.setPhone("1234567890");
        customerResponse.setAddress("123 Customer St");

        // Setup shipment response
        shipmentResponse = new ShipmentResponse();
        shipmentResponse.setId(1L);
        shipmentResponse.setSenderId(1L);
        shipmentResponse.setSenderName("John Doe");
        shipmentResponse.setRecipientId(2L);
        shipmentResponse.setRecipientName("Jane Doe");
        shipmentResponse.setDeliveryAddress("123 Test St");
        shipmentResponse.setWeight(new BigDecimal("5.00"));
        shipmentResponse.setPrice(new BigDecimal("25.00"));
        shipmentResponse.setStatus(ShipmentStatus.DELIVERED);
        shipmentResponse.setRegisteredAt(LocalDateTime.now());

        // Setup dashboard metrics
        dashboardMetrics = new DashboardMetricsResponse(100L, 20L, 75L, new BigDecimal("5000.00"));

        // Setup customer metrics
        customerMetrics = new CustomerMetricsResponse(10L, 5L, 2L, new BigDecimal("250.00"));

        // Setup revenue response
        revenueResponse = new RevenueResponse(
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                new BigDecimal("1000.00"),
                50L
        );

        // Setup customer entity for auth lookup
        User customerUser = new User();
        customerUser.setId(2L);
        customerUser.setUsername("jane.customer");
        customerUser.setEmail("jane@test.com");
        customerUser.setRole(Role.CUSTOMER);

        customer = new Customer(customerUser);
        customer.setId(1L);
        customer.setPhone("1234567890");
        customer.setAddress("123 Customer St");
    }

    @Nested
    @DisplayName("GET /api/reports/employees Tests")
    class GetAllEmployeesReportTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all employees when authenticated as employee")
        void getAllEmployeesReport_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
            when(reportService.getAllEmployeesReport()).thenReturn(employees);

            // Act & Assert
            mockMvc.perform(get("/api/reports/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].username").value("john.employee"));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getAllEmployeesReport_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/employees"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllEmployeesReport_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/reports/employees"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/customers Tests")
    class GetAllCustomersReportTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all customers when authenticated as employee")
        void getAllCustomersReport_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<CustomerResponse> customers = Arrays.asList(customerResponse);
            when(reportService.getAllCustomersReport()).thenReturn(customers);

            // Act & Assert
            mockMvc.perform(get("/api/reports/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].username").value("jane.customer"));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getAllCustomersReport_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/customers"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/shipments Tests")
    class GetAllShipmentsReportTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all shipments when authenticated as employee")
        void getAllShipmentsReport_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getAllShipmentsReport()).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @WithMockUser(username = "jane.customer", roles = {"CUSTOMER"})
        @DisplayName("Should return only customer's shipments when authenticated as customer")
        void getAllShipmentsReport_AuthenticatedCustomer_ReturnsOwnShipments() throws Exception {
            // Arrange
            when(customerRepository.findByUsername("jane.customer")).thenReturn(Optional.of(customer));
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(shipmentService.getShipmentsByCustomerId(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/reports/shipments/employee/{employeeId} Tests")
    class GetShipmentsByEmployeeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return shipments by employee when authenticated as employee")
        void getShipmentsByEmployee_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getShipmentsByEmployeeReport(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/employee/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when employee not found")
        void getShipmentsByEmployee_EmployeeNotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(reportService.getShipmentsByEmployeeReport(999L))
                    .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/employee/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getShipmentsByEmployee_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/shipments/employee/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/shipments/pending Tests")
    class GetPendingShipmentsReportTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return pending shipments when authenticated as employee")
        void getPendingShipmentsReport_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            shipmentResponse.setStatus(ShipmentStatus.REGISTERED);
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getPendingShipmentsReport()).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getPendingShipmentsReport_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/shipments/pending"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/shipments/customer/{customerId}/sent Tests")
    class GetShipmentsSentByCustomerTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return sent shipments when authenticated as employee")
        void getShipmentsSentByCustomer_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getShipmentsSentByCustomerReport(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/customer/1/sent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "jane.customer", roles = {"CUSTOMER"})
        @DisplayName("Should return own sent shipments when authenticated as customer")
        void getShipmentsSentByCustomer_OwnShipments_Success() throws Exception {
            // Arrange
            when(customerRepository.findByUsername("jane.customer")).thenReturn(Optional.of(customer));
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getShipmentsSentByCustomerReport(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/customer/1/sent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "jane.customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when customer tries to access other customer's shipments")
        void getShipmentsSentByCustomer_OtherCustomerShipments_Forbidden() throws Exception {
            // Arrange
            when(customerRepository.findByUsername("jane.customer")).thenReturn(Optional.of(customer));

            // Act & Assert - customer ID 1 trying to access customer ID 999's shipments
            mockMvc.perform(get("/api/reports/shipments/customer/999/sent"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/shipments/customer/{customerId}/received Tests")
    class GetShipmentsReceivedByCustomerTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return received shipments when authenticated as employee")
        void getShipmentsReceivedByCustomer_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getShipmentsReceivedByCustomerReport(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/customer/1/received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "jane.customer", roles = {"CUSTOMER"})
        @DisplayName("Should return own received shipments when authenticated as customer")
        void getShipmentsReceivedByCustomer_OwnShipments_Success() throws Exception {
            // Arrange
            when(customerRepository.findByUsername("jane.customer")).thenReturn(Optional.of(customer));
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(reportService.getShipmentsReceivedByCustomerReport(1L)).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/reports/shipments/customer/1/received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/reports/revenue Tests")
    class GetRevenueReportTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return revenue report when authenticated as employee")
        void getRevenueReport_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(reportService.getRevenueReport(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(revenueResponse);

            // Act & Assert
            mockMvc.perform(get("/api/reports/revenue")
                            .param("startDate", LocalDate.now().minusDays(7).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRevenue").value(1000.00))
                    .andExpect(jsonPath("$.deliveredShipmentsCount").value(50));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getRevenueReport_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/revenue")
                            .param("startDate", LocalDate.now().minusDays(7).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getRevenueReport_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/reports/revenue")
                            .param("startDate", LocalDate.now().minusDays(7).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/dashboard Tests")
    class GetDashboardMetricsTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return dashboard metrics when authenticated as employee")
        void getDashboardMetrics_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(reportService.getDashboardMetrics()).thenReturn(dashboardMetrics);

            // Act & Assert
            mockMvc.perform(get("/api/reports/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalShipments").value(100))
                    .andExpect(jsonPath("$.pendingShipments").value(20))
                    .andExpect(jsonPath("$.deliveredShipments").value(75))
                    .andExpect(jsonPath("$.totalRevenue").value(5000.00));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getDashboardMetrics_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/dashboard"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/reports/customer-metrics Tests")
    class GetCustomerMetricsTests {

        @Test
        @WithMockUser(username = "jane.customer", roles = {"CUSTOMER"})
        @DisplayName("Should return customer metrics when authenticated as customer")
        void getCustomerMetrics_AuthenticatedCustomer_Success() throws Exception {
            // Arrange
            when(customerRepository.findByUsername("jane.customer")).thenReturn(Optional.of(customer));
            when(reportService.getCustomerMetrics(1L)).thenReturn(customerMetrics);

            // Act & Assert
            mockMvc.perform(get("/api/reports/customer-metrics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSent").value(10))
                    .andExpect(jsonPath("$.totalReceived").value(5))
                    .andExpect(jsonPath("$.inTransit").value(2))
                    .andExpect(jsonPath("$.totalSpent").value(250.00));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 403 when authenticated as employee")
        void getCustomerMetrics_AuthenticatedEmployee_Forbidden() throws Exception {
            mockMvc.perform(get("/api/reports/customer-metrics"))
                    .andExpect(status().isForbidden());
        }
    }
}
