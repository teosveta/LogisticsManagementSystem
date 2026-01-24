package com.logistics.dto.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for customer creation and update requests.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only customer data transfer for input.
 */
public class CustomerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Pattern(regexp = "^$|^[0-9+\\s()\\-]{7,20}$", message = "Phone must contain only numbers and valid characters (+, -, spaces, parentheses)")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    // Default constructor
    public CustomerRequest() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
