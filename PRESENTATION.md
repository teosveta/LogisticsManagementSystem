# Logistics Management System - Presentation Guide

## Complete Study Guide and Presentation Script

This document teaches you how to present your Logistics Management System to your professor. Use it as both a presentation script and a study guide for defending your technical decisions.

---

## Table of Contents

1. [Project Introduction (2-3 minutes)](#section-1-project-introduction)
2. [Architecture Overview (5 minutes)](#section-2-architecture-overview)
3. [SOLID Principles Implementation (10 minutes)](#section-3-solid-principles-implementation)
4. [Database Design (5 minutes)](#section-4-database-design)
5. [Security Implementation (5 minutes)](#section-5-security-implementation)
6. [Pricing System (5 minutes)](#section-6-pricing-system)
7. [Validation Strategy (4 minutes)](#section-7-validation-strategy)
8. [Access Control Logic (3 minutes)](#section-8-access-control-logic)
9. [Report Generation (3 minutes)](#section-9-report-generation)
10. [Testing Approach (3 minutes)](#section-10-testing-approach)
11. [Code Quality Highlights (2 minutes)](#section-11-code-quality-highlights)
12. [Live Demonstration Script](#section-12-live-demonstration-script)
13. [Common Questions & Answers](#section-13-common-questions--answers)
14. [Project Strengths to Highlight](#section-14-project-strengths-to-highlight)
15. [Technical Decisions Justification](#section-15-technical-decisions-justification)

---

## SECTION 1: Project Introduction

**Duration: 2-3 minutes**

### What to Say

> "I've built a REST API for a logistics company management system using Spring Boot 3.2 and Java 17. The system handles the complete lifecycle of shipment management - from registration and pricing calculation to tracking and delivery confirmation.
>
> The key business features include:
> - User authentication with role-based access control (Employees and Customers)
> - Company and office management
> - Shipment registration with automatic price calculation
> - Shipment status tracking through its lifecycle
> - Comprehensive reporting including revenue analysis
>
> I chose this technology stack because Spring Boot is the industry standard for enterprise Java applications, and Java 17 is the current Long-Term Support version with modern language features."

### Project Structure to Show

```
src/main/java/com/logistics/
├── config/                    # Security and OpenAPI configuration
│   ├── SecurityConfig.java
│   └── OpenApiConfig.java
├── controller/                # 8 REST controllers
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── OfficeController.java
│   ├── PricingController.java
│   ├── ShipmentController.java
│   └── ReportController.java
├── dto/                       # Request/Response DTOs
├── exception/                 # Global exception handling
├── model/
│   ├── entity/               # 7 JPA entities
│   └── enums/                # 3 enums (Role, ShipmentStatus, EmployeeType)
├── repository/               # 7 Spring Data JPA repositories
├── security/                 # JWT authentication
├── service/
│   ├── interfaces           # 8 service interfaces
│   └── impl/                # 8 service implementations
└── util/
    └── EntityMapper.java    # Entity to DTO conversion
```

### Key Dependencies (pom.xml)

**Show this excerpt:**

```xml
<properties>
    <java.version>17</java.version>
    <jjwt.version>0.12.3</jjwt.version>
</properties>

<dependencies>
    <!-- Spring Boot Web - REST API support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA - Database ORM with Hibernate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security - Authentication and Authorization -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Spring Validation - DTO validation (@Valid, @NotNull, etc.) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JWT Dependencies - Token-based authentication -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt.version}</version>
    </dependency>

    <!-- MySQL Connector - Database driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
</dependencies>
```

### Key Talking Points

- Java 17 is the current LTS (Long-Term Support) version with performance improvements
- Spring Boot 3.2 provides rapid development with production-ready features
- MySQL 8.0 for ACID-compliant relational data storage
- JWT for stateless, scalable authentication
- Spring Data JPA with Hibernate for ORM

---

## SECTION 2: Architecture Overview

**Duration: 5 minutes**

### What to Explain

> "The system follows a classic three-layer architecture that enforces separation of concerns. Each layer has a specific responsibility, and layers only communicate through well-defined interfaces."

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│              (Controllers - REST Endpoints)                  │
│   Handles: HTTP requests, validation, response formatting    │
│   Example: ShipmentController, AuthController                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                      │
│                (Services - Core Logic)                       │
│   Handles: Business rules, orchestration, transactions       │
│   Example: ShipmentServiceImpl, PricingServiceImpl           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                         │
│              (Repositories - Database Access)                │
│   Handles: CRUD operations, custom queries                   │
│   Example: ShipmentRepository, CustomerRepository            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
│                      (MySQL 8.0)                             │
│   Stores: Persistent data with ACID compliance               │
└─────────────────────────────────────────────────────────────┘
```

### Code Example: ShipmentController (Presentation Layer)

**File: `controller/ShipmentController.java` (lines 38-52)**

```java
/**
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): Handles only shipment-related HTTP endpoints.
 *   Does not contain business logic - delegates to ShipmentService.
 * - Dependency Inversion (DIP): Depends on service interfaces (ShipmentService,
 *   CustomerService), not on concrete implementations or repositories.
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;    // INTERFACE, not implementation!
    private final CustomerService customerService;    // INTERFACE, not implementation!

    public ShipmentController(ShipmentService shipmentService, CustomerService customerService) {
        this.shipmentService = shipmentService;
        this.customerService = customerService;
    }
```

### What to Say About This Code

> "Notice that the controller injects `ShipmentService` and `CustomerService` - these are **interfaces**, not concrete implementations. The controller doesn't know or care about `ShipmentServiceImpl`. This is the **Dependency Inversion Principle** in action.
>
> The controller only handles HTTP concerns - parsing requests, calling the service, and formatting responses. All business logic is delegated to the service layer. This is the **Single Responsibility Principle**."

### Code Example: Service Method Delegation

**File: `controller/ShipmentController.java` (lines 58-70)**

```java
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")  // Security annotation - only employees can register
public ResponseEntity<ShipmentResponse> registerShipment(
        @Valid @RequestBody ShipmentRequest request,  // @Valid triggers DTO validation
        Authentication authentication) {

    String employeeUsername = authentication.getName();

    // Controller delegates ALL business logic to the service
    ShipmentResponse response = shipmentService.registerShipment(request, employeeUsername);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Professor Might Ask

**Q: "Why use interfaces instead of concrete classes?"**

**A:** "Using interfaces provides three key benefits:
1. **Testability**: I can easily mock the interface in unit tests without needing the real implementation
2. **Flexibility**: I can swap implementations without changing the controller code (e.g., different pricing strategies)
3. **Loose coupling**: The controller doesn't depend on implementation details, only on the contract defined by the interface"

**Q: "What happens if you inject the repository directly into the controller?"**

**A:** "That would violate the layered architecture and create several problems:
1. Business logic would leak into the controller
2. The controller would have too many responsibilities (SRP violation)
3. Transaction management becomes harder because `@Transactional` belongs on services
4. Testing becomes more difficult because you can't easily mock database operations"

---

## SECTION 3: SOLID Principles Implementation

**Duration: 10 minutes** (Most important section!)

### Overview

> "I implemented all five SOLID principles throughout the codebase. Let me demonstrate each one with specific code examples."

---

### A. Single Responsibility Principle (SRP)

**Definition:** Each class should have one, and only one, reason to change.

#### Code Example: PricingServiceImpl

**File: `service/impl/PricingServiceImpl.java` (lines 15-31)**

```java
/**
 * Implementation of PricingService that loads configuration from the DATABASE.
 *
 * SOLID Principles Applied:
 * - Single Responsibility (SRP): This class ONLY handles pricing calculations.
 *   It doesn't persist data or validate shipments - those are other services' jobs.
 * - Open/Closed (OCP): Configuration is stored in database, so pricing
 *   can be changed without modifying code or redeploying.
 * - Dependency Inversion (DIP): Depends on PricingConfigRepository interface.
 *
 * Pricing Formula:
 * Total = Base Price + (Weight × Price per kg) + Delivery Type Fee
 */
@Service
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;

    public PricingServiceImpl(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
    }
```

#### What to Say

> "PricingService has **one responsibility**: calculate shipment prices. It does NOT:
> - Create shipments - that's ShipmentService's job
> - Save to database - that's the repository's job
> - Handle HTTP requests - that's the controller's job
>
> If pricing rules change, I only modify this one class. If shipment creation logic changes, PricingService remains untouched. This isolation makes the code easier to test and maintain."

#### More SRP Examples

| Class | Single Responsibility |
|-------|----------------------|
| `ShipmentController` | HTTP endpoint handling for shipments |
| `ShipmentServiceImpl` | Shipment business logic |
| `ShipmentRepository` | Database operations for shipments |
| `GlobalExceptionHandler` | Exception-to-response conversion |
| `JwtTokenProvider` | JWT token generation and validation |
| `EntityMapper` | Entity to DTO conversion |

---

### B. Open/Closed Principle (OCP)

**Definition:** Software entities should be open for extension but closed for modification.

#### Code Example: Database-Driven Pricing

**File: `service/impl/PricingServiceImpl.java` (lines 64-88)**

```java
@Override
@Transactional(readOnly = true)
public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
    // Configuration comes from DATABASE, not hardcoded values
    PricingConfig config = getActiveConfig();

    // Step 1: Start with base price
    BigDecimal total = config.getBasePrice();

    // Step 2: Add weight-based cost (weight × price per kg)
    BigDecimal weightCost = weight.multiply(config.getPricePerKg());
    total = total.add(weightCost);

    // Step 3: Add delivery type fee (0 for office, addressDeliveryFee for address)
    if (!isOfficeDelivery) {
        total = total.add(config.getAddressDeliveryFee());
    }

    // Round to 2 decimal places for currency
    total = total.setScale(2, RoundingMode.HALF_UP);

    return total;
}
```

#### What to Say

> "The pricing system is **open for extension** - I can change pricing at any time by updating the database, without touching the code. It's **closed for modification** - the calculation logic doesn't need to change when pricing values change.
>
> If I had hardcoded prices like `basePrice = 5.00`, I would need to modify the code and redeploy every time prices change. With database configuration, I just update a row and the new prices take effect immediately."

#### Enum Extensibility

**File: `model/enums/ShipmentStatus.java`**

```java
public enum ShipmentStatus {
    REGISTERED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
    // Can add new statuses (e.g., PROCESSING, RETURNED) without changing existing code
}
```

> "If we need new shipment statuses like PROCESSING or RETURNED, we just add them to the enum. Existing status handling code continues to work unchanged."

---

### C. Liskov Substitution Principle (LSP)

**Definition:** Objects of a superclass should be replaceable with objects of its subclasses without affecting correctness.

#### Design Decision: Composition Over Inheritance

> "I deliberately avoided problematic inheritance hierarchies. Instead of having Employee and Customer extend User, I used **composition**."

**File: `model/entity/Employee.java` (lines 20-25)**

```java
@Entity
public class Employee {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // COMPOSITION - Employee HAS a User, doesn't extend User

    private String employeeType;
    private BigDecimal salary;
    // ...
}
```

**File: `model/entity/Customer.java` (lines 20-25)**

```java
@Entity
public class Customer {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // COMPOSITION - Customer HAS a User, doesn't extend User

    private String phone;
    private String address;
    // ...
}
```

#### What to Say

> "I avoided the classic inheritance pitfall where Employee and Customer would extend User. This would cause LSP violations because:
> 1. Not all User operations make sense for both subtypes
> 2. Employee and Customer have very different additional fields
> 3. A User could be both an Employee AND a Customer
>
> Instead, I used composition: Employee and Customer both **reference** a User entity. This is more flexible and avoids LSP violations."

#### Interface Implementation LSP

```java
// Any implementation of ShipmentService can be substituted
public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse getShipmentById(Long id);
    List<ShipmentResponse> getAllShipments();
    // ...
}

// This implementation can be swapped with another without breaking controllers
public class ShipmentServiceImpl implements ShipmentService {
    // ...
}
```

---

### D. Interface Segregation Principle (ISP)

**Definition:** No client should be forced to depend on methods it does not use.

#### Code Example: Focused Service Interfaces

**File: `service/PricingService.java`**

```java
public interface PricingService {
    BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery);
    BigDecimal getBasePrice();
    BigDecimal getPricePerKg();
    BigDecimal getAddressDeliveryFee();
    // Only 4 methods - all related to pricing. No bloat!
}
```

**File: `service/ShipmentService.java`**

```java
public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse getShipmentById(Long id);
    List<ShipmentResponse> getAllShipments();
    List<ShipmentResponse> getShipmentsByCustomerId(Long customerId);
    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);
    // Only shipment-related methods
}
```

#### What to Say

> "Each service interface contains only methods related to its domain. PricingService has pricing methods. ShipmentService has shipment methods. There's no 'GodService' with 50 unrelated methods.
>
> This means when a controller needs pricing, it only depends on PricingService - it's not forced to know about shipments, customers, or anything else. This is Interface Segregation in action."

#### Contrast with Anti-Pattern

```java
// BAD - Fat Interface (ISP violation)
public interface LogisticsService {
    void calculatePrice();
    void registerShipment();
    void createCustomer();
    void createEmployee();
    void generateReport();
    void sendEmail();
    // 50 more unrelated methods...
}

// GOOD - Segregated Interfaces (what I implemented)
public interface PricingService { /* pricing methods */ }
public interface ShipmentService { /* shipment methods */ }
public interface CustomerService { /* customer methods */ }
public interface ReportService { /* report methods */ }
```

---

### E. Dependency Inversion Principle (DIP)

**Definition:** High-level modules should not depend on low-level modules. Both should depend on abstractions.

#### Code Example: ShipmentServiceImpl

**File: `service/impl/ShipmentServiceImpl.java` (lines 55-79)**

```java
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;  // Interface
    private final CustomerRepository customerRepository;   // Interface
    private final EmployeeRepository employeeRepository;   // Interface
    private final OfficeRepository officeRepository;       // Interface

    /**
     * PricingService injected via constructor.
     * This is Dependency Inversion (DIP) in action:
     * - We depend on the PricingService INTERFACE
     * - We don't know or care about the implementation
     * - This allows swapping pricing strategies easily
     */
    private final PricingService pricingService;  // Interface, NOT PricingServiceImpl!

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               CustomerRepository customerRepository,
                               EmployeeRepository employeeRepository,
                               OfficeRepository officeRepository,
                               PricingService pricingService) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.officeRepository = officeRepository;
        this.pricingService = pricingService;  // Spring injects the actual implementation
    }
```

#### What to Say

> "ShipmentServiceImpl depends on **PricingService** the interface, not PricingServiceImpl the concrete class. Spring resolves which implementation to inject at runtime.
>
> This means:
> 1. I can swap pricing implementations without changing ShipmentServiceImpl
> 2. In tests, I can mock PricingService without needing a real database
> 3. The high-level business logic (shipment registration) doesn't depend on low-level details (how pricing is calculated)"

#### Dependency Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    HIGH-LEVEL MODULE                         │
│                  ShipmentController                          │
│           (depends on ShipmentService interface)             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      ABSTRACTION                             │
│                  ShipmentService (interface)                 │
└─────────────────────────────────────────────────────────────┘
                              ↑
┌─────────────────────────────────────────────────────────────┐
│                    LOW-LEVEL MODULE                          │
│                  ShipmentServiceImpl                         │
│           (implements ShipmentService interface)             │
└─────────────────────────────────────────────────────────────┘
```

#### Professor Might Ask

**Q: "What if you inject the concrete class directly?"**

**A:** "If I injected `ShipmentServiceImpl` directly instead of the `ShipmentService` interface:
1. **Testing becomes harder** - I can't easily mock the concrete class
2. **Coupling increases** - The controller now depends on implementation details
3. **Flexibility decreases** - I can't swap implementations without code changes
4. **Violates DIP** - High-level module depends on low-level module"

---

## SECTION 4: Database Design

**Duration: 5 minutes**

### Entity Relationship Diagram

```
┌──────────────┐         ┌─────────────────┐
│    users     │         │   customers     │
│   (auth)     │1───────1│  (phone, addr)  │
└──────────────┘         └─────────────────┘
       │1                        │1
       │                         │
       │1        ┌──────────────┐│
       └────────→│  employees   ││
                 │ (salary,type)││
                 └──────────────┘│
                       │M        │M
                       │         │
                ┌──────▼─────┐   │
                │  companies │   │
                │ (reg_num)  │   │
                └──────┬─────┘   │
                       │1        │
                       │M        │
                ┌──────▼──────┐  │         ┌─────────────────┐
                │   offices   │  └────────→│    shipments    │
                │  (address)  │←───────────│ (weight, price) │
                └─────────────┘            └─────────────────┘
                                                   │
                                           ┌───────▼───────┐
                                           │ pricing_config│
                                           │(base, per_kg) │
                                           └───────────────┘
```

### Schema Highlights

**File: `resources/schema.sql` (lines 110-136)**

```sql
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    registered_by_id BIGINT NOT NULL,
    origin_office_id BIGINT,
    delivery_address VARCHAR(255),
    delivery_office_id BIGINT,
    weight DECIMAL(10,2) NOT NULL,      -- BigDecimal in Java
    price DECIMAL(10,2) NOT NULL,       -- BigDecimal in Java
    status VARCHAR(20) NOT NULL,
    registered_at DATETIME NOT NULL,
    delivered_at DATETIME,
    updated_at DATETIME,

    FOREIGN KEY (sender_id) REFERENCES customers(id),
    FOREIGN KEY (recipient_id) REFERENCES customers(id),
    FOREIGN KEY (registered_by_id) REFERENCES employees(id),
    INDEX idx_shipments_status (status),
    INDEX idx_shipments_delivered_at (delivered_at)
);
```

### What to Say

> "All monetary values use `DECIMAL(10,2)`, never `FLOAT` or `DOUBLE`. This prevents precision errors in financial calculations. In Java, these map to `BigDecimal`.
>
> The pricing configuration is stored in the database, so it can be changed without redeploying the application. Only one pricing config row should be active at a time.
>
> Foreign keys enforce referential integrity - you can't create a shipment for a non-existent customer."

### Live Demo Suggestion

1. Open MySQL Workbench or command line
2. Show `pricing_config` table with current values
3. Update a pricing value
4. Create a new shipment
5. Show the new price was applied

---

## SECTION 5: Security Implementation

**Duration: 5 minutes**

### Authentication Flow Diagram

```
┌─────────┐                                    ┌──────────────┐
│  Client │                                    │   Server     │
└────┬────┘                                    └──────┬───────┘
     │                                                │
     │  1. POST /api/auth/login                       │
     │     {username, password}                       │
     │───────────────────────────────────────────────>│
     │                                                │
     │                        2. Validate credentials │
     │                        3. Generate JWT token   │
     │                                                │
     │  4. Response: {token: "eyJhbG...", role: ...}  │
     │<───────────────────────────────────────────────│
     │                                                │
     │  5. GET /api/shipments                         │
     │     Authorization: Bearer eyJhbG...            │
     │───────────────────────────────────────────────>│
     │                                                │
     │                        6. Validate JWT token   │
     │                        7. Extract username/role│
     │                        8. Check @PreAuthorize  │
     │                                                │
     │  9. Response: [shipment data]                  │
     │<───────────────────────────────────────────────│
```

### SecurityConfig Code

**File: `config/SecurityConfig.java` (lines 67-113)**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // Disable CSRF - we're using stateless JWT authentication
            .csrf(csrf -> csrf.disable())

            // Set session management to stateless
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                    // Static files - public access
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/css/**", "/js/**").permitAll()

                    // Auth endpoints - public access
                    .requestMatchers("/api/auth/**").permitAll()

                    // Swagger documentation
                    .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            )

            // Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### JWT Token Provider

**File: `security/JwtTokenProvider.java` (lines 62-77)**

```java
public String generateToken(String username, Role role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);  // 24 hours

    String token = Jwts.builder()
            .subject(username)           // Who the token is for
            .claim("role", role.name())  // User's role embedded in token
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)         // HMAC-SHA256 signature
            .compact();

    return token;
}
```

### Role-Based Access Control

**File: `controller/ShipmentController.java`**

```java
// Only employees can register shipments
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<ShipmentResponse> registerShipment(...) { }

// Only employees can update shipment status
@PatchMapping("/{id}/status")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<ShipmentResponse> updateShipmentStatus(...) { }
```

### What to Say

> "I use JWT for stateless authentication. This means the server doesn't store session data - all information needed to validate a request is in the token itself.
>
> The flow is:
> 1. User logs in with username/password
> 2. Server validates credentials and generates a JWT containing username and role
> 3. Client stores the token and sends it in the Authorization header for subsequent requests
> 4. Server validates the token signature and expiration on each request
> 5. Spring Security's `@PreAuthorize` annotation checks if the user has the required role"

---

## SECTION 6: Pricing System

**Duration: 5 minutes**

### Pricing Formula

```
Total Price = Base Price + (Weight × Price per Kg) + Delivery Fee

Where:
├── Base Price:       Fixed cost per shipment (default: $5.00)
├── Price per Kg:     Cost per kilogram (default: $2.00/kg)
└── Delivery Fee:
    ├── Office delivery:  $0.00 (free)
    └── Address delivery: $10.00 (extra fee)
```

### Example Calculations

```
Example 1: 5kg package to office
  Total = $5.00 + (5 × $2.00) + $0.00 = $15.00

Example 2: 5kg package to address
  Total = $5.00 + (5 × $2.00) + $10.00 = $25.00

Example 3: 2.75kg package to office
  Total = $5.00 + (2.75 × $2.00) + $0.00 = $10.50
```

### Code Implementation

**File: `service/impl/PricingServiceImpl.java` (lines 64-88)**

```java
@Override
@Transactional(readOnly = true)
public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
    PricingConfig config = getActiveConfig();  // From database

    // Step 1: Start with base price
    BigDecimal total = config.getBasePrice();

    // Step 2: Add weight-based cost (weight × price per kg)
    BigDecimal weightCost = weight.multiply(config.getPricePerKg());
    total = total.add(weightCost);

    // Step 3: Add delivery type fee (0 for office, addressDeliveryFee for address)
    if (!isOfficeDelivery) {
        total = total.add(config.getAddressDeliveryFee());
    }

    // Round to 2 decimal places for currency
    total = total.setScale(2, RoundingMode.HALF_UP);

    return total;
}
```

### What to Say

> "Pricing is calculated **server-side only**. The frontend never calculates prices because:
> 1. Prices could be manipulated by users
> 2. The server is the source of truth
>
> I use `BigDecimal` for all monetary calculations because `double` can have precision errors. For example, `0.1 + 0.2` in floating-point equals `0.30000000000000004`, not `0.3`.
>
> Configuration comes from the database, so prices can be changed without code modifications. This follows the Open/Closed Principle."

### Live Demo

1. Show current `pricing_config` in database
2. Register a shipment, note the price
3. Update pricing in database (e.g., increase base price)
4. Register another shipment, show new price applied

---

## SECTION 7: Validation Strategy

**Duration: 4 minutes**

### Multi-Layer Validation

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: FRONTEND (Immediate User Feedback)                │
│  validation.js - validateShipmentForm()                     │
│  "Weight must be greater than 0"                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 2: DTO (@Valid Annotations)                          │
│  ShipmentRequest.java - @NotNull, @DecimalMin, @DecimalMax  │
│  Automatic HTTP 400 if validation fails                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 3: SERVICE (Business Rules)                          │
│  ShipmentServiceImpl - validateDeliveryDestination()        │
│  "Cannot specify both address and office"                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 4: EXCEPTION HANDLER (Consistent Responses)          │
│  GlobalExceptionHandler - formats all errors                │
│  Returns structured JSON error response                     │
└─────────────────────────────────────────────────────────────┘
```

### Layer 1: Frontend Validation

**File: `resources/static/js/validation.js` (lines 202-242)**

```javascript
export function validateShipmentForm(data) {
    const errors = [];

    // Sender validation
    if (!data.senderId) {
        errors.push('Please select a sender.');
    }

    // Weight validation - must be between 0.01 and 10000 kg
    if (!data.weight || data.weight <= 0) {
        errors.push('Weight must be greater than 0.');
    } else if (data.weight < 0.01) {
        errors.push('Weight must be at least 0.01 kg.');
    } else if (data.weight > 10000) {
        errors.push('Weight cannot exceed 10000 kg.');
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}
```

### Layer 2: DTO Validation

**File: `dto/shipment/ShipmentRequest.java` (lines 24-59)**

```java
public class ShipmentRequest {

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @Size(max = 255, message = "Delivery address must not exceed 255 characters")
    private String deliveryAddress;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "10000.00", message = "Weight cannot exceed 10000 kg")
    private BigDecimal weight;
}
```

### Layer 3: Service Validation

**File: `service/impl/ShipmentServiceImpl.java` (lines 282-300)**

```java
private void validateDeliveryDestination(ShipmentRequest request) {
    boolean hasAddress = request.isAddressDelivery();
    boolean hasOffice = request.isOfficeDelivery();

    if (!hasAddress && !hasOffice) {
        throw new InvalidDataException(
            "Either deliveryAddress or deliveryOfficeId must be provided");
    }

    if (hasAddress && hasOffice) {
        throw new InvalidDataException(
            "Cannot specify both deliveryAddress and deliveryOfficeId");
    }
}
```

### Layer 4: Global Exception Handler

**File: `exception/GlobalExceptionHandler.java` (lines 148-169)**

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    // Collect all field validation errors
    Map<String, String> validationErrors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
        validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "One or more fields have validation errors",
            request.getRequestURI()
    );
    errorResponse.setValidationErrors(validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
}
```

### What to Say

> "I validate at **multiple layers** for defense in depth:
>
> 1. **Frontend validation** gives immediate user feedback without a server round-trip
> 2. **DTO validation** catches any requests that bypass the frontend (e.g., API tools like Postman)
> 3. **Service validation** enforces business rules like 'either address OR office, not both'
> 4. **Exception handler** ensures all errors return a consistent JSON format
>
> Even if a malicious user bypasses frontend validation, the backend still validates everything. This is a security best practice."

---

## SECTION 8: Access Control Logic

**Duration: 3 minutes**

### Role-Based Data Filtering

**File: `controller/ShipmentController.java` (lines 102-119)**

```java
@GetMapping
@Operation(summary = "Get all shipments",
           description = "Employees see all. Customers see only their own.")
public ResponseEntity<List<ShipmentResponse>> getAllShipments(
        Authentication authentication) {

    List<ShipmentResponse> shipments;

    if (isCustomer(authentication)) {
        // Customer: only their shipments (as sender OR recipient)
        Long customerId = getCustomerIdFromAuth(authentication);
        shipments = shipmentService.getShipmentsByCustomerId(customerId);
    } else {
        // Employee: all shipments
        shipments = shipmentService.getAllShipments();
    }

    return ResponseEntity.ok(shipments);
}
```

### Service Layer Filtering

**File: `service/impl/ShipmentServiceImpl.java` (lines 166-180)**

```java
@Override
@Transactional(readOnly = true)
public List<ShipmentResponse> getShipmentsByCustomerId(Long customerId) {
    // Validate customer exists
    if (!customerRepository.existsById(customerId)) {
        throw new ResourceNotFoundException("Customer", "id", customerId);
    }

    // Return shipments where customer is sender OR recipient
    return shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId)
            .stream()
            .map(EntityMapper::toShipmentResponse)
            .collect(Collectors.toList());
}
```

### Access Denied for Unauthorized Access

**File: `controller/ShipmentController.java` (lines 77-95)**

```java
@GetMapping("/{id}")
public ResponseEntity<ShipmentResponse> getShipmentById(
        @PathVariable Long id,
        Authentication authentication) {

    ShipmentResponse shipment = shipmentService.getShipmentById(id);

    // If customer, verify they have access to this shipment
    if (isCustomer(authentication)) {
        Long customerId = getCustomerIdFromAuth(authentication);
        if (!shipment.getSenderId().equals(customerId) &&
                !shipment.getRecipientId().equals(customerId)) {
            throw new UnauthorizedException(
                "You can only view shipments where you are sender or recipient");
        }
    }

    return ResponseEntity.ok(shipment);
}
```

### What to Say

> "Access control happens at **multiple levels**:
>
> 1. `@PreAuthorize("hasRole('EMPLOYEE')")` blocks customers from employee-only endpoints entirely
> 2. For shared endpoints like viewing shipments, the **controller checks the role** and **services filter data**
> 3. A customer can only see shipments where they are the sender OR recipient
> 4. Employees bypass these filters and see everything
>
> This ensures data isolation - customers never accidentally see other customers' shipments."

### Live Demo

1. Login as a customer
2. View shipments - only see own shipments
3. Try to access another shipment by ID - get 403 Forbidden
4. Login as employee - see all shipments

---

## SECTION 9: Report Generation

**Duration: 3 minutes**

### Available Reports (8 Total)

| Report | Endpoint | Access |
|--------|----------|--------|
| All Employees | GET /api/reports/employees | Employee only |
| All Customers | GET /api/reports/customers | Employee only |
| All Shipments | GET /api/reports/shipments | Employee: all, Customer: own |
| By Employee | GET /api/reports/shipments/employee/{id} | Employee only |
| Pending Shipments | GET /api/reports/shipments/pending | Employee only |
| Sent by Customer | GET /api/reports/shipments/customer/{id}/sent | Own data |
| Received by Customer | GET /api/reports/shipments/customer/{id}/received | Own data |
| Revenue | GET /api/reports/revenue?startDate=...&endDate=... | Employee only |

### Revenue Report Implementation

**File: `service/impl/ReportServiceImpl.java` (lines 137-171)**

```java
/**
 * IMPORTANT: Only counts DELIVERED shipments as revenue.
 * Cancelled or pending shipments do NOT count as revenue because:
 * - Cancelled: No payment was completed
 * - Pending: Payment hasn't been confirmed/completed
 *
 * Revenue = SUM(price) for all DELIVERED shipments in the date range.
 */
@Override
public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
    logger.info("Generating revenue report from {} to {}", startDate, endDate);

    // Convert LocalDate to LocalDateTime for query
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    // Calculate total revenue (sum of prices for DELIVERED shipments only)
    BigDecimal totalRevenue = shipmentRepository
        .calculateRevenueBetweenDates(startDateTime, endDateTime);

    // If no delivered shipments, revenue is 0
    if (totalRevenue == null) {
        totalRevenue = BigDecimal.ZERO;
    }

    // Count delivered shipments in the period
    long deliveredCount = shipmentRepository
        .findDeliveredShipmentsBetweenDates(startDateTime, endDateTime).size();

    return new RevenueResponse(startDate, endDate, totalRevenue, deliveredCount);
}
```

### What to Say

> "The revenue report only counts **DELIVERED** shipments. Cancelled or pending shipments don't count because payment hasn't been completed.
>
> The report filters by **delivery date**, not registration date. This gives an accurate picture of when revenue was actually earned.
>
> I use `BigDecimal.add()` for summing prices to maintain precision - never `double` for financial calculations."

---

## SECTION 10: Testing Approach

**Duration: 3 minutes**

### Test Structure

```
src/test/java/com/logistics/
├── controller/     # Integration tests (8 test classes)
├── service/        # Unit tests (6 test classes)
├── repository/     # Repository tests
└── security/       # Security tests
```

### Unit Test Example

**File: `service/PricingServiceTest.java` (lines 47-65)**

```java
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingConfigRepository pricingConfigRepository;

    @InjectMocks
    private PricingServiceImpl pricingService;

    @Test
    @DisplayName("Should calculate correct price for office delivery")
    void calculatePrice_OfficeDelivery_ReturnsCorrectPrice() {
        // Arrange
        when(pricingConfigRepository.findByActiveTrue())
            .thenReturn(Optional.of(defaultConfig));
        BigDecimal weight = new BigDecimal("5.00");

        // Act
        // Formula: 5.00 + (5.00 × 2.00) + 0.00 = 15.00
        BigDecimal price = pricingService.calculatePrice(weight, true);

        // Assert
        assertEquals(new BigDecimal("15.00"), price);
        verify(pricingConfigRepository).findByActiveTrue();
    }
}
```

### What to Say

> "I use **Mockito** to mock dependencies, which allows testing business logic in isolation. The `@Mock` annotation creates a mock of the repository, and `@InjectMocks` injects it into the service.
>
> The test follows the **Arrange-Act-Assert** pattern:
> 1. **Arrange**: Set up test data and mock behavior
> 2. **Act**: Call the method being tested
> 3. **Assert**: Verify the result and mock interactions
>
> Tests cover edge cases like zero weight, negative values, and missing configuration."

### Key Test Cases

- Pricing calculation for office vs. address delivery
- Error handling when no pricing config exists
- Validation of business rules
- Authentication and authorization flows

---

## SECTION 11: Code Quality Highlights

**Duration: 2 minutes**

### Clean Code Practices

**Descriptive Method Names:**
```java
// GOOD - Self-documenting
private void validateDeliveryDestination(ShipmentRequest request)
private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next)
public boolean isOfficeDelivery()

// BAD - Unclear purpose
private void check(Request r)
private void validate(Status s)
public boolean flag1()
```

### Comprehensive JavaDoc

```java
/**
 * Registers a new shipment in the system.
 *
 * @param request           shipment details including sender, recipient, weight
 * @param employeeUsername  username of the employee registering the shipment
 * @return registered shipment with calculated price
 * @throws ResourceNotFoundException if sender/recipient/employee not found
 * @throws InvalidDataException if delivery destination is invalid
 */
public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
```

### No Business Logic in Frontend

```javascript
// Frontend just displays backend response
const price = response.price;  // Calculated by backend

// NOT: const price = calculatePrice(weight, deliveryType);  // DON'T DO THIS!
```

---

## SECTION 12: Live Demonstration Script

**Duration: 10 minutes**

### Step-by-Step Demo

#### 1. Registration & Login (2 min)

```bash
# Register an employee
POST /api/auth/register
{
  "username": "john_employee",
  "email": "john@company.com",
  "password": "password123",
  "role": "EMPLOYEE"
}

# Show JWT token in response
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "john_employee",
  "role": "EMPLOYEE"
}

# Register a customer
POST /api/auth/register
{
  "username": "alice_customer",
  "email": "alice@email.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

#### 2. Employee Operations (3 min)

```bash
# Create company (as employee)
POST /api/companies
Authorization: Bearer <employee_token>
{
  "name": "FastShip Logistics",
  "registrationNumber": "REG-12345",
  "address": "123 Main St",
  "phone": "+1-555-0100"
}

# Create office
POST /api/offices
{
  "companyId": 1,
  "name": "Downtown Office",
  "address": "456 Center Ave",
  "city": "New York",
  "country": "USA"
}

# Register shipment - show auto-calculated price
POST /api/shipments
{
  "senderId": 1,
  "recipientId": 2,
  "originOfficeId": 1,
  "deliveryAddress": "789 Customer Blvd",
  "weight": 5.00
}

# Point out: Price was calculated automatically!
# 5.00 base + (5 × 2.00) + 10.00 address fee = 25.00
```

#### 3. Customer View (2 min)

```bash
# Login as customer
POST /api/auth/login
{ "username": "alice_customer", "password": "password123" }

# View shipments - only see own
GET /api/shipments
Authorization: Bearer <customer_token>
# Returns only shipments where alice is sender or recipient

# Try to access another shipment
GET /api/shipments/999
# Returns 403 Forbidden: "You can only view your own shipments"
```

#### 4. Reports (2 min)

```bash
# Generate revenue report (as employee)
GET /api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer <employee_token>

# Response:
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "totalRevenue": 1250.00,
  "deliveredCount": 47
}
```

#### 5. Configuration Change (1 min)

```sql
-- In MySQL, update pricing
UPDATE pricing_config SET base_price = 7.00 WHERE active = true;
```

```bash
# Register new shipment
POST /api/shipments
{ ... weight: 5.00 ... }

# Show new price: 7.00 + (5 × 2.00) + 10.00 = 27.00
# (was 25.00 before)
```

---

## SECTION 13: Common Questions & Answers

### Q1: "Why use BigDecimal instead of double?"

**A:** "Double uses IEEE 754 floating-point representation, which can't exactly represent all decimal values. For example, `0.1 + 0.2` in double equals `0.30000000000000004`, not `0.3`. In financial calculations, these small errors can accumulate and cause significant discrepancies. BigDecimal uses arbitrary-precision decimal representation, ensuring exact calculations. This is why all monetary values in my system - prices, salaries, fees - use BigDecimal."

### Q2: "Explain the difference between authentication and authorization in your system."

**A:** "Authentication verifies WHO the user is. In my system, this happens when the user logs in:
- User provides username/password
- AuthService validates credentials against the database
- If valid, a JWT token is generated containing the username and role

Authorization determines WHAT the user can do. This happens on every request:
- JwtAuthenticationFilter extracts and validates the token
- `@PreAuthorize("hasRole('EMPLOYEE')")` checks if the user has the required role
- Service layer further filters data based on user identity"

### Q3: "What happens if two employees try to update the same shipment simultaneously?"

**A:** "Spring's `@Transactional` annotation ensures database operations are atomic. If two employees update simultaneously:
1. Each transaction reads the current state
2. Each makes its changes
3. The first to commit succeeds
4. The second either overwrites (last-write-wins) or fails if there's a conflict

For true optimistic locking, I could add a `@Version` field to the entity, which would throw `OptimisticLockException` on concurrent updates. This wasn't implemented as it wasn't in the requirements, but it's an enhancement I'm aware of."

### Q4: "Why is pricing in the database instead of the code?"

**A:** "Storing pricing in the database provides several benefits:
1. **No redeployment**: Prices can change without rebuilding and redeploying the application
2. **Audit trail**: We can track pricing changes over time by keeping old configurations
3. **Runtime flexibility**: Business users could update prices through an admin interface
4. **Open/Closed Principle**: The pricing logic is closed for modification but open for extension through configuration"

### Q5: "Show me how you prevent SQL injection."

**A:** "I use Spring Data JPA, which generates parameterized queries. For example:

```java
shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId)
```

This generates:
```sql
SELECT * FROM shipments WHERE sender_id = ? OR recipient_id = ?
```

The `?` placeholders are filled with properly escaped parameter values, making SQL injection impossible. I never concatenate user input into SQL strings."

### Q6: "What would you change if this went to production?"

**A:** "Several enhancements would be needed:
1. **Caching**: Add Redis for frequently accessed data like pricing configuration
2. **Rate limiting**: Prevent API abuse with request throttling
3. **Logging**: Structured logging (JSON) with correlation IDs for request tracing
4. **Monitoring**: Metrics endpoint for Prometheus, dashboards in Grafana
5. **HTTPS**: Enforce TLS for all connections
6. **Environment configuration**: Externalize sensitive config (database passwords, JWT secret) to environment variables or a vault
7. **Database migrations**: Use Flyway or Liquibase for schema version control"

### Q7: "Explain how JWT token validation works."

**A:** "The flow is:
1. Client sends request with `Authorization: Bearer <token>` header
2. `JwtAuthenticationFilter.doFilterInternal()` intercepts the request
3. Token is extracted from the header
4. `JwtTokenProvider.validateToken()` is called:
   - Parses the token using the secret key
   - Verifies the signature hasn't been tampered with
   - Checks the expiration date
5. If valid, username is extracted and `UserDetailsService` loads the user
6. A `UsernamePasswordAuthenticationToken` is created and set in the SecurityContext
7. The request proceeds to the controller with authentication available"

### Q8: "Why separate DTOs from entities?"

**A:** "DTOs (Data Transfer Objects) serve different purposes than entities:
1. **Decoupling**: API contracts don't change when database schema changes
2. **Security**: Entities may have fields we don't want to expose (password hash)
3. **Validation**: DTOs have validation annotations for request data
4. **Flexibility**: Response DTOs can combine data from multiple entities
5. **Performance**: We can include only the fields needed for a specific response"

### Q9: "How do you ensure data consistency?"

**A:** "Multiple mechanisms ensure consistency:
1. **Database constraints**: Foreign keys, NOT NULL, UNIQUE
2. **`@Transactional`**: Service methods run in transactions that rollback on failure
3. **Validation**: Multi-layer validation prevents invalid data from being saved
4. **Business rules**: Service layer enforces rules like 'either address OR office'
5. **Entity callbacks**: `@PrePersist` and `@PreUpdate` set timestamps automatically"

### Q10: "What if a customer tries to register a shipment?"

**A:** "The `@PreAuthorize("hasRole('EMPLOYEE')")` annotation on the `registerShipment` endpoint blocks customers completely. Spring Security intercepts the request before it reaches the controller and returns:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

The customer never gets past the security filter."

---

## SECTION 14: Project Strengths to Highlight

### Say These Confidently

- "Complete implementation of all 8 required reports with role-based access"
- "Strict SOLID principles with documentation in every service class"
- "Multi-layer validation: frontend, DTO annotations, service layer, exception handler"
- "Database-driven pricing configuration for runtime flexibility"
- "Role-based access control with proper customer data isolation"
- "BigDecimal usage throughout for financial accuracy"
- "Clean layered architecture with proper separation of concerns"
- "JWT stateless authentication with 24-hour token expiration"
- "RESTful API design following HTTP conventions"
- "Comprehensive test coverage of critical business logic"

---

## SECTION 15: Technical Decisions Justification

### Be Ready to Explain WHY

| Decision | Justification |
|----------|---------------|
| **Java 17** | Current LTS version, modern features (records, sealed classes available), long-term support |
| **Spring Boot 3.2** | Industry standard, auto-configuration, embedded server, extensive ecosystem |
| **JWT Authentication** | Stateless (no server-side session), scalable, works well with REST APIs |
| **MySQL 8.0** | ACID compliance, relational data with foreign keys, mature and reliable |
| **Layered Architecture** | Separation of concerns, easier testing, maintainability |
| **Interface-based Services** | Dependency inversion, testability with mocks, implementation flexibility |
| **BigDecimal** | Financial precision, no floating-point errors |
| **Database Pricing** | Runtime configurability, no redeployment for price changes |
| **Spring Data JPA** | Reduces boilerplate, type-safe queries, automatic query generation |
| **BCrypt Password Hashing** | Industry-standard, includes salt, adjustable work factor |

---

## Presentation Timing Summary

| Section | Duration |
|---------|----------|
| 1. Introduction | 2-3 min |
| 2. Architecture | 5 min |
| **3. SOLID Principles** | **10 min** (most important!) |
| 4. Database | 5 min |
| 5. Security | 5 min |
| 6. Pricing | 5 min |
| 7. Validation | 4 min |
| 8. Access Control | 3 min |
| 9. Reports | 3 min |
| 10. Testing | 3 min |
| 11. Code Quality | 2 min |
| 12. Live Demo | 10 min |
| 13. Q&A | 15 min |
| **Total** | **~60 minutes** |

---

## Quick Reference Card

### Key Files to Know

| File | Purpose | Show For |
|------|---------|----------|
| `pom.xml` | Dependencies | Introduction |
| `SecurityConfig.java` | JWT setup | Security section |
| `ShipmentController.java` | REST + DIP | Architecture |
| `PricingServiceImpl.java` | SRP + OCP | SOLID |
| `ShipmentRequest.java` | Validation | Validation |
| `GlobalExceptionHandler.java` | Error handling | Validation |
| `PricingServiceTest.java` | Unit testing | Testing |
| `schema.sql` | Database design | Database |

### SOLID Quick Reminder

- **S**ingle Responsibility: One class = one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes must be substitutable for base types
- **I**nterface Segregation: No fat interfaces, focused contracts
- **D**ependency Inversion: Depend on abstractions, not concretions

---

**Good luck with your presentation!**
