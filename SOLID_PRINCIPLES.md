# SOLID Principles Implementation Guide

This document explains how each SOLID principle is implemented in the Logistics Management System with concrete code examples.

---

## S - Single Responsibility Principle (SRP)

> "A class should have one, and only one, reason to change."

### Example 1: Service Separation

Each service has ONE clear responsibility:

```java
// PricingService - ONLY handles pricing calculations
public interface PricingService {
    BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery);
    BigDecimal getBasePrice();
    BigDecimal getPricePerKg();
    BigDecimal getAddressDeliveryFee();
}

// ShipmentService - ONLY handles shipment lifecycle
public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);
    // ... other shipment operations
}

// ReportService - ONLY handles report generation
public interface ReportService {
    List<ShipmentResponse> getAllShipmentsReport();
    RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate);
    // ... other reports
}
```

**Why is this good?**
- If pricing formula changes, ONLY PricingService changes
- If shipment workflow changes, ONLY ShipmentService changes
- Each service is easy to test in isolation

### Example 2: Entity vs. DTO Separation

```java
// Entity - handles persistence ONLY
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // JPA annotations, database mapping
}

// DTO - handles data transfer ONLY
public class ShipmentResponse {
    private Long id;
    private String senderName;  // Computed field for API response
    private String deliveryDestination;  // Formatted for display
}

// Mapper - handles conversion ONLY
public class EntityMapper {
    public static ShipmentResponse toShipmentResponse(Shipment shipment) {
        // Conversion logic here
    }
}
```

---

## O - Open/Closed Principle (OCP)

> "Software entities should be open for extension, but closed for modification."

### Example 1: Configurable Pricing

```java
@Service
public class PricingServiceImpl implements PricingService {

    // Configuration comes from external properties - no code changes needed!
    private final BigDecimal basePrice;
    private final BigDecimal pricePerKg;
    private final BigDecimal addressDeliveryFee;

    public PricingServiceImpl(
            @Value("${pricing.base-price:5.00}") BigDecimal basePrice,
            @Value("${pricing.price-per-kg:2.00}") BigDecimal pricePerKg,
            @Value("${pricing.address-delivery-fee:10.00}") BigDecimal addressDeliveryFee) {
        this.basePrice = basePrice;
        this.pricePerKg = pricePerKg;
        this.addressDeliveryFee = addressDeliveryFee;
    }
}
```

**How to extend?**
- Change pricing in `application.properties` - no code changes!
- Need a new pricing strategy? Create `PremiumPricingServiceImpl` - no modification to existing code!

### Example 2: Exception Handlers

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Each exception type has its own handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(...) { }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(...) { }

    // Adding new exception handling? Just add a new method!
    // Existing handlers don't need to change
}
```

---

## L - Liskov Substitution Principle (LSP)

> "Objects of a superclass should be replaceable with objects of its subclasses without affecting program correctness."

### Example 1: Service Interface Substitution

```java
// Interface
public interface ShipmentService {
    List<ShipmentResponse> getAllShipments();
}

// Implementation
@Service
public class ShipmentServiceImpl implements ShipmentService {
    @Override
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(EntityMapper::toShipmentResponse)
                .collect(Collectors.toList());
    }
}

// Controller depends on interface - can use ANY implementation
@RestController
public class ShipmentController {
    private final ShipmentService shipmentService;  // Interface type!

    // Constructor injection - could be ShipmentServiceImpl, MockShipmentService, etc.
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }
}
```

### Example 2: UserDetailsService Implementation

```java
// Spring Security's interface
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username);
}

// Our implementation - completely substitutable
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

---

## I - Interface Segregation Principle (ISP)

> "Clients should not be forced to depend on interfaces they do not use."

### Example 1: Focused Service Interfaces

