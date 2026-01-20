package com.logistics.model.entity;

import com.logistics.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity representing a system user for authentication and authorization.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This entity only manages user authentication data.
 *   It doesn't contain business logic - that's in UserService.
 * - Open/Closed (OCP): New roles can be added to the Role enum without modifying this class.
 *
 * A User can be associated with either an Employee or a Customer, but not both.
 * The role field determines access permissions throughout the system.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user.
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for login - must be unique across the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email address - must be unique and valid format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt encrypted password.
     * Never stored or transmitted in plain text.
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    /**
     * User role determining access level.
     * EMPLOYEE or CUSTOMER - determines what actions are permitted.
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Timestamp when the user account was created.
     * Set automatically on creation.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     * Updated automatically on each modification.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One-to-one relationship with Employee.
     * Only set if role is EMPLOYEE.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee employee;

    /**
     * One-to-one relationship with Customer.
     * Only set if role is CUSTOMER.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customer customer;

    /**
     * JPA lifecycle callback - sets creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - updates timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public User() {
    }

    /**
     * Constructs a new User with essential fields.
     *
     * @param username the unique username
     * @param email    the user's email address
     * @param password the BCrypt encrypted password
     * @param role     the user's role (EMPLOYEE or CUSTOMER)
     */
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters with documentation

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
