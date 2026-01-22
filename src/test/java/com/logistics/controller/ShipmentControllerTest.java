package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.shipment.ShipmentRequest;
import com.logistics.dto.shipment.ShipmentResponse;
import com.logistics.dto.shipment.ShipmentStatusUpdateRequest;
import com.logistics.model.enums.ShipmentStatus;
import com.logistics.service.ShipmentService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ShipmentController.
 * Tests HTTP endpoints with authentication/authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShipmentService shipmentService;

    private ShipmentResponse shipmentResponse;
    private ShipmentRequest shipmentRequest;

    @BeforeEach
    void setUp() {
        shipmentResponse = new ShipmentResponse();
        shipmentResponse.setId(1L);
        shipmentResponse.setSenderId(1L);
        shipmentResponse.setSenderName("John Doe");
        shipmentResponse.setRecipientId(2L);
        shipmentResponse.setRecipientName("Jane Doe");
        shipmentResponse.setDeliveryAddress("123 Test St");
        shipmentResponse.setWeight(new BigDecimal("5.00"));
        shipmentResponse.setPrice(new BigDecimal("25.00"));
        shipmentResponse.setStatus(ShipmentStatus.REGISTERED);
        shipmentResponse.setRegisteredAt(LocalDateTime.now());

        shipmentRequest = new ShipmentRequest();
        shipmentRequest.setSenderId(1L);
        shipmentRequest.setRecipientId(2L);
        shipmentRequest.setDeliveryAddress("123 Test St");
        shipmentRequest.setWeight(new BigDecimal("5.00"));
    }

    @Nested
    @DisplayName("POST /api/shipments Tests")
    class CreateShipmentTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should create shipment when authenticated as employee")
        void createShipment_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(shipmentService.registerShipment(any(ShipmentRequest.class), eq("employee")))
                    .thenReturn(shipmentResponse);

            // Act & Assert
            mockMvc.perform(post("/api/shipments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shipmentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.price").value(25.00))
                    .andExpect(jsonPath("$.status").value("REGISTERED"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createShipment_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/shipments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shipmentRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void createShipment_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(post("/api/shipments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shipmentRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid request")
        void createShipment_InvalidRequest_BadRequest() throws Exception {
            // Arrange - missing required fields
            ShipmentRequest invalidRequest = new ShipmentRequest();

            // Act & Assert
            mockMvc.perform(post("/api/shipments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/shipments Tests")
    class GetAllShipmentsTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return all shipments when authenticated as employee")
        void getAllShipments_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            List<ShipmentResponse> shipments = Arrays.asList(shipmentResponse);
            when(shipmentService.getAllShipments()).thenReturn(shipments);

            // Act & Assert
            mockMvc.perform(get("/api/shipments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllShipments_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/shipments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/shipments/{id} Tests")
    class GetShipmentByIdTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return shipment when found")
        void getShipmentById_Found_Success() throws Exception {
            // Arrange
            when(shipmentService.getShipmentById(1L)).thenReturn(shipmentResponse);

            // Act & Assert
            mockMvc.perform(get("/api/shipments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.senderName").value("John Doe"));
        }
    }

    @Nested
    @DisplayName("PUT /api/shipments/{id} Tests")
    class UpdateShipmentTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update shipment when authenticated as employee")
        void updateShipment_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(shipmentService.updateShipment(eq(1L), any(ShipmentRequest.class), eq("employee")))
                    .thenReturn(shipmentResponse);

            // Act & Assert
            mockMvc.perform(put("/api/shipments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shipmentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("PATCH /api/shipments/{id}/status Tests")
    class UpdateShipmentStatusTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update status when authenticated as employee")
        void updateShipmentStatus_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            ShipmentStatusUpdateRequest statusRequest = new ShipmentStatusUpdateRequest();
            statusRequest.setStatus(ShipmentStatus.IN_TRANSIT);

            shipmentResponse.setStatus(ShipmentStatus.IN_TRANSIT);
            when(shipmentService.updateShipmentStatus(eq(1L), any(ShipmentStatusUpdateRequest.class)))
                    .thenReturn(shipmentResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/shipments/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/shipments/{id} Tests")
    class DeleteShipmentTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should delete shipment when authenticated as employee")
        void deleteShipment_AuthenticatedEmployee_Success() throws Exception {
            mockMvc.perform(delete("/api/shipments/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void deleteShipment_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/shipments/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
