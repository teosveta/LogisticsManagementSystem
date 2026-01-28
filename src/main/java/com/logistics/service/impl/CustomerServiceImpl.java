package com.logistics.service.impl;

import com.logistics.dto.customer.CustomerRequest;
import com.logistics.dto.customer.CustomerResponse;
import com.logistics.exception.DuplicateResourceException;
import com.logistics.exception.ResourceNotFoundException;
import com.logistics.model.entity.Customer;
import com.logistics.model.entity.User;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.UserRepository;
import com.logistics.service.CustomerService;
import com.logistics.util.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        logger.info("Creating customer for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (customerRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Customer", "userId", request.getUserId());
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created with ID: {}", savedCustomer.getId());

        return EntityMapper.toCustomerResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        logger.debug("Fetching customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        return EntityMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUsername(String username) {
        logger.debug("Fetching customer with username: {}", username);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "username", username));

        return EntityMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(Long userId) {
        logger.debug("Fetching customer with user ID: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));

        return EntityMapper.toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        logger.debug("Fetching all customers");

        return customerRepository.findAll().stream()
                .map(EntityMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        logger.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer updatedCustomer = customerRepository.save(customer);
        logger.info("Customer updated with ID: {}", updatedCustomer.getId());

        return EntityMapper.toCustomerResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }

        customerRepository.deleteById(id);
        logger.info("Customer deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCustomerIdByUsername(String username) {
        logger.debug("Getting customer ID for username: {}", username);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "username", username));

        return customer.getId();
    }
}
