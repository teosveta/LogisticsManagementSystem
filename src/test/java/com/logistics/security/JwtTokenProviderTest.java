package com.logistics.security;

import com.logistics.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests JWT token generation and validation.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Test secret - must be at least 256 bits (32 characters) for HS256
    private static final String TEST_SECRET = "TestSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong12345";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate valid token for customer")
        void generateToken_CustomerRole_ReturnsValidToken() {
            // Act
            String token = jwtTokenProvider.generateToken("testuser", Role.CUSTOMER);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.contains(".")); // JWT format: header.payload.signature
        }

        @Test
        @DisplayName("Should generate valid token for employee")
        void generateToken_EmployeeRole_ReturnsValidToken() {
            // Act
            String token = jwtTokenProvider.generateToken("employee", Role.EMPLOYEE);

            // Assert
            assertNotNull(token);
            assertTrue(jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateToken_DifferentUsers_ReturnsDifferentTokens() {
            // Act
            String token1 = jwtTokenProvider.generateToken("user1", Role.CUSTOMER);
            String token2 = jwtTokenProvider.generateToken("user2", Role.CUSTOMER);

            // Assert
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken Tests")
    class GetUsernameFromTokenTests {

        @Test
        @DisplayName("Should extract correct username from token")
        void getUsernameFromToken_ValidToken_ReturnsUsername() {
            // Arrange
            String username = "testuser";
            String token = jwtTokenProvider.generateToken(username, Role.CUSTOMER);

            // Act
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

            // Assert
            assertEquals(username, extractedUsername);
        }

        @Test
        @DisplayName("Should extract correct username for different users")
        void getUsernameFromToken_DifferentUsers_ReturnsCorrectUsernames() {
            // Arrange
            String token1 = jwtTokenProvider.generateToken("user1", Role.CUSTOMER);
            String token2 = jwtTokenProvider.generateToken("user2", Role.EMPLOYEE);

            // Act & Assert
            assertEquals("user1", jwtTokenProvider.getUsernameFromToken(token1));
            assertEquals("user2", jwtTokenProvider.getUsernameFromToken(token2));
        }
    }

    @Nested
    @DisplayName("getRoleFromToken Tests")
    class GetRoleFromTokenTests {

        @Test
        @DisplayName("Should extract correct role from token - CUSTOMER")
        void getRoleFromToken_CustomerRole_ReturnsCustomer() {
            // Arrange
            String token = jwtTokenProvider.generateToken("testuser", Role.CUSTOMER);

            // Act
            Role extractedRole = jwtTokenProvider.getRoleFromToken(token);

            // Assert
            assertEquals(Role.CUSTOMER, extractedRole);
        }

        @Test
        @DisplayName("Should extract correct role from token - EMPLOYEE")
        void getRoleFromToken_EmployeeRole_ReturnsEmployee() {
            // Arrange
            String token = jwtTokenProvider.generateToken("employee", Role.EMPLOYEE);

            // Act
            Role extractedRole = jwtTokenProvider.getRoleFromToken(token);

            // Assert
            assertEquals(Role.EMPLOYEE, extractedRole);
        }
    }

    @Nested
    @DisplayName("validateToken Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void validateToken_ValidToken_ReturnsTrue() {
            // Arrange
            String token = jwtTokenProvider.generateToken("testuser", Role.CUSTOMER);

            // Act
            boolean isValid = jwtTokenProvider.validateToken(token);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for malformed token")
        void validateToken_MalformedToken_ReturnsFalse() {
            // Arrange
            String malformedToken = "not.a.valid.jwt.token";

            // Act
            boolean isValid = jwtTokenProvider.validateToken(malformedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for empty token")
        void validateToken_EmptyToken_ReturnsFalse() {
            // Act
            boolean isValid = jwtTokenProvider.validateToken("");

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for null token")
        void validateToken_NullToken_ReturnsFalse() {
            // Act - null token should return false (handled gracefully)
            boolean isValid = jwtTokenProvider.validateToken(null);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for token with invalid signature")
        void validateToken_InvalidSignature_ReturnsFalse() {
            // Arrange
            String token = jwtTokenProvider.generateToken("testuser", Role.CUSTOMER);
            // Tamper with the signature
            String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

            // Act
            boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for expired token")
        void validateToken_ExpiredToken_ReturnsFalse() {
            // Arrange - create provider with very short expiration
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider(TEST_SECRET, 1L); // 1ms expiration
            String token = shortLivedProvider.generateToken("testuser", Role.CUSTOMER);

            // Wait for token to expire
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act
            boolean isValid = shortLivedProvider.validateToken(token);

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Token Round-Trip Tests")
    class TokenRoundTripTests {

        @Test
        @DisplayName("Should preserve username and role through token generation and extraction")
        void roundTrip_UsernameAndRole_PreservedCorrectly() {
            // Arrange
            String username = "roundtripuser";
            Role role = Role.EMPLOYEE;

            // Act
            String token = jwtTokenProvider.generateToken(username, role);
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
            Role extractedRole = jwtTokenProvider.getRoleFromToken(token);

            // Assert
            assertEquals(username, extractedUsername);
            assertEquals(role, extractedRole);
            assertTrue(jwtTokenProvider.validateToken(token));
        }
    }
}
