-- ========================================
-- LOGISTICS MANAGEMENT SYSTEM - SAMPLE DATA
-- ========================================
-- Run this after the application creates tables
-- Passwords are BCrypt hashed: "password123"
-- ========================================

-- ========================================
-- PRICING CONFIGURATION (Required for app to work)
-- ========================================
INSERT INTO pricing_config (base_price, price_per_kg, address_delivery_fee, active, created_at, updated_at) VALUES
(5.00, 2.00, 10.00, true, NOW(), NOW());

-- ========================================
-- USERS (Password for all: "password123")
-- BCrypt hash: $2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG
-- ========================================
INSERT INTO users (username, email, password, role, created_at, updated_at) VALUES
-- Employees (5)
('admin', 'admin@logistics.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'EMPLOYEE', NOW(), NOW()),
('john_courier', 'john@logistics.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'EMPLOYEE', NOW(), NOW()),
('jane_courier', 'jane@logistics.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'EMPLOYEE', NOW(), NOW()),
('mike_staff', 'mike@logistics.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'EMPLOYEE', NOW(), NOW()),
('sarah_staff', 'sarah@logistics.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'EMPLOYEE', NOW(), NOW()),
-- Customers (5)
('customer_alice', 'alice@email.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'CUSTOMER', NOW(), NOW()),
('customer_bob', 'bob@email.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'CUSTOMER', NOW(), NOW()),
('customer_carol', 'carol@email.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'CUSTOMER', NOW(), NOW()),
('customer_david', 'david@email.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'CUSTOMER', NOW(), NOW()),
('customer_emma', 'emma@email.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'CUSTOMER', NOW(), NOW());

-- ========================================
-- COMPANY (1 main logistics company)
-- ========================================
INSERT INTO companies (name, registration_number, address, phone, email, created_at, updated_at) VALUES
('Swift Logistics Inc.', 'SL-2024-001', '100 Main Street, New York, NY 10001', '+1-555-100-0001', 'contact@swiftlogistics.com', NOW(), NOW());

-- ========================================
-- OFFICES (3 offices)
-- ========================================
INSERT INTO offices (company_id, name, address, city, country, phone, created_at, updated_at) VALUES
(1, 'Downtown Office', '123 Business Ave', 'New York', 'USA', '+1-555-200-0001', NOW(), NOW()),
(1, 'Airport Branch', '456 Airport Road', 'New York', 'USA', '+1-555-200-0002', NOW(), NOW()),
(1, 'Uptown Center', '789 Uptown Blvd', 'New York', 'USA', '+1-555-200-0003', NOW(), NOW());

-- ========================================
-- EMPLOYEES (5: 2 couriers, 3 office staff)
-- ========================================
INSERT INTO employees (user_id, company_id, employee_type, office_id, hire_date, salary, created_at, updated_at) VALUES
(1, 1, 'OFFICE_STAFF', 1, '2020-01-15', 55000.00, NOW(), NOW()),  -- admin at Downtown
(2, 1, 'COURIER', NULL, '2021-03-20', 42000.00, NOW(), NOW()),     -- john_courier (no office)
(3, 1, 'COURIER', NULL, '2021-06-10', 42000.00, NOW(), NOW()),     -- jane_courier (no office)
(4, 1, 'OFFICE_STAFF', 2, '2022-02-01', 48000.00, NOW(), NOW()),  -- mike_staff at Airport
(5, 1, 'OFFICE_STAFF', 3, '2022-09-15', 48000.00, NOW(), NOW());  -- sarah_staff at Uptown

-- ========================================
-- CUSTOMERS (5)
-- ========================================
INSERT INTO customers (user_id, phone, address, created_at, updated_at) VALUES
(6, '+1-555-300-0001', '10 Apple Street, Brooklyn, NY', NOW(), NOW()),   -- alice
(7, '+1-555-300-0002', '20 Banana Ave, Queens, NY', NOW(), NOW()),       -- bob
(8, '+1-555-300-0003', '30 Cherry Lane, Manhattan, NY', NOW(), NOW()),   -- carol
(9, '+1-555-300-0004', '40 Date Drive, Bronx, NY', NOW(), NOW()),        -- david
(10, '+1-555-300-0005', '50 Elder Road, Staten Island, NY', NOW(), NOW()); -- emma

-- ========================================
-- SHIPMENTS (12 shipments with various statuses)
-- Price calculation: Base(5) + Weight*2 + DeliveryFee(0 for office, 10 for address)
-- ========================================
INSERT INTO shipments (sender_id, recipient_id, registered_by_id, delivery_address, delivery_office_id, weight, price, status, registered_at, delivered_at, updated_at) VALUES
-- DELIVERED shipments (for revenue calculation)
(1, 2, 1, NULL, 1, 3.00, 11.00, 'DELIVERED', '2024-01-10 10:00:00', '2024-01-12 14:30:00', '2024-01-12 14:30:00'),          -- Alice to Bob, office delivery
(2, 3, 1, '30 Cherry Lane, Manhattan, NY', NULL, 5.00, 25.00, 'DELIVERED', '2024-01-11 09:00:00', '2024-01-13 11:00:00', '2024-01-13 11:00:00'),  -- Bob to Carol, address
(3, 4, 4, NULL, 2, 2.50, 10.00, 'DELIVERED', '2024-01-12 11:00:00', '2024-01-14 16:00:00', '2024-01-14 16:00:00'),          -- Carol to David, office
(4, 5, 4, '50 Elder Road, Staten Island, NY', NULL, 10.00, 35.00, 'DELIVERED', '2024-01-13 14:00:00', '2024-01-15 09:00:00', '2024-01-15 09:00:00'),  -- David to Emma, address
(5, 1, 5, NULL, 3, 1.00, 7.00, 'DELIVERED', '2024-01-14 08:00:00', '2024-01-16 10:00:00', '2024-01-16 10:00:00'),           -- Emma to Alice, office

-- IN_TRANSIT shipments
(1, 3, 1, '30 Cherry Lane, Manhattan, NY', NULL, 4.00, 23.00, 'IN_TRANSIT', '2024-01-17 09:00:00', NULL, '2024-01-17 15:00:00'),  -- Alice to Carol, address
(2, 4, 4, NULL, 1, 6.00, 17.00, 'IN_TRANSIT', '2024-01-17 10:00:00', NULL, '2024-01-17 14:00:00'),                              -- Bob to David, office
(3, 5, 5, '50 Elder Road, Staten Island, NY', NULL, 3.50, 22.00, 'IN_TRANSIT', '2024-01-18 11:00:00', NULL, '2024-01-18 11:00:00'),  -- Carol to Emma, address

-- REGISTERED shipments (not yet picked up)
(4, 1, 1, NULL, 2, 2.00, 9.00, 'REGISTERED', '2024-01-19 08:00:00', NULL, '2024-01-19 08:00:00'),                               -- David to Alice, office
(5, 2, 4, '20 Banana Ave, Queens, NY', NULL, 8.00, 31.00, 'REGISTERED', '2024-01-19 09:00:00', NULL, '2024-01-19 09:00:00'),    -- Emma to Bob, address
(1, 4, 5, NULL, 3, 1.50, 8.00, 'REGISTERED', '2024-01-19 10:00:00', NULL, '2024-01-19 10:00:00'),                               -- Alice to David, office

-- CANCELLED shipment
(2, 5, 1, '50 Elder Road, Staten Island, NY', NULL, 5.00, 25.00, 'CANCELLED', '2024-01-15 10:00:00', NULL, '2024-01-15 16:00:00');  -- Bob to Emma, cancelled
