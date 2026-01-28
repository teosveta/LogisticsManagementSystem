package com.logistics.dto.shipment;

import com.logistics.model.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for shipment responses.
 *
 * Note: Both weight and price use BigDecimal for monetary/measurement precision.
 */
public class ShipmentResponse {

    private Long id;

    // Sender information
    private Long senderId;
    private String senderName;
    private String senderEmail;

    // Recipient information
    private Long recipientId;
    private String recipientName;
    private String recipientEmail;

    // Frontend-compatible aliases
    private String receiverName;  // Alias for recipientName

    // Employee who registered the shipment
    private Long registeredById;
    private String registeredByName;

    // Origin office (where shipment was registered)
    private Long originOfficeId;
    private String originOfficeName;

    // Delivery destination (either address or office)
    private String deliveryAddress;
    private Long deliveryOfficeId;
    private String deliveryOfficeName;
    private String deliveryDestination;

    // Frontend-compatible aliases and flags
    private String destinationOfficeName;  // Alias for deliveryOfficeName
    private boolean deliverToAddress;      // True if delivery is to address (not office)

    // Shipment details
    private BigDecimal weight;
    private BigDecimal price;
    private ShipmentStatus status;

    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ShipmentResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Long getRegisteredById() {
        return registeredById;
    }

    public void setRegisteredById(Long registeredById) {
        this.registeredById = registeredById;
    }

    public String getRegisteredByName() {
        return registeredByName;
    }

    public void setRegisteredByName(String registeredByName) {
        this.registeredByName = registeredByName;
    }

    public Long getOriginOfficeId() {
        return originOfficeId;
    }

    public void setOriginOfficeId(Long originOfficeId) {
        this.originOfficeId = originOfficeId;
    }

    public String getOriginOfficeName() {
        return originOfficeName;
    }

    public void setOriginOfficeName(String originOfficeName) {
        this.originOfficeName = originOfficeName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Long getDeliveryOfficeId() {
        return deliveryOfficeId;
    }

    public void setDeliveryOfficeId(Long deliveryOfficeId) {
        this.deliveryOfficeId = deliveryOfficeId;
    }

    public String getDeliveryOfficeName() {
        return deliveryOfficeName;
    }

    public void setDeliveryOfficeName(String deliveryOfficeName) {
        this.deliveryOfficeName = deliveryOfficeName;
    }

    public String getDeliveryDestination() {
        return deliveryDestination;
    }

    public void setDeliveryDestination(String deliveryDestination) {
        this.deliveryDestination = deliveryDestination;
    }

    public String getDestinationOfficeName() {
        return destinationOfficeName;
    }

    public void setDestinationOfficeName(String destinationOfficeName) {
        this.destinationOfficeName = destinationOfficeName;
    }

    public boolean isDeliverToAddress() {
        return deliverToAddress;
    }

    public void setDeliverToAddress(boolean deliverToAddress) {
        this.deliverToAddress = deliverToAddress;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
