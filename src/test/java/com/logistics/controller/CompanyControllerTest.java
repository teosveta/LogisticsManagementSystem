package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.company.CompanyRequest;
import com.logistics.dto.company.CompanyResponse;
import com.logistics.service.CompanyService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CompanyController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    private CompanyResponse companyResponse;
    private CompanyRequest companyRequest;

    @BeforeEach
    void setUp() {
        companyResponse = new CompanyResponse();
        companyResponse.setId(1L);
        companyResponse.setName("Test Logistics");
        companyResponse.setRegistrationNumber("REG123");
        companyResponse.setAddress("123 Test St");
        companyResponse.setPhone("1234567890");
        companyResponse.setEmail("test@company.com");

        companyRequest = new CompanyRequest();
        companyRequest.setName("Test Logistics");
        companyRequest.setRegistrationNumber("REG123");
        companyRequest.setAddress("123 Test St");
        companyRequest.setPhone("1234567890");
        companyRequest.setEmail("test@company.com");
    }

    @Nested
    @DisplayName("POST /api/companies Tests")
    class CreateCompanyTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should create company when authenticated as employee")
        void createCompany_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(companyService.createCompany(any(CompanyRequest.class))).thenReturn(companyResponse);

            // Act & Assert
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(companyRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Logistics"))
                    .andExpect(jsonPath("$.registrationNumber").value("REG123"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createCompany_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(companyRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void createCompany_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(companyRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid request - missing required fields")
        void createCompany_MissingRequiredFields_BadRequest() throws Exception {
            // Arrange - missing required fields
            CompanyRequest invalidRequest = new CompanyRequest();

            // Act & Assert
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/companies Tests")
    class GetAllCompaniesTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all companies when authenticated")
        void getAllCompanies_Authenticated_Success() throws Exception {
            // Arrange
            List<CompanyResponse> companies = Arrays.asList(companyResponse);
            when(companyService.getAllCompanies()).thenReturn(companies);

            // Act & Assert
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Logistics"));
        }
    }

    @Nested
    @DisplayName("GET /api/companies/{id} Tests")
    class GetCompanyByIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return company when found")
        void getCompanyById_Found_Success() throws Exception {
            // Arrange
            when(companyService.getCompanyById(1L)).thenReturn(companyResponse);

            // Act & Assert
            mockMvc.perform(get("/api/companies/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Logistics"));
        }
    }

    @Nested
    @DisplayName("PUT /api/companies/{id} Tests")
    class UpdateCompanyTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update company when authenticated as employee")
        void updateCompany_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            companyResponse.setName("Updated Logistics");
            when(companyService.updateCompany(eq(1L), any(CompanyRequest.class))).thenReturn(companyResponse);

            companyRequest.setName("Updated Logistics");

            // Act & Assert
            mockMvc.perform(put("/api/companies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(companyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Logistics"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/companies/{id} Tests")
    class DeleteCompanyTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should delete company when authenticated as employee")
        void deleteCompany_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            doNothing().when(companyService).deleteCompany(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/companies/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void deleteCompany_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/companies/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
