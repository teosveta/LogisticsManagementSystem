package com.logistics.model.entity;

import com.logistics.model.enums.EmployeeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an employee of the logistics company.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This entity only manages employee data.
 *   Employee-related operations are handled in EmployeeService.
 * - Open/Closed (OCP): New employee types can be added to EmployeeType enum
 *   without modifying this class structure.
 *
 * An Employee:
 * - Has exactly one User account (1:1 relationship)
 * - Belongs to exactly one Company
 * - May be assigned to an Office (required for OFFICE_STAFF)
 * - Can register shipments in the system
 *
 * IMPORTANT: Salary uses BigDecimal to prevent floating-point precision issues
 * with monetary calculations.
 */
@Entity
@Table(name = "employees")
public class Employee {

    /**
     * Unique identifier for the employee.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with User.
     * Every employee must have a user account for authentication.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Many-to-one relationship with Company.
     * Every employee works for exactly one company.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Type of employee: COURIER or OFFICE_STAFF.
     * Determines the employee's responsibilities.
     */
    @NotNull(message = "Employee type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 20)
    private EmployeeType employeeType;

    /**
     * Many-to-one relationship with Office.
     * Optional for couriers, typically required for office staff.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    /**
     * Date when the employee was hired.
     */
    @NotNull(message = "Hire date is required")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Employee's salary.
     * Uses BigDecimal for precise monetary calculations.
     * NEVER use double or float for money!
     */
    @NotNull(message = "Salary is required")
    @PositiveOrZero(message = "Salary must be positive or zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;

    /**
     * Timestamp when the employee record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One-to-many relationship with shipments.
     * Shipments registered by this employee.
     */
    @OneToMany(mappedBy = "registeredBy", fetch = FetchType.LAZY)
    private List<Shipment> registeredShipments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public Employee() {
    }

    /**
     * Constructs an Employee with required fields.
     *
     * @param user         the associated user account
     * @param company      the employer company
     * @param employeeType COURIER or OFFICE_STAFF
     * @param hireDate     date of hire
     * @param salary       monthly salary (BigDecimal for precision)
     */
    public Employee(User user, Company company, EmployeeType employeeType,
                    LocalDate hireDate, BigDecimal salary) {
        this.user = user;
        this.company = company;
        this.employeeType = employeeType;
        this.hireDate = hireDate;
        this.salary = salary;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Shipment> getRegisteredShipments() {
        return registeredShipments;
    }

    public void setRegisteredShipments(List<Shipment> registeredShipments) {
        this.registeredShipments = registeredShipments;
    }

    /**
     * Checks if this employee is a courier.
     *
     * @return true if employee type is COURIER
     */
    public boolean isCourier() {
        return EmployeeType.COURIER.equals(this.employeeType);
    }

    /**
     * Checks if this employee is office staff.
     *
     * @return true if employee type is OFFICE_STAFF
     */
    public boolean isOfficeStaff() {
        return EmployeeType.OFFICE_STAFF.equals(this.employeeType);
    }
}
