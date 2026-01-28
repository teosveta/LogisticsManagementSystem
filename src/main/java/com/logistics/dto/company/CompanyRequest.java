package com.logistics.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for company creation and update requests.
 */
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Registration number is required")
    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    private String registrationNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Pattern(regexp = "^$|^[0-9+\\s()\\-]{7,20}$", message = "Phone must contain only numbers and valid characters (+, -, spaces, parentheses)")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    // Default constructor
    public CompanyRequest() {
    }

    // Getters and Setters
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
}
