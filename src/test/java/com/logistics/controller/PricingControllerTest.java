package com.logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.pricing.PricingConfigRequest;
import com.logistics.model.entity.PricingConfig;
import com.logistics.security.CustomUserDetailsService;
import com.logistics.security.JwtAuthenticationEntryPoint;
import com.logistics.security.JwtTokenProvider;
import com.logistics.service.impl.PricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for PricingController.
 * Tests HTTP endpoints with mocked service layer.
 */
@WebMvcTest(PricingController.class)
class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PricingServiceImpl pricingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private PricingConfig pricingConfig;

    @BeforeEach
    void setUp() {
        pricingConfig = new PricingConfig(
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("10.00")
        );
        pricingConfig.setId(1L);
    }

    @Nested
    @DisplayName("GET /api/pricing Tests")
    class GetPricingInfoTests {

        @Test
        @WithMockUser(username = "user", roles = {"CUSTOMER"})
        @DisplayName("Should return pricing info when authenticated")
        void getPricingInfo_Authenticated_Success() throws Exception {
            // Arrange
            when(pricingService.getBasePrice()).thenReturn(new BigDecimal("5.00"));
            when(pricingService.getPricePerKg()).thenReturn(new BigDecimal("2.00"));
            when(pricingService.getAddressDeliveryFee()).thenReturn(new BigDecimal("10.00"));

            // Act & Assert
            mockMvc.perform(get("/api/pricing"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.basePrice").value(5.00))
                    .andExpect(jsonPath("$.pricePerKg").value(2.00))
                    .andExpect(jsonPath("$.addressDeliveryFee").value(10.00));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getPricingInfo_NotAuthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/pricing"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/pricing/config Tests")
    class GetPricingConfigTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return pricing config when authenticated as employee")
        void getPricingConfig_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            when(pricingService.getActivePricingConfig()).thenReturn(pricingConfig);

            // Act & Assert
            mockMvc.perform(get("/api/pricing/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.basePrice").value(5.00))
                    .andExpect(jsonPath("$.pricePerKg").value(2.00));
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void getPricingConfig_AuthenticatedCustomer_Forbidden() throws Exception {
            mockMvc.perform(get("/api/pricing/config"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/pricing/config Tests")
    class UpdatePricingConfigTests {

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should update pricing config when authenticated as employee")
        void updatePricingConfig_AuthenticatedEmployee_Success() throws Exception {
            // Arrange
            PricingConfigRequest request = new PricingConfigRequest(
                    new BigDecimal("7.00"),
                    new BigDecimal("3.00"),
                    new BigDecimal("15.00")
            );

            PricingConfig updatedConfig = new PricingConfig(
                    new BigDecimal("7.00"),
                    new BigDecimal("3.00"),
                    new BigDecimal("15.00")
            );
            updatedConfig.setId(2L);

            when(pricingService.updatePricingConfig(any(), any(), any())).thenReturn(updatedConfig);

            // Act & Assert
            mockMvc.perform(put("/api/pricing/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.basePrice").value(7.00))
                    .andExpect(jsonPath("$.pricePerKg").value(3.00));
        }

        @Test
        @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
        @DisplayName("Should return 400 for invalid pricing values")
        void updatePricingConfig_InvalidValues_BadRequest() throws Exception {
            // Arrange - null required fields
            PricingConfigRequest invalidRequest = new PricingConfigRequest();

            // Act & Assert
            mockMvc.perform(put("/api/pricing/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "customer", roles = {"CUSTOMER"})
        @DisplayName("Should return 403 when authenticated as customer")
        void updatePricingConfig_AuthenticatedCustomer_Forbidden() throws Exception {
            // Arrange
            PricingConfigRequest request = new PricingConfigRequest(
                    new BigDecimal("7.00"),
                    new BigDecimal("3.00"),
                    new BigDecimal("15.00")
            );

            // Act & Assert
            mockMvc.perform(put("/api/pricing/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }
}
