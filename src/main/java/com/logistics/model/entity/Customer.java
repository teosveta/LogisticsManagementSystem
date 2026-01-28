package com.logistics.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer who sends or receives shipments.
 *
 * A Customer:
 * - Has exactly one User account (1:1 relationship)
 * - Can send shipments (as sender)
 * - Can receive shipments (as recipient)
 * - Can only view shipments where they are sender OR recipient
 */
@Entity
@Table(name = "customers")
public class Customer {

    /**
     * Unique identifier for the customer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with User.
     * Every customer must have a user account for authentication.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Customer's contact phone number.
     */
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    /**
     * Customer's default address for deliveries.
     */
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column
    private String address;

    /**
     * Timestamp when the customer was registered.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One-to-many relationship: shipments sent by this customer.
     */
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Shipment> sentShipments = new ArrayList<>();

    /**
     * One-to-many relationship: shipments received by this customer.
     */
    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private List<Shipment> receivedShipments = new ArrayList<>();

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
    public Customer() {
    }

    /**
     * Constructs a Customer with required fields.
     *
     * @param user the associated user account
     */
    public Customer(User user) {
        this.user = user;
    }

    /**
     * Constructs a Customer with all fields.
     *
     * @param user    the associated user account
     * @param phone   contact phone number
     * @param address default delivery address
     */
    public Customer(User user, String phone, String address) {
        this.user = user;
        this.phone = phone;
        this.address = address;
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

    public List<Shipment> getSentShipments() {
        return sentShipments;
    }

    public void setSentShipments(List<Shipment> sentShipments) {
        this.sentShipments = sentShipments;
    }

    public List<Shipment> getReceivedShipments() {
        return receivedShipments;
    }

    public void setReceivedShipments(List<Shipment> receivedShipments) {
        this.receivedShipments = receivedShipments;
    }

    /**
     * Gets all shipments associated with this customer (both sent and received).
     *
     * @return combined list of all shipments
     */
    public List<Shipment> getAllShipments() {
        List<Shipment> allShipments = new ArrayList<>();
        allShipments.addAll(sentShipments);
        allShipments.addAll(receivedShipments);
        return allShipments;
    }
}
