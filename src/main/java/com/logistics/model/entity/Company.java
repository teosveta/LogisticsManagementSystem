package com.logistics.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a logistics company.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This entity only manages company data.
 *   Business operations involving companies are handled in CompanyService.
 * - Open/Closed (OCP): Additional company attributes can be added via inheritance
 *   or new fields without changing existing functionality.
 *
 * A Company has multiple Offices and Employees.
 * In this system, typically there's one main logistics company.
 */
@Entity
@Table(name = "companies")
public class Company {

    /**
     * Unique identifier for the company.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Official company name.
     */
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Official registration/business number.
     * Must be unique to identify the company legally.
     */
    @NotBlank(message = "Registration number is required")
    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    /**
     * Company headquarters address.
     */
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(nullable = false)
    private String address;

    /**
     * Company contact phone number.
     */
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    /**
     * Company contact email.
     */
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(length = 100)
    private String email;

    /**
     * Timestamp when the company was registered in the system.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One-to-many relationship with offices.
     * A company can have multiple office locations.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Office> offices = new ArrayList<>();

    /**
     * One-to-many relationship with employees.
     * A company employs multiple staff members.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

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
    public Company() {
    }

    /**
     * Constructs a Company with required fields.
     *
     * @param name               the company name
     * @param registrationNumber the official registration number
     * @param address            the company address
     */
    public Company(String name, String registrationNumber, String address) {
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.address = address;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    /**
     * Adds an office to this company.
     * Maintains bidirectional relationship.
     *
     * @param office the office to add
     */
    public void addOffice(Office office) {
        offices.add(office);
        office.setCompany(this);
    }

    /**
     * Adds an employee to this company.
     * Maintains bidirectional relationship.
     *
     * @param employee the employee to add
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setCompany(this);
    }
}
