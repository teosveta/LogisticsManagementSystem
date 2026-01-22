package com.logistics.service;

import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.User;
import com.logistics.model.enums.Role;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.UserRepository;
import com.logistics.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 * Tests customer CRUD operations in isolation.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private User user;
    private Customer customer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testcustomer");
        user.setEmail("customer@test.com");
        user.setRole(Role.CUSTOMER);

        customer = new Customer(user);
        customer.setId(1L);
        customer.setPhone("1234567890");
        customer.setAddress("123 Customer St");

        customerRequest = new CustomerRequest();
        customerRequest.setUserId(1L);
        customerRequest.setPhone("1234567890");
        customerRequest.setAddress("123 Customer St");
    }

    @Nested
    @DisplayName("createCustomer Tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer successfully")
        void createCustomer_ValidData_Success() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(customerRepository.existsByUserId(1L)).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // Act
            CustomerResponse response = customerService.createCustomer(customerRequest);

            // Assert
            assertNotNull(response);
            assertEquals("1234567890", response.getPhone());
            assertEquals("123 Customer St", response.getAddress());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void createCustomer_UserNotFound_ThrowsException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            customerRequest.setUserId(999L);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.createCustomer(customerRequest));
        }

        @Test
        @DisplayName("Should throw exception for duplicate customer")
        void createCustomer_DuplicateCustomer_ThrowsException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(customerRepository.existsByUserId(1L)).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                    () -> customerService.createCustomer(customerRequest));

            assertTrue(exception.getMessage().contains("userId"));
        }
    }

    @Nested
    @DisplayName("getCustomerById Tests")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should return customer when found")
        void getCustomerById_Exists_ReturnsCustomer() {
            // Arrange
            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

            // Act
            CustomerResponse response = customerService.getCustomerById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void getCustomerById_NotFound_ThrowsException() {
            // Arrange
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.getCustomerById(999L));
        }
    }

    @Nested
    @DisplayName("getCustomerByUsername Tests")
    class GetCustomerByUsernameTests {

        @Test
        @DisplayName("Should return customer when found by username")
        void getCustomerByUsername_Exists_ReturnsCustomer() {
            // Arrange
            when(customerRepository.findByUsername("testcustomer")).thenReturn(Optional.of(customer));

            // Act
            CustomerResponse response = customerService.getCustomerByUsername("testcustomer");

            // Assert
            assertNotNull(response);
            assertEquals("testcustomer", response.getUsername());
        }

        @Test
        @DisplayName("Should throw exception when username not found")
        void getCustomerByUsername_NotFound_ThrowsException() {
            // Arrange
            when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.getCustomerByUsername("unknown"));
        }
    }

    @Nested
    @DisplayName("getAllCustomers Tests")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Should return all customers")
        void getAllCustomers_ReturnsAllCustomers() {
            // Arrange
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("customer2");
            user2.setEmail("customer2@test.com");
            user2.setRole(Role.CUSTOMER);

            Customer customer2 = new Customer(user2);
            customer2.setId(2L);

            when(customerRepository.findAll()).thenReturn(Arrays.asList(customer, customer2));

            // Act
            List<CustomerResponse> responses = customerService.getAllCustomers();

            // Assert
            assertEquals(2, responses.size());
        }
    }

    @Nested
    @DisplayName("updateCustomer Tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer successfully")
        void updateCustomer_ValidData_Success() {
            // Arrange
            CustomerRequest updateRequest = new CustomerRequest();
            updateRequest.setUserId(1L);
            updateRequest.setPhone("9999999999");
            updateRequest.setAddress("999 New St");

            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            // Act
            CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

            // Assert
            assertNotNull(response);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void updateCustomer_NotFound_ThrowsException() {
            // Arrange
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.updateCustomer(999L, customerRequest));
        }
    }

    @Nested
    @DisplayName("deleteCustomer Tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should delete customer successfully")
        void deleteCustomer_Exists_Success() {
            // Arrange
            when(customerRepository.existsById(1L)).thenReturn(true);
            doNothing().when(customerRepository).deleteById(1L);

            // Act
            customerService.deleteCustomer(1L);

            // Assert
            verify(customerRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void deleteCustomer_NotFound_ThrowsException() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> customerService.deleteCustomer(999L));
        }
    }
}
