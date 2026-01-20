package com.logistics.dto.auth;

import com.logistics.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This DTO only handles registration data transfer.
 *   Validation is declarative via annotations; business logic is in AuthService.
 *
 * Contains all fields required to create a new user account.
 */
public class RegisterRequest {

    /**
     * Desired username for the new account.
     * Must be unique across the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Email address for the new account.
     * Must be unique and valid format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password for the new account.
     * Will be encrypted before storage.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Role for the new account (EMPLOYEE or CUSTOMER).
     * Determines access permissions.
     */
    @NotNull(message = "Role is required")
    private Role role;

    // Default constructor
    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
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
}
