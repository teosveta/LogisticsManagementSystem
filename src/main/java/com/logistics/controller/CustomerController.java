package com.logistics.controller;

import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for customer management.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only customer-related HTTP endpoints.
 * - Dependency Inversion (DIP): Depends on CustomerService interface.
 *
 * Access: EMPLOYEE role only for management operations.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create customer", description = "Creates a new customer (Employee only)")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        logger.info("Creating customer for user ID: {}", request.getUserId());
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by ID (Employee only)")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        logger.debug("Fetching customer with ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Get customer by user ID", description = "Retrieves a customer by their user ID (Customer or Employee)")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        logger.debug("Fetching customer with user ID: {}", userId);
        CustomerResponse response = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all customers", description = "Retrieves all customers (Employee only)")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        logger.debug("Fetching all customers");
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update customer", description = "Updates an existing customer (Employee only)")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        logger.info("Updating customer with ID: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete customer", description = "Deletes a customer by ID (Employee only)")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
