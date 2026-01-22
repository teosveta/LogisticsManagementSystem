package com.logistics.service;

import com.logistics.dto.auth.AuthResponse;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.RegisterRequest;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.Employee;
import com.logistics.model.entity.User;
import com.logistics.model.enums.Role;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.EmployeeRepository;
import com.logistics.repository.UserRepository;
import com.logistics.security.JwtTokenProvider;
import com.logistics.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests authentication and registration logic in isolation.
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private CustomerRepository customerRepository;
    private EmployeeRepository employeeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthenticationManager authenticationManager;
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Create mocks manually to avoid Mockito inline mock issues with final classes
        userRepository = mock(UserRepository.class);
        customerRepository = mock(CustomerRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);

        // Create a real JwtTokenProvider for testing
        jwtTokenProvider = new JwtTokenProvider(
            "TestSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong12345",
            3600000L
        );

        // Create service with dependencies
        authService = new AuthServiceImpl(
            userRepository, customerRepository, employeeRepository,
            passwordEncoder, jwtTokenProvider, authenticationManager
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CUSTOMER);

        registerRequest = new RegisterRequest("newuser", "new@example.com", "password123", Role.CUSTOMER);
        loginRequest = new LoginRequest("testuser", "password123");
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register customer successfully")
        void register_CustomerRole_Success() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertNotNull(response);
            assertEquals("newuser", response.getUsername());
            assertEquals("new@example.com", response.getEmail());
            assertEquals(Role.CUSTOMER, response.getRole());
            assertNotNull(response.getToken());
            assertTrue(response.getToken().contains(".")); // JWT format
            verify(customerRepository).save(any(Customer.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should register employee successfully")
        void register_EmployeeRole_Success() {
            // Arrange
            RegisterRequest employeeRequest = new RegisterRequest("newemployee", "emp@example.com", "password123", Role.EMPLOYEE);

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AuthResponse response = authService.register(employeeRequest);

            // Assert
            assertNotNull(response);
            assertEquals("newemployee", response.getUsername());
            assertEquals(Role.EMPLOYEE, response.getRole());
            assertNotNull(response.getToken());
            verify(employeeRepository).save(any(Employee.class));
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate username")
        void register_DuplicateUsername_ThrowsException() {
            // Arrange
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> authService.register(registerRequest));

            assertTrue(exception.getMessage().contains("username"));
        }

        @Test
        @DisplayName("Should throw exception for duplicate email")
        void register_DuplicateEmail_ThrowsException() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> authService.register(registerRequest));

            assertTrue(exception.getMessage().contains("email"));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void register_EncodesPassword() {
            // Arrange
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            authService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode("password123");
        }
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_ValidCredentials_Success() {
            // Arrange - use concrete UsernamePasswordAuthenticationToken instead of mocking
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken("testuser", "password123");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authToken);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals("testuser", response.getUsername());
            assertEquals("test@example.com", response.getEmail());
            assertEquals(Role.CUSTOMER, response.getRole());
            assertNotNull(response.getToken());
            assertTrue(response.getToken().contains(".")); // JWT format
        }

        @Test
        @DisplayName("Should throw exception for invalid credentials")
        void login_InvalidCredentials_ThrowsException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThrows(BadCredentialsException.class,
                    () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Should authenticate using AuthenticationManager")
        void login_UsesAuthenticationManager() {
            // Arrange - use concrete UsernamePasswordAuthenticationToken
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken("testuser", "password123");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authToken);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // Act
            authService.login(loginRequest);

            // Assert
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }
}
