package com.logistics.service;

import com.logistics.exception.InvalidDataException;
import com.logistics.model.entity.PricingConfig;
import com.logistics.repository.PricingConfigRepository;
import com.logistics.service.impl.PricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PricingService.
 * Tests pricing calculation logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingConfigRepository pricingConfigRepository;

    @InjectMocks
    private PricingServiceImpl pricingService;

    private PricingConfig defaultConfig;

    @BeforeEach
    void setUp() {
        defaultConfig = new PricingConfig(
                new BigDecimal("5.00"),   // base price
                new BigDecimal("2.00"),   // price per kg
                new BigDecimal("10.00")   // address delivery fee
        );
    }

    @Nested
    @DisplayName("calculatePrice Tests")
    class CalculatePriceTests {

        @Test
        @DisplayName("Should calculate correct price for office delivery")
        void calculatePrice_OfficeDelivery_ReturnsCorrectPrice() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));
            BigDecimal weight = new BigDecimal("5.00");

            // Act
            // Formula: 5.00 + (5.00 × 2.00) + 0.00 = 15.00
            BigDecimal price = pricingService.calculatePrice(weight, true);

            // Assert
            assertEquals(new BigDecimal("15.00"), price);
            verify(pricingConfigRepository).findByActiveTrue();
        }

        @Test
        @DisplayName("Should calculate correct price for address delivery")
        void calculatePrice_AddressDelivery_ReturnsCorrectPrice() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));
            BigDecimal weight = new BigDecimal("5.00");

            // Act
            // Formula: 5.00 + (5.00 × 2.00) + 10.00 = 25.00
            BigDecimal price = pricingService.calculatePrice(weight, false);

            // Assert
            assertEquals(new BigDecimal("25.00"), price);
        }

        @Test
        @DisplayName("Should handle zero weight")
        void calculatePrice_ZeroWeight_ReturnsBasePrice() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));
            BigDecimal weight = BigDecimal.ZERO;

            // Act
            // Formula: 5.00 + (0 × 2.00) + 0 = 5.00
            BigDecimal price = pricingService.calculatePrice(weight, true);

            // Assert
            assertEquals(new BigDecimal("5.00"), price);
        }

        @Test
        @DisplayName("Should handle decimal weight correctly")
        void calculatePrice_DecimalWeight_RoundsCorrectly() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));
            BigDecimal weight = new BigDecimal("2.75");

            // Act
            // Formula: 5.00 + (2.75 × 2.00) + 0 = 5.00 + 5.50 = 10.50
            BigDecimal price = pricingService.calculatePrice(weight, true);

            // Assert
            assertEquals(new BigDecimal("10.50"), price);
        }

        @Test
        @DisplayName("Should throw exception when no active config exists")
        void calculatePrice_NoActiveConfig_ThrowsException() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.empty());

            // Act & Assert
            InvalidDataException exception = assertThrows(InvalidDataException.class,
                    () -> pricingService.calculatePrice(BigDecimal.ONE, true));

            assertTrue(exception.getMessage().contains("No active pricing configuration"));
        }
    }

    @Nested
    @DisplayName("getBasePrice Tests")
    class GetBasePriceTests {

        @Test
        @DisplayName("Should return base price from active config")
        void getBasePrice_ActiveConfigExists_ReturnsBasePrice() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));

            // Act
            BigDecimal basePrice = pricingService.getBasePrice();

            // Assert
            assertEquals(new BigDecimal("5.00"), basePrice);
        }
    }

    @Nested
    @DisplayName("getPricePerKg Tests")
    class GetPricePerKgTests {

        @Test
        @DisplayName("Should return price per kg from active config")
        void getPricePerKg_ActiveConfigExists_ReturnsPricePerKg() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));

            // Act
            BigDecimal pricePerKg = pricingService.getPricePerKg();

            // Assert
            assertEquals(new BigDecimal("2.00"), pricePerKg);
        }
    }

    @Nested
    @DisplayName("getAddressDeliveryFee Tests")
    class GetAddressDeliveryFeeTests {

        @Test
        @DisplayName("Should return address delivery fee from active config")
        void getAddressDeliveryFee_ActiveConfigExists_ReturnsAddressDeliveryFee() {
            // Arrange
            when(pricingConfigRepository.findByActiveTrue()).thenReturn(Optional.of(defaultConfig));

            // Act
            BigDecimal fee = pricingService.getAddressDeliveryFee();

            // Assert
            assertEquals(new BigDecimal("10.00"), fee);
        }
    }

    @Nested
    @DisplayName("updatePricingConfig Tests")
    class UpdatePricingConfigTests {

        @Test
        @DisplayName("Should update pricing config successfully")
        void updatePricingConfig_ValidData_UpdatesSuccessfully() {
            // Arrange
            BigDecimal newBasePrice = new BigDecimal("7.00");
            BigDecimal newPricePerKg = new BigDecimal("3.00");
            BigDecimal newAddressDeliveryFee = new BigDecimal("15.00");

            PricingConfig newConfig = new PricingConfig(newBasePrice, newPricePerKg, newAddressDeliveryFee);
            when(pricingConfigRepository.save(any(PricingConfig.class))).thenReturn(newConfig);

            // Act
            PricingConfig result = pricingService.updatePricingConfig(newBasePrice, newPricePerKg, newAddressDeliveryFee);

            // Assert
            assertEquals(newBasePrice, result.getBasePrice());
            assertEquals(newPricePerKg, result.getPricePerKg());
            assertEquals(newAddressDeliveryFee, result.getAddressDeliveryFee());
            verify(pricingConfigRepository).deactivateAll();
            verify(pricingConfigRepository).save(any(PricingConfig.class));
        }

        @Test
        @DisplayName("Should throw exception for negative base price")
        void updatePricingConfig_NegativeBasePrice_ThrowsException() {
            // Arrange
            BigDecimal negativePrice = new BigDecimal("-1.00");

            // Act & Assert
            assertThrows(InvalidDataException.class,
                    () -> pricingService.updatePricingConfig(negativePrice, BigDecimal.ONE, BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw exception for negative price per kg")
        void updatePricingConfig_NegativePricePerKg_ThrowsException() {
            // Arrange
            BigDecimal negativePrice = new BigDecimal("-1.00");

            // Act & Assert
            assertThrows(InvalidDataException.class,
                    () -> pricingService.updatePricingConfig(BigDecimal.ONE, negativePrice, BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw exception for null base price")
        void updatePricingConfig_NullBasePrice_ThrowsException() {
            // Act & Assert
            assertThrows(InvalidDataException.class,
                    () -> pricingService.updatePricingConfig(null, BigDecimal.ONE, BigDecimal.ONE));
        }
    }
}
