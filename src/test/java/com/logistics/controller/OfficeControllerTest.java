package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.office.OfficeRequest;
import com.logistics.dto.office.OfficeResponse;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.service.OfficeService;
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
 * Integration tests for OfficeController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OfficeService officeService;

    private OfficeResponse officeResponse;
    private OfficeRequest officeRequest;

    @BeforeEach
    void setUp() {
        officeResponse = new OfficeResponse();
        officeResponse.setId(1L);
        officeResponse.setCompanyId(1L);
        officeResponse.setCompanyName("Test Logistics");
        officeResponse.setName("Main Office");
        officeResponse.setAddress("123 Office St");
        officeResponse.setCity("Test City");
        officeResponse.setCountry("Test Country");
        officeResponse.setPhone("1234567890");
        officeResponse.setFullAddress("123 Office St, Test City, Test Country");
        officeResponse.setCreatedAt(LocalDateTime.now());

        officeRequest = new OfficeRequest();
        officeRequest.setCompanyId(1L);
        officeRequest.setName("Main Office");
        officeRequest.setAddress("123 Office St");
        officeRequest.setCity("Test City");
        officeRequest.setCountry("Test Country");
        officeRequest.setPhone("1234567890");
    }

    @Nested
    @DisplayName("POST /api/offices Tests")
    class CreateOfficeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should create office when authenticated as employee")
        void createOffice_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(officeService.createOffice(any(OfficeRequest.class))).thenReturn(officeResponse);

            // Act & Assert
            mockMvc.perform(post("/api/offices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Main Office"))
                    .andExpect(jsonPath("$.city").value("Test City"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createOffice_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/offices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void createOffice_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(post("/api/offices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid request")
        void createOffice_InvalidRequest_BadRequest() throws Exception {
            // Arrange - missing required fields
            OfficeRequest invalidRequest = new OfficeRequest();

            // Act & Assert
            mockMvc.perform(post("/api/offices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/offices/{id} Tests")
    class GetOfficeByIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return office when found")
        void getOfficeById_Found_Success() throws Exception {
            // Arrange
            when(officeService.getOfficeById(1L)).thenReturn(officeResponse);

            // Act & Assert
            mockMvc.perform(get("/api/offices/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Main Office"));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when office not found")
        void getOfficeById_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(officeService.getOfficeById(999L))
                    .thenThrow(new ResourceNotFoundException("Office", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/offices/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getOfficeById_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/offices/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/offices Tests")
    class GetAllOfficesTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all offices when authenticated as employee")
        void getAllOffices_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<OfficeResponse> offices = Arrays.asList(officeResponse);
            when(officeService.getAllOffices()).thenReturn(offices);

            // Act & Assert
            mockMvc.perform(get("/api/offices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllOffices_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/offices"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/offices/company/{companyId} Tests")
    class GetOfficesByCompanyIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return offices for company")
        void getOfficesByCompanyId_Success() throws Exception {
            // Arrange
            List<OfficeResponse> offices = Arrays.asList(officeResponse);
            when(officeService.getOfficesByCompanyId(1L)).thenReturn(offices);

            // Act & Assert
            mockMvc.perform(get("/api/offices/company/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].companyId").value(1));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getOfficesByCompanyId_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/offices/company/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/offices/{id} Tests")
    class UpdateOfficeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update office when authenticated as employee")
        void updateOffice_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(officeService.updateOffice(eq(1L), any(OfficeRequest.class))).thenReturn(officeResponse);

            // Act & Assert
            mockMvc.perform(put("/api/offices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when office not found")
        void updateOffice_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            when(officeService.updateOffice(eq(999L), any(OfficeRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Office", "id", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/offices/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void updateOffice_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(put("/api/offices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(officeRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/offices/{id} Tests")
    class DeleteOfficeTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should delete office when authenticated as employee")
        void deleteOffice_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            doNothing().when(officeService).deleteOffice(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/offices/1"))
                    .andExpect(status().isNoContent());

            verify(officeService).deleteOffice(1L);
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 404 when office not found")
        void deleteOffice_NotFound_ReturnsNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Office", "id", 999L))
                    .when(officeService).deleteOffice(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/offices/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void deleteOffice_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/offices/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
