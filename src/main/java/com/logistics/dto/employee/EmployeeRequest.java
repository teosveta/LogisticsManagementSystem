package com.logistics.dto.employee;

import com.logistics.model.enums.EmployeeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for employee creation and update requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only employee data transfer for input.
 *
 * Note: Salary uses BigDecimal for monetary precision.
 */
public class EmployeeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Employee type is required")
    private EmployeeType employeeType;

    /**
     * Office ID - optional for couriers, typically required for office staff.
     */
    private Long officeId;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    /**
     * Salary as BigDecimal for precise monetary calculations.
     * NEVER use double or float for money!
     */
    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    // Default constructor
    public EmployeeRequest() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
}
