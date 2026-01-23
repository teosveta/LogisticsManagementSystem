package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.service.CustomerService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse customerResponse;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customerResponse = new CustomerResponse();
        customerResponse.setId(1L);
        customerResponse.setUserId(1L);
        customerResponse.setUsername("jane.doe");
        customerResponse.setName("jane.doe");
        customerResponse.setEmail("jane.doe@test.com");
        customerResponse.setPhone("1234567890");
        customerResponse.setAddress("123 Customer St");
        customerResponse.setCreatedAt(LocalDateTime.now());

        customerRequest = new CustomerRequest();
        customerRequest.setUserId(1L);
        customerRequest.setPhone("1234567890");
        customerRequest.setAddress("123 Customer St");
    }

    @Nested
    @DisplayName("POST /api/customers Tests")
    class CreateCustomerTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should create customer when authenticated as employee")
        void createCustomer_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(customerResponse);

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("jane.doe"))
                    .andExpect(jsonPath("$.phone").value("1234567890"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createCustomer_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void createCustomer_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid request")
        void createCustomer_InvalidRequest_BadRequest() throws Exception {
            // Arrange - missing required fields
            CustomerRequest invalidRequest = new CustomerRequest();

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/customers/{id} Tests")
    class GetCustomerByIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return customer when found")
        void getCustomerById_Found_Success() throws Exception {
            // Arrange
            when(customerService.getCustomerById(1L)).thenReturn(customerResponse);

            // Act & Assert
            mockMvc.perform(get("/api/customers/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("jane.doe"));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when customer not found")
        void getCustomerById_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(customerService.getCustomerById(999L))
                    .thenThrow(new ResourceNotFoundException("Customer", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/customers/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getCustomerById_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/customers/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/customers Tests")
    class GetAllCustomersTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all customers when authenticated as employee")
        void getAllCustomers_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<CustomerResponse> customers = Arrays.asList(customerResponse);
            when(customerService.getAllCustomers()).thenReturn(customers);

            // Act & Assert
            mockMvc.perform(get("/api/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllCustomers_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/customers"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getAllCustomers_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/customers"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/customers/{id} Tests")
    class UpdateCustomerTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update customer when authenticated as employee")
        void updateCustomer_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(customerService.updateCustomer(eq(1L), any(CustomerRequest.class))).thenReturn(customerResponse);

            // Act & Assert
            mockMvc.perform(put("/api/customers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when customer not found")
        void updateCustomer_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(customerService.updateCustomer(eq(999L), any(CustomerRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Customer", "id", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/customers/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void updateCustomer_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(put("/api/customers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/customers/{id} Tests")
    class DeleteCustomerTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should delete customer when authenticated as employee")
        void deleteCustomer_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            doNothing().when(customerService).deleteCustomer(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/customers/1"))
                    .andExpect(status().isNoContent());

            verify(customerService).deleteCustomer(1L);
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when customer not found")
        void deleteCustomer_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Customer", "id", 999L))
                    .when(customerService).deleteCustomer(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/customers/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void deleteCustomer_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/customers/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
