package com.logistics.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This DTO only handles login credentials transfer.
 *
 * Contains username and password for authentication.
 */
public class LoginRequest {

    /**
     * Username for authentication.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Password for authentication.
     * Compared against BCrypt hash in database.
     */
    @NotBlank(message = "Password is required")
    private String password;

    // Default constructor
    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
