package com.logistics.service;

import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;

import java.util.List;

/**
 * Service interface for Customer operations.
 */
public interface CustomerService {

    /**
     * Creates a new customer.
     *
     * @param request the customer data
     * @return the created customer response
     */
    CustomerResponse createCustomer(CustomerRequest request);

    /**
     * Retrieves a customer by ID.
     *
     * @param id the customer ID
     * @return the customer response
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * Retrieves a customer by username.
     *
     * @param username the username
     * @return the customer response
     */
    CustomerResponse getCustomerByUsername(String username);

    /**
     * Retrieves a customer by their associated user ID.
     *
     * @param userId the user's ID
     * @return the customer response
     */
    CustomerResponse getCustomerByUserId(Long userId);

    /**
     * Retrieves all customers.
     *
     * @return list of all customers
     */
    List<CustomerResponse> getAllCustomers();

    /**
     * Updates an existing customer.
     *
     * @param id      the customer ID
     * @param request the updated customer data
     * @return the updated customer response
     */
    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer ID
     */
    void deleteCustomer(Long id);

    /**
     * Gets the customer ID for a given username.
     * Used by controllers to resolve customer identity from authentication.
     *
     * This method supports the Dependency Inversion Principle (DIP) by allowing
     * controllers to depend on the service interface rather than directly
     * accessing the repository layer.
     *
     * @param username the username from authentication
     * @return the customer ID
     * @throws com.logistics.exception.ResourceNotFoundException if customer not found
     */
    Long getCustomerIdByUsername(String username);
}
