package com.logistics.service;

import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;

import java.util.List;

/**
 * Service interface for Customer operations.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles customer-related business logic.
 * - Interface Segregation (ISP): Contains only methods relevant to customers.
 * - Dependency Inversion (DIP): Controllers depend on this interface.
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
}