Instead of one giant interface:
```java
// BAD - Fat interface
public interface LogisticsService {
    // Company operations
    CompanyResponse createCompany(CompanyRequest request);
    void deleteCompany(Long id);

    // Shipment operations
    ShipmentResponse registerShipment(ShipmentRequest request);
    void updateShipmentStatus(Long id, ShipmentStatus status);

    // Report operations
    RevenueResponse getRevenueReport(LocalDate start, LocalDate end);
    List<ShipmentResponse> getPendingShipments();

    // ... dozens more methods
}
```

We have small, focused interfaces:
```java
// GOOD - Segregated interfaces
public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request);
    CompanyResponse getCompanyById(Long id);
    List<CompanyResponse> getAllCompanies();
    CompanyResponse updateCompany(Long id, CompanyRequest request);
    void deleteCompany(Long id);
}

public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);
    // ... shipment-specific methods only
}

public interface ReportService {
    RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate);
    List<ShipmentResponse> getPendingShipmentsReport();
    // ... report-specific methods only
}
```

### Example 2: Focused DTOs

```java
// For creating a shipment
public class ShipmentRequest {
    private Long senderId;
    private Long recipientId;
    private BigDecimal weight;
    private String deliveryAddress;
    private Long deliveryOfficeId;
    // Only fields needed for creation
}

// For updating status (separate concern)
public class ShipmentStatusUpdateRequest {
    private ShipmentStatus status;
    // Only the status field - nothing else needed!
}

// For responses
public class ShipmentResponse {
    // All fields needed for display
}
```

---

## D - Dependency Inversion Principle (DIP)

> "High-level modules should not depend on low-level modules. Both should depend on abstractions."

### Example 1: ShipmentService Depends on PricingService Interface

```java
@Service
public class ShipmentServiceImpl implements ShipmentService {

    // Depends on INTERFACE, not concrete PricingServiceImpl
    private final PricingService pricingService;

    // Constructor injection with interface type
    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            PricingService pricingService) {  // Interface!
        this.pricingService = pricingService;
    }

    @Override
    public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername) {
        // Uses the interface - doesn't know or care about implementation
        BigDecimal price = pricingService.calculatePrice(
                request.getWeight(),
                request.isOfficeDelivery()
        );
        // ...
    }
}
```

**Benefits:**
- Can swap `PricingServiceImpl` with `DiscountPricingService` without changing `ShipmentServiceImpl`
- Easy to mock for testing
- Decoupled - changes to pricing implementation don't affect shipment logic

### Example 2: Security Configuration

```java
@Configuration
public class SecurityConfig {

    // Depends on interfaces, not implementations
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;  // Spring's interface!

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;  // Could be any implementation
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);  // Interface!
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
```

### Example 3: Repository Pattern

```java
// Repository interface (abstraction)
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByStatus(ShipmentStatus status);
    BigDecimal calculateRevenueBetweenDates(LocalDateTime start, LocalDateTime end);
}

// Service depends on interface
@Service
public class ReportServiceImpl implements ReportService {

    private final ShipmentRepository shipmentRepository;  // Interface!

    public ReportServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        // Uses repository interface - implementation is provided by Spring Data JPA
        BigDecimal revenue = shipmentRepository.calculateRevenueBetweenDates(
                startDateTime, endDateTime);
        // ...
    }
}
```

---

## Summary

| Principle | Application in This Project |
|-----------|----------------------------|
| **SRP** | Separate services for Pricing, Shipment, Report, Auth, etc. |
| **OCP** | Configurable pricing via properties; extensible exception handling |
| **LSP** | All service implementations are substitutable via interfaces |
| **ISP** | Small, focused interfaces for each domain area |
| **DIP** | All services depend on interfaces, not implementations |

## Key Takeaways

1. **Every class has ONE job** - If a class has "and" in its description, it probably does too much
2. **Extend, don't modify** - Add new implementations instead of changing existing code
3. **Program to interfaces** - Always declare variables as interface types
4. **Keep interfaces small** - Better to have many small interfaces than one large one
5. **Inject dependencies** - Let the framework wire things together, don't create dependencies manually
