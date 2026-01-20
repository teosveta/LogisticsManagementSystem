package com.logistics.controller;

import com.logistics.dto.employee.EmployeeRequest;
import com.logistics.dto.employee.EmployeeResponse;
import com.logistics.service.EmployeeService;
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
 * REST Controller for employee management.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only employee-related HTTP endpoints.
 * - Dependency Inversion (DIP): Depends on EmployeeService interface.
 *
 * Access: EMPLOYEE role only (employee management is administrative).
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create employee", description = "Creates a new employee (Employee only)")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        logger.info("Creating employee for user ID: {}", request.getUserId());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by ID (Employee only)")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        logger.debug("Fetching employee with ID: {}", id);
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all employees", description = "Retrieves all employees (Employee only)")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        logger.debug("Fetching all employees");
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update employee", description = "Updates an existing employee (Employee only)")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        logger.info("Updating employee with ID: {}", id);
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete employee", description = "Deletes an employee by ID (Employee only)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        logger.info("Deleting employee with ID: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
