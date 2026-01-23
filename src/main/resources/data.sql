-- ========================================
-- LOGISTICS MANAGEMENT SYSTEM - SAMPLE DATA
-- ========================================
-- Run this after the application creates tables
-- Passwords are BCrypt hashed: "password123"
-- Uses INSERT IGNORE to avoid duplicate errors on restart
-- ========================================

-- ========================================
-- PRICING CONFIGURATION (Required for app to work)
-- ========================================
-- Only insert if no active pricing config exists
INSERT INTO pricing_config (base_price, price_per_kg, address_delivery_fee, active, created_at, updated_at)
SELECT 5.00, 2.00, 10.00, true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM pricing_config WHERE active = true);

-- ========================================
-- USERS (Password for all: "password123")
-- BCrypt hash: $2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG
-- ========================================
INSERT IGNORE INTO users (username, email, password, role, created_at, updated_at) VALUES
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
INSERT IGNORE INTO companies (name, registration_number, address, phone, email, created_at, updated_at) VALUES
('Swift Logistics Inc.', 'SL-2024-001', '100 Main Street, New York, NY 10001', '+1-555-100-0001', 'contact@swiftlogistics.com', NOW(), NOW());

-- ========================================
-- OFFICES (3 offices)
-- ========================================
INSERT IGNORE INTO offices (company_id, name, address, city, country, phone, created_at, updated_at) VALUES
(1, 'Downtown Office', '123 Business Ave', 'New York', 'USA', '+1-555-200-0001', NOW(), NOW()),
(1, 'Airport Branch', '456 Airport Road', 'New York', 'USA', '+1-555-200-0002', NOW(), NOW()),
(1, 'Uptown Center', '789 Uptown Blvd', 'New York', 'USA', '+1-555-200-0003', NOW(), NOW());

-- ========================================
-- EMPLOYEES (5: 2 couriers, 3 office staff)
-- ========================================
INSERT IGNORE INTO employees (user_id, company_id, employee_type, office_id, hire_date, salary, created_at, updated_at) VALUES
(1, 1, 'OFFICE_STAFF', 1, '2020-01-15', 55000.00, NOW(), NOW()),  -- admin at Downtown
(2, 1, 'COURIER', NULL, '2021-03-20', 42000.00, NOW(), NOW()),     -- john_courier (no office)
(3, 1, 'COURIER', NULL, '2021-06-10', 42000.00, NOW(), NOW()),     -- jane_courier (no office)
(4, 1, 'OFFICE_STAFF', 2, '2022-02-01', 48000.00, NOW(), NOW()),  -- mike_staff at Airport
(5, 1, 'OFFICE_STAFF', 3, '2022-09-15', 48000.00, NOW(), NOW());  -- sarah_staff at Uptown

-- ========================================
-- CUSTOMERS (5)
-- ========================================
INSERT IGNORE INTO customers (user_id, phone, address, created_at, updated_at) VALUES
(6, '+1-555-300-0001', '10 Apple Street, Brooklyn, NY', NOW(), NOW()),   -- alice
(7, '+1-555-300-0002', '20 Banana Ave, Queens, NY', NOW(), NOW()),       -- bob
(8, '+1-555-300-0003', '30 Cherry Lane, Manhattan, NY', NOW(), NOW()),   -- carol
(9, '+1-555-300-0004', '40 Date Drive, Bronx, NY', NOW(), NOW()),        -- david
(10, '+1-555-300-0005', '50 Elder Road, Staten Island, NY', NOW(), NOW()); -- emma

-- ========================================
-- SHIPMENTS (12 shipments with various statuses)
-- Price calculation: Base(5) + Weight*2 + DeliveryFee(0 for office, 10 for address)
-- Uses DATE_SUB(NOW(),...) for relative dates so revenue reports work with default date range
-- ========================================
INSERT IGNORE INTO shipments (sender_id, recipient_id, registered_by_id, delivery_address, delivery_office_id, weight, price, status, registered_at, delivered_at, updated_at) VALUES
-- DELIVERED shipments (for revenue calculation) - within last 30 days for default report range
(1, 2, 1, NULL, 1, 3.00, 11.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY)),
(2, 3, 1, '30 Cherry Lane, Manhattan, NY', NULL, 5.00, 25.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 4, 4, NULL, 2, 2.50, 10.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
(4, 5, 4, '50 Elder Road, Staten Island, NY', NULL, 10.00, 35.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 1, 5, NULL, 3, 1.00, 7.00, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- IN_TRANSIT shipments
(1, 3, 1, '30 Cherry Lane, Manhattan, NY', NULL, 4.00, 23.00, 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 4, 4, NULL, 1, 6.00, 17.00, 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 5, 5, '50 Elder Road, Staten Island, NY', NULL, 3.50, 22.00, 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- REGISTERED shipments (not yet picked up)
(4, 1, 1, NULL, 2, 2.00, 9.00, 'REGISTERED', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 2, 4, '20 Banana Ave, Queens, NY', NULL, 8.00, 31.00, 'REGISTERED', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 4, 5, NULL, 3, 1.50, 8.00, 'REGISTERED', NOW(), NULL, NOW()),

-- CANCELLED shipment
(2, 5, 1, '50 Elder Road, Staten Island, NY', NULL, 5.00, 25.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 7 DAY), NULL, DATE_SUB(NOW(), INTERVAL 6 DAY));
