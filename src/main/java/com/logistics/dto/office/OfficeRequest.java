package com.logistics.dto.office;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for office creation and update requests.
 */
public class OfficeRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Office name is required")
    @Size(max = 100, message = "Office name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Pattern(regexp = "^$|^[0-9+\\s()\\-]{7,20}$", message = "Phone must contain only numbers and valid characters (+, -, spaces, parentheses)")
    private String phone;

    // Default constructor
    public OfficeRequest() {
    }

    // Getters and Setters
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
