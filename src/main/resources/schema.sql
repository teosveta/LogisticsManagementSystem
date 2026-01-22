-- ========================================
-- LOGISTICS MANAGEMENT SYSTEM - DATABASE SCHEMA
-- ========================================
-- This file is for reference only.
-- Hibernate will auto-create tables with ddl-auto=update
-- ========================================

-- Create database (run this manually if needed)
-- CREATE DATABASE IF NOT EXISTS logistics_db;
-- USE logistics_db;

-- ========================================
-- USERS TABLE
-- Stores authentication and authorization data
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
);

-- ========================================
-- COMPANIES TABLE
-- Stores logistics company information
-- ========================================
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    INDEX idx_companies_registration (registration_number)
);

-- ========================================
-- OFFICES TABLE
-- Stores company office/branch locations
-- ========================================
CREATE TABLE IF NOT EXISTS offices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_offices_company (company_id),
    INDEX idx_offices_city (city)
);

-- ========================================
-- EMPLOYEES TABLE
-- Stores employee information (couriers and office staff)
-- ========================================
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_id BIGINT NOT NULL,
    employee_type VARCHAR(20) NOT NULL,
    office_id BIGINT,
    hire_date DATE NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (office_id) REFERENCES offices(id),
    INDEX idx_employees_user (user_id),
    INDEX idx_employees_company (company_id),
    INDEX idx_employees_type (employee_type)
);

-- ========================================
-- CUSTOMERS TABLE
-- Stores customer information (senders/recipients)
-- ========================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_customers_user (user_id)
);

-- ========================================
-- SHIPMENTS TABLE
-- Stores shipment/delivery information
-- ========================================
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    registered_by_id BIGINT NOT NULL,
    delivery_address VARCHAR(255),
    delivery_office_id BIGINT,
    weight DECIMAL(10,2) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    registered_at DATETIME NOT NULL,
    delivered_at DATETIME,
    updated_at DATETIME,

    FOREIGN KEY (sender_id) REFERENCES customers(id),
    FOREIGN KEY (recipient_id) REFERENCES customers(id),
    FOREIGN KEY (registered_by_id) REFERENCES employees(id),
    FOREIGN KEY (delivery_office_id) REFERENCES offices(id),
    INDEX idx_shipments_sender (sender_id),
    INDEX idx_shipments_recipient (recipient_id),
    INDEX idx_shipments_employee (registered_by_id),
    INDEX idx_shipments_status (status),
    INDEX idx_shipments_delivered_at (delivered_at)
);

-- ========================================
-- PRICING CONFIGURATION TABLE
-- Stores configurable pricing values
-- Only one row should be active at a time
-- ========================================
CREATE TABLE IF NOT EXISTS pricing_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_price DECIMAL(10,2) NOT NULL,
    price_per_kg DECIMAL(10,2) NOT NULL,
    address_delivery_fee DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,

    INDEX idx_pricing_active (active)
);
