package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.employee.EmployeeRequest;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.enums.EmployeeType;
import com.logistics.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponse employeeResponse;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        employeeResponse = new EmployeeResponse();
        employeeResponse.setId(1L);
        employeeResponse.setUserId(1L);
        employeeResponse.setUsername("john.doe");
        employeeResponse.setName("john.doe");
        employeeResponse.setEmail("john.doe@test.com");
        employeeResponse.setCompanyId(1L);
        employeeResponse.setCompanyName("Test Logistics");
        employeeResponse.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employeeResponse.setHireDate(LocalDate.now());
        employeeResponse.setSalary(new BigDecimal("50000.00"));
        employeeResponse.setCreatedAt(LocalDateTime.now());

        employeeRequest = new EmployeeRequest();
        employeeRequest.setUserId(1L);
        employeeRequest.setCompanyId(1L);
        employeeRequest.setEmployeeType(EmployeeType.OFFICE_STAFF);
        employeeRequest.setHireDate(LocalDate.now());
        employeeRequest.setSalary(new BigDecimal("50000.00"));
    }

    @Nested
    @DisplayName("POST /api/employees Tests")
    class CreateEmployeeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should create employee when authenticated as employee")
        void createEmployee_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(employeeResponse);

            // Act & Assert
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("john.doe"))
                    .andExpect(jsonPath("$.employeeType").value("OFFICE_STAFF"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createEmployee_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void createEmployee_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid request")
        void createEmployee_InvalidRequest_BadRequest() throws Exception {
            // Arrange - missing required fields
            EmployeeRequest invalidRequest = new EmployeeRequest();

            // Act & Assert
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/employees/{id} Tests")
    class GetEmployeeByIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return employee when found")
        void getEmployeeById_Found_Success() throws Exception {
            // Arrange
            when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

            // Act & Assert
            mockMvc.perform(get("/api/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("john.doe"));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when employee not found")
        void getEmployeeById_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(employeeService.getEmployeeById(999L))
                    .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/employees/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getEmployeeById_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/employees/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/employees Tests")
    class GetAllEmployeesTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all employees when authenticated as employee")
        void getAllEmployees_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<EmployeeResponse> employees = Arrays.asList(employeeResponse);
            when(employeeService.getAllEmployees()).thenReturn(employees);

            // Act & Assert
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllEmployees_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getAllEmployees_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/employees/{id} Tests")
    class UpdateEmployeeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update employee when authenticated as employee")
        void updateEmployee_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(employeeResponse);

            // Act & Assert
            mockMvc.perform(put("/api/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when employee not found")
        void updateEmployee_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(employeeService.updateEmployee(eq(999L), any(EmployeeRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/employees/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void updateEmployee_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(put("/api/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/employees/{id} Tests")
    class DeleteEmployeeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should delete employee when authenticated as employee")
        void deleteEmployee_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            doNothing().when(employeeService).deleteEmployee(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/employees/1"))
                    .andExpect(status().isNoContent());

            verify(employeeService).deleteEmployee(1L);
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when employee not found")
        void deleteEmployee_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Employee", "id", 999L))
                    .when(employeeService).deleteEmployee(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/employees/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void deleteEmployee_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/employees/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
