package com.logistics.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a company office location.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This entity only manages office location data.
 *   Operations involving offices are handled in OfficeService.
 * - Open/Closed (OCP): Can extend functionality through composition rather
 *   than modifying this class.
 *
 * An Office belongs to a Company and can be:
 * - A workplace for office staff employees
 * - A delivery destination for shipments
 */
@Entity
@Table(name = "offices")
public class Office {

    /**
     * Unique identifier for the office.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-one relationship with the parent company.
     * Every office must belong to a company.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Office name or identifier (e.g., "Downtown Branch", "Airport Office").
     */
    @NotBlank(message = "Office name is required")
    @Size(max = 100, message = "Office name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Full street address of the office.
     */
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(nullable = false)
    private String address;

    /**
     * City where the office is located.
     */
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * Country where the office is located.
     */
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Office contact phone number.
     */
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    /**
     * Timestamp when the office was registered.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One-to-many relationship with employees assigned to this office.
     * Typically office staff; couriers may or may not have an assigned office.
     */
    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    /**
     * One-to-many relationship with shipments delivered to this office.
     * When a shipment's delivery destination is an office (not an address).
     */
    @OneToMany(mappedBy = "deliveryOffice", fetch = FetchType.LAZY)
    private List<Shipment> shipments = new ArrayList<>();

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
    public Office() {
    }

    /**
     * Constructs an Office with required fields.
     *
     * @param company the parent company
     * @param name    the office name
     * @param address the street address
     * @param city    the city
     * @param country the country
     */
    public Office(Company company, String name, String address, String city, String country) {
        this.company = company;
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    /**
     * Returns the full address including city and country.
     *
     * @return formatted full address string
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s", address, city, country);
    }
}
