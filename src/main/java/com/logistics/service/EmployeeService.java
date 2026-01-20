package com.logistics.service;

import com.logistics.dto.employee.EmployeeRequest;
import com.logistics.dto.employee.EmployeeResponse;

import java.util.List;

/**
 * Service interface for Employee operations.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Only handles employee-related business logic.
 * - Interface Segregation (ISP): Contains only methods relevant to employees.
 * - Dependency Inversion (DIP): Controllers depend on this interface.
 */
public interface EmployeeService {

    /**
     * Creates a new employee.
     *
     * @param request the employee data
     * @return the created employee response
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Retrieves an employee by ID.
     *
     * @param id the employee ID
     * @return the employee response
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Retrieves an employee by username.
     *
     * @param username the username
     * @return the employee response
     */
    EmployeeResponse getEmployeeByUsername(String username);

    /**
     * Retrieves all employees.
     *
     * @return list of all employees
     */
    List<EmployeeResponse> getAllEmployees();

    /**
     * Updates an existing employee.
     *
     * @param id      the employee ID
     * @param request the updated employee data
     * @return the updated employee response
     */
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    /**
     * Deletes an employee by ID.
     *
     * @param id the employee ID
     */
    void deleteEmployee(Long id);
}
