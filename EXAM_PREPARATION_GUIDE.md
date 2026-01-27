# ПОДГОТОВКА ЗА ИЗПИТ - Logistics Management System

## СЪДЪРЖАНИЕ
1. [Архитектура на проекта](#1-архитектура-на-проекта)
2. [Контролери (Controllers)](#2-контролери-controllers)
3. [Сървиси (Services)](#3-сървиси-services)
4. [Хранилища (Repositories)](#4-хранилища-repositories)
5. [DTO класове](#5-dto-класове)
6. [Entity класове](#6-entity-класове)
7. [Enum класове](#7-enum-класове)
8. [Помощни класове (Utilities)](#8-помощни-класове-utilities)
9. [Често задавани въпроси и отговори](#9-често-задавани-въпроси-и-отговори)

---

## 1. АРХИТЕКТУРА НА ПРОЕКТА

### Слоеве на приложението (Layered Architecture)
```
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                          │
│        (REST endpoints, валидация на заявки, auth)          │
│   Файлове: src/main/java/com/logistics/controller/          │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                            │
│        (Бизнес логика, валидации, транзакции)               │
│   Файлове: src/main/java/com/logistics/service/             │
│            src/main/java/com/logistics/service/impl/         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                          │
│        (Достъп до базата данни, заявки)                     │
│   Файлове: src/main/java/com/logistics/repository/          │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE                                │
│        (Entity класове, релации)                            │
│   Файлове: src/main/java/com/logistics/model/entity/        │
└─────────────────────────────────────────────────────────────┘
```

### Защо използвам слоеста архитектура?
- **Разделение на отговорностите (Separation of Concerns)** - всеки слой има конкретна задача
- **Лесна поддръжка** - промяна в един слой не засяга другите
- **Тестируемост** - всеки слой може да се тества отделно
- **SOLID принципи** - следва Single Responsibility Principle

---

## 2. КОНТРОЛЕРИ (Controllers)

**Местоположение:** `src/main/java/com/logistics/controller/`

### 2.1 AuthController.java
**Път:** `/api/auth`
**Предназначение:** Автентикация и регистрация на потребители

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/auth/register` | POST | Регистрация на нов потребител | Публичен |
| `/api/auth/login` | POST | Вход и получаване на JWT токен | Публичен |

**Ключови методи:**
```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request)

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
```

**Защо няма @PreAuthorize?** - Тези endpoints са публични, защото потребителят трябва да може да се регистрира и влезе без токен.

---

### 2.2 CompanyController.java
**Път:** `/api/companies`
**Предназначение:** CRUD операции за компании

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/companies` | POST | Създаване на компания | EMPLOYEE |
| `/api/companies` | GET | Всички компании | EMPLOYEE |
| `/api/companies/{id}` | GET | Компания по ID | EMPLOYEE |
| `/api/companies/{id}` | PUT | Обновяване | EMPLOYEE |
| `/api/companies/{id}` | DELETE | Изтриване | EMPLOYEE |

**Сигурност:**
```java
@PreAuthorize("hasRole('EMPLOYEE')")  // На ниво клас - важи за всички методи
```

---

### 2.3 OfficeController.java
**Път:** `/api/offices`
**Предназначение:** CRUD операции за офиси

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/offices` | POST | Създаване на офис | EMPLOYEE |
| `/api/offices` | GET | Всички офиси | EMPLOYEE |
| `/api/offices/{id}` | GET | Офис по ID | EMPLOYEE |
| `/api/offices/company/{companyId}` | GET | Офиси по компания | EMPLOYEE |
| `/api/offices/{id}` | PUT | Обновяване | EMPLOYEE |
| `/api/offices/{id}` | DELETE | Изтриване | EMPLOYEE |

**Важна заявка към БД:**
```java
// В OfficeRepository - използва се от getOfficesByCompanyId()
List<Office> findByCompanyId(Long companyId);
```

---

### 2.4 EmployeeController.java
**Път:** `/api/employees`
**Предназначение:** CRUD операции за служители

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/employees` | POST | Създаване на служител | EMPLOYEE |
| `/api/employees` | GET | Всички служители | EMPLOYEE |
| `/api/employees/{id}` | GET | Служител по ID | EMPLOYEE |
| `/api/employees/{id}` | PUT | Обновяване | EMPLOYEE |
| `/api/employees/{id}` | DELETE | Изтриване | EMPLOYEE |

---

### 2.5 CustomerController.java
**Път:** `/api/customers`
**Предназначение:** CRUD операции за клиенти

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/customers` | POST | Създаване на клиент | EMPLOYEE |
| `/api/customers` | GET | Всички клиенти | EMPLOYEE |
| `/api/customers/{id}` | GET | Клиент по ID | EMPLOYEE |
| `/api/customers/user/{userId}` | GET | Клиент по User ID | EMPLOYEE или CUSTOMER |
| `/api/customers/{id}` | PUT | Обновяване | EMPLOYEE |
| `/api/customers/{id}` | DELETE | Изтриване | EMPLOYEE |

**Смесен достъп:**
```java
@PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId)
```

---

### 2.6 ShipmentController.java (НАЙ-ВАЖЕН)
**Път:** `/api/shipments`
**Предназначение:** Управление на пратки

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/shipments` | POST | Регистриране на пратка | EMPLOYEE |
| `/api/shipments` | GET | Всички пратки / Моите пратки | EMPLOYEE / CUSTOMER |
| `/api/shipments/{id}` | GET | Пратка по ID | EMPLOYEE / CUSTOMER (само свои) |
| `/api/shipments/{id}/status` | PATCH | Промяна на статус | EMPLOYEE |
| `/api/shipments/{id}` | PUT | Обновяване | EMPLOYEE |
| `/api/shipments/{id}` | DELETE | Изтриване | EMPLOYEE |

**Контрол на достъпа за CUSTOMER:**
```java
// В метода getShipmentById():
if (isCustomer(authentication)) {
    Long customerId = getCustomerIdFromAuth(authentication);
    // Клиентът вижда само пратки, където е подател ИЛИ получател
    if (!shipment.getSenderId().equals(customerId) &&
        !shipment.getRecipientId().equals(customerId)) {
        throw new UnauthorizedException("You can only view your own shipments");
    }
}
```

**Помощни методи:**
```java
private boolean isCustomer(Authentication auth) {
    return auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
}

private Long getCustomerIdFromAuth(Authentication auth) {
    String username = auth.getName();
    return customerService.getCustomerIdByUsername(username);
}
```

---

### 2.7 PricingController.java
**Път:** `/api/pricing`
**Предназначение:** Управление на ценообразуването

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/pricing` | GET | Текущи цени | Всички автентикирани |
| `/api/pricing/config` | GET | Пълна конфигурация | EMPLOYEE |
| `/api/pricing/config` | PUT | Обновяване на цени | EMPLOYEE |

**Къде се намират стойностите на цените?**
- В базата данни, таблица `pricing_config`
- Entity: `PricingConfig.java`
- Полета: `basePrice`, `pricePerKg`, `addressDeliveryFee`

---

### 2.8 ReportController.java
**Път:** `/api/reports`
**Предназначение:** Справки и отчети

| Endpoint | Метод | Описание | Достъп |
|----------|-------|----------|--------|
| `/api/reports/employees` | GET | Всички служители | EMPLOYEE |
| `/api/reports/customers` | GET | Всички клиенти | EMPLOYEE |
| `/api/reports/shipments` | GET | Всички пратки | EMPLOYEE / CUSTOMER |
| `/api/reports/shipments/employee/{id}` | GET | Пратки по служител | EMPLOYEE |
| `/api/reports/shipments/pending` | GET | Чакащи пратки | EMPLOYEE |
| `/api/reports/shipments/customer/{id}/sent` | GET | Изпратени от клиент | EMPLOYEE / CUSTOMER |
| `/api/reports/shipments/customer/{id}/received` | GET | Получени от клиент | EMPLOYEE / CUSTOMER |
| `/api/reports/revenue` | GET | Приходи за период | EMPLOYEE |
| `/api/reports/dashboard` | GET | Dashboard метрики | EMPLOYEE |
| `/api/reports/customer-metrics` | GET | Метрики за клиент | CUSTOMER |

---

## 3. СЪРВИСИ (Services)

**Местоположение:**
- Интерфейси: `src/main/java/com/logistics/service/`
- Имплементации: `src/main/java/com/logistics/service/impl/`

### 3.1 AuthService / AuthServiceImpl

**Интерфейс методи:**
```java
AuthResponse register(RegisterRequest request);
AuthResponse login(LoginRequest request);
```

**Валидации в register():**
```java
// Проверка за дублиране на username
if (userRepository.existsByUsername(request.getUsername())) {
    throw new DuplicateResourceException("Username already exists");
}

// Проверка за дублиране на email
if (userRepository.existsByEmail(request.getEmail())) {
    throw new DuplicateResourceException("Email already exists");
}
```

**Логика за създаване на потребител:**
```java
// 1. Криптиране на паролата
user.setPassword(passwordEncoder.encode(request.getPassword()));

// 2. Запис на User
User savedUser = userRepository.save(user);

// 3. Създаване на съответния запис (Customer или Employee)
if (request.getRole() == Role.CUSTOMER) {
    Customer customer = new Customer();
    customer.setUser(savedUser);
    customerRepository.save(customer);
} else {
    Employee employee = new Employee();
    employee.setUser(savedUser);
    employee.setHireDate(LocalDate.now());
    employeeRepository.save(employee);
}

// 4. Генериране на JWT токен
String token = jwtTokenProvider.generateToken(savedUser);
```

---

### 3.2 ShipmentService / ShipmentServiceImpl (НАЙ-ВАЖЕН)

**Интерфейс методи:**
```java
ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
ShipmentResponse getShipmentById(Long id);
List<ShipmentResponse> getAllShipments();
List<ShipmentResponse> getShipmentsByCustomerId(Long customerId);
ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);
ShipmentResponse updateShipment(Long id, ShipmentRequest request, String employeeUsername);
void deleteShipment(Long id);
List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status);
```

#### ВАЛИДАЦИЯ НА ДЕСТИНАЦИЯ
**Местоположение:** `ShipmentServiceImpl.validateDeliveryDestination()`
```java
private void validateDeliveryDestination(ShipmentRequest request) {
    boolean hasAddress = request.isAddressDelivery();
    boolean hasOffice = request.isOfficeDelivery();

    // Трябва да има ИЛИ адрес, ИЛИ офис
    if (!hasAddress && !hasOffice) {
        throw new InvalidDataException("Either deliveryAddress or deliveryOfficeId must be provided");
    }

    // НЕ може да има И двете
    if (hasAddress && hasOffice) {
        throw new InvalidDataException("Cannot specify both deliveryAddress and deliveryOfficeId");
    }
}
```

#### ВАЛИДАЦИЯ НА ТЕГЛО
**Местоположение:** `ShipmentServiceImpl.validateWeight()`
```java
private void validateWeight(BigDecimal weight) {
    if (weight == null) {
        throw new InvalidDataException("Weight is required");
    }

    if (weight.compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidDataException("Weight must be greater than 0");
    }

    BigDecimal maxWeight = new BigDecimal("10000.00");
    if (weight.compareTo(maxWeight) > 0) {
        throw new InvalidDataException("Weight cannot exceed 10000 kg");
    }
}
```

#### ВАЛИДАЦИЯ НА СТАТУС ПРЕХОДИ
**Местоположение:** `ShipmentServiceImpl.validateStatusTransition()`
```java
private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
    // Финални статуси - не могат да се променят
    if (currentStatus == ShipmentStatus.DELIVERED || currentStatus == ShipmentStatus.CANCELLED) {
        throw new InvalidStatusTransitionException(
            "Cannot change status of " + currentStatus + " shipment");
    }

    // REGISTERED може да отиде към IN_TRANSIT или CANCELLED
    if (currentStatus == ShipmentStatus.REGISTERED) {
        if (newStatus != ShipmentStatus.IN_TRANSIT && newStatus != ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "REGISTERED can only transition to IN_TRANSIT or CANCELLED");
        }
    }

    // IN_TRANSIT може да отиде към DELIVERED или CANCELLED
    if (currentStatus == ShipmentStatus.IN_TRANSIT) {
        if (newStatus != ShipmentStatus.DELIVERED && newStatus != ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "IN_TRANSIT can only transition to DELIVERED or CANCELLED");
        }
    }
}
```

**Диаграма на преходите:**
```
REGISTERED ───────→ IN_TRANSIT ───────→ DELIVERED (край)
     │                    │
     └────→ CANCELLED ←───┘
              (край)
```

---

### 3.3 PricingService / PricingServiceImpl

**Интерфейс методи:**
```java
BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery);
BigDecimal getBasePrice();
BigDecimal getPricePerKg();
BigDecimal getAddressDeliveryFee();
PricingConfigResponse getActivePricingConfig();
PricingConfigResponse updatePricingConfig(PricingConfigRequest request);
```

#### ФОРМУЛА ЗА КАЛКУЛИРАНЕ НА ЦЕНА
**Местоположение:** `PricingServiceImpl.calculatePrice()`
```java
public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
    PricingConfig config = getActiveConfig();

    // Базова цена
    BigDecimal total = config.getBasePrice();

    // + Тегло × Цена на кг
    total = total.add(weight.multiply(config.getPricePerKg()));

    // + Такса за доставка до адрес (ако не е до офис)
    if (!isOfficeDelivery) {
        total = total.add(config.getAddressDeliveryFee());
    }

    // Закръгляне до 2 знака
    return total.setScale(2, RoundingMode.HALF_UP);
}
```

**Примерни изчисления:**
```
Конфигурация: basePrice=5.00, pricePerKg=2.00, addressFee=10.00

Пример 1: 5kg до офис
Цена = 5.00 + (5 × 2.00) + 0 = 15.00 BGN

Пример 2: 5kg до адрес
Цена = 5.00 + (5 × 2.00) + 10.00 = 25.00 BGN

Пример 3: 0.5kg до офис
Цена = 5.00 + (0.5 × 2.00) + 0 = 6.00 BGN
```

---

### 3.4 ReportService / ReportServiceImpl

**Интерфейс методи:**
```java
List<EmployeeResponse> getAllEmployeesReport();
List<CustomerResponse> getAllCustomersReport();
List<ShipmentResponse> getAllShipmentsReport();
List<ShipmentResponse> getShipmentsByEmployeeReport(Long employeeId);
List<ShipmentResponse> getPendingShipmentsReport();
List<ShipmentResponse> getShipmentsSentByCustomerReport(Long customerId);
List<ShipmentResponse> getShipmentsReceivedByCustomerReport(Long customerId);
RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate);
DashboardMetricsResponse getDashboardMetrics();
CustomerMetricsResponse getCustomerMetrics(Long customerId);
```

#### ИЗЧИСЛЯВАНЕ НА ПРИХОДИ
**Местоположение:** `ReportServiceImpl.getRevenueReport()`
```java
public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    // САМО DELIVERED пратки се броят като приход!
    BigDecimal totalRevenue = shipmentRepository
        .calculateRevenueBetweenDates(startDateTime, endDateTime);

    Long deliveredCount = shipmentRepository
        .countDeliveredBetweenDates(startDateTime, endDateTime);

    return new RevenueResponse(startDate, endDate, totalRevenue, deliveredCount);
}
```

**Важно:** Само пратки със статус `DELIVERED` се включват в приходите!

---

## 4. ХРАНИЛИЩА (Repositories)

**Местоположение:** `src/main/java/com/logistics/repository/`

### 4.1 UserRepository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // За автентикация
    Optional<User> findByEmail(String email);        // За търсене
    boolean existsByUsername(String username);        // Проверка за дубликат
    boolean existsByEmail(String email);              // Проверка за дубликат
    List<User> findByRole(Role role);                 // Филтриране по роля
}
```

---

### 4.2 CompanyRepository
```java
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByRegistrationNumber(String registrationNumber);
    Optional<Company> findByName(String name);
    boolean existsByRegistrationNumber(String registrationNumber);  // Уникален номер
}
```

---

### 4.3 OfficeRepository
```java
public interface OfficeRepository extends JpaRepository<Office, Long> {
    List<Office> findByCompanyId(Long companyId);  // Офиси на компания
    List<Office> findByCity(String city);
    List<Office> findByCountry(String country);
    List<Office> findByNameContainingIgnoreCase(String name);  // Търсене
}
```

---

### 4.4 EmployeeRepository
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);

    // CUSTOM QUERY - присъединяване на User таблицата
    @Query("SELECT e FROM Employee e WHERE e.user.username = :username")
    Optional<Employee> findByUsername(@Param("username") String username);

    List<Employee> findByCompanyId(Long companyId);
    List<Employee> findByEmployeeType(EmployeeType type);
    List<Employee> findByOfficeId(Long officeId);
    boolean existsByUserId(Long userId);  // Проверка за дубликат

    // Default методи
    default List<Employee> findAllCouriers() {
        return findByEmployeeType(EmployeeType.COURIER);
    }

    default List<Employee> findAllOfficeStaff() {
        return findByEmployeeType(EmployeeType.OFFICE_STAFF);
    }
}
```

---

### 4.5 CustomerRepository
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);

    // CUSTOM QUERY
    @Query("SELECT c FROM Customer c WHERE c.user.username = :username")
    Optional<Customer> findByUsername(@Param("username") String username);

    @Query("SELECT c FROM Customer c WHERE c.user.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    boolean existsByUserId(Long userId);
}
```

---

### 4.6 ShipmentRepository (НАЙ-СЛОЖЕН)
```java
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // По служител, който е регистрирал
    List<Shipment> findByRegisteredById(Long employeeId);

    // По подател
    List<Shipment> findBySenderId(Long senderId);

    // По получател
    List<Shipment> findByRecipientId(Long recipientId);

    // Пратки на клиент (подател ИЛИ получател)
    @Query("SELECT s FROM Shipment s WHERE s.sender.id = :customerId OR s.recipient.id = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);

    // По статус
    List<Shipment> findByStatus(ShipmentStatus status);

    // Чакащи пратки (НЕ доставени)
    @Query("SELECT s FROM Shipment s WHERE s.status != 'DELIVERED' AND s.status != 'CANCELLED'")
    List<Shipment> findAllPendingShipments();

    // Доставени в период (за приходи)
    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    List<Shipment> findDeliveredBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // ИЗЧИСЛЯВАНЕ НА ПРИХОДИ
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s " +
           "WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    BigDecimal calculateRevenueBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // Броене
    Long countByStatus(ShipmentStatus status);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'REGISTERED' OR s.status = 'IN_TRANSIT'")
    Long countInTransitShipments();

    // Общ приход
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    // Метрики за клиент
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.sender.id = :senderId AND s.status = 'DELIVERED'")
    BigDecimal calculateTotalSpentByCustomer(@Param("senderId") Long senderId);

    Long countBySenderId(Long senderId);
}
```

---

### 4.7 PricingConfigRepository
```java
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {

    // Взимане на активната конфигурация
    Optional<PricingConfig> findByActiveTrue();

    // Деактивиране на всички (при обновяване)
    @Modifying
    @Query("UPDATE PricingConfig p SET p.active = false WHERE p.active = true")
    void deactivateAll();
}
```

---

## 5. DTO КЛАСОВЕ

**Местоположение:** `src/main/java/com/logistics/dto/`

### Защо използвам DTO (Data Transfer Objects)?
1. **Сигурност** - не изпращам Entity директно (може да съдържа чувствителни данни)
2. **Гъвкавост** - мога да форматирам данните различно за различни клиенти
3. **Валидация** - анотациите за валидация са в Request DTO
4. **Разделение** - Entity е за БД, DTO е за API

### 5.1 Auth DTOs (`auth/`)

**RegisterRequest.java:**
```java
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
```

**LoginRequest.java:**
```java
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
```

**AuthResponse.java:**
```java
public class AuthResponse {
    private String token;      // JWT токен
    private Long userId;
    private String username;
    private String email;
    private Role role;
}
```

---

### 5.2 Shipment DTOs (`shipment/`)

**ShipmentRequest.java:**
```java
public class ShipmentRequest {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    private Long originOfficeId;  // Опционално

    @Size(max = 255)
    private String deliveryAddress;  // ИЛИ това

    private Long deliveryOfficeId;   // ИЛИ това

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "10000.00", message = "Weight cannot exceed 10000 kg")
    private BigDecimal weight;

    // Помощни методи
    public boolean isOfficeDelivery() {
        return deliveryOfficeId != null;
    }

    public boolean isAddressDelivery() {
        return deliveryAddress != null && !deliveryAddress.isBlank();
    }
}
```

**ShipmentResponse.java:**
```java
public class ShipmentResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private Long registeredById;
    private String registeredByName;
    private Long originOfficeId;
    private String originOfficeName;
    private String deliveryAddress;
    private Long deliveryOfficeId;
    private String deliveryOfficeName;
    private BigDecimal weight;
    private BigDecimal price;           // Калкулирана автоматично!
    private ShipmentStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;
    private boolean deliverToAddress;   // Флаг за frontend
}
```

---

### 5.3 Report DTOs (`report/`)

**RevenueResponse.java:**
```java
public class RevenueResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private Long deliveredShipmentsCount;
}
```

**DashboardMetricsResponse.java:**
```java
public class DashboardMetricsResponse {
    private Long totalShipments;
    private Long pendingShipments;    // REGISTERED + IN_TRANSIT
    private Long deliveredShipments;
    private BigDecimal totalRevenue;  // Само от DELIVERED
}
```

---

## 6. ENTITY КЛАСОВЕ

**Местоположение:** `src/main/java/com/logistics/model/entity/`

### 6.1 User.java
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;  // BCrypt криптирана

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Релации
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Employee employee;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

### 6.2 Shipment.java (НАЙ-ВАЖЕН)
```java
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Подател
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Customer sender;

    // Получател
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Customer recipient;

    // Служител, регистрирал пратката
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id", nullable = false)
    private Employee registeredBy;

    // Офис на произход (опционален)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_office_id")
    private Office originOffice;

    // ВЗАИМНО ИЗКЛЮЧВАЩИ СЕ полета за дестинация
    @Column(name = "delivery_address")
    private String deliveryAddress;  // За доставка до адрес

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_office_id")
    private Office deliveryOffice;   // За доставка до офис

    // Тегло - ВИНАГИ BigDecimal за парични стойности!
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("10000.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    // Цена - калкулира се автоматично от PricingService
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Статус
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.REGISTERED;

    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;

    // Помощни методи
    public boolean isOfficeDelivery() {
        return deliveryOffice != null;
    }

    public boolean isAddressDelivery() {
        return deliveryAddress != null && !deliveryAddress.isBlank();
    }

    public String getDeliveryDestination() {
        if (isOfficeDelivery()) {
            return "Office: " + deliveryOffice.getName();
        } else if (isAddressDelivery()) {
            return "Address: " + deliveryAddress;
        }
        return "Not specified";
    }

    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == ShipmentStatus.CANCELLED;
    }
}
```

---

### 6.3 PricingConfig.java
```java
@Entity
@Table(name = "pricing_config")
public class PricingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;         // Базова цена за пратка

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerKg;        // Цена на килограм

    @Column(precision = 10, scale = 2)
    private BigDecimal addressDeliveryFee; // Такса за доставка до адрес

    @Column(nullable = false)
    private Boolean active = true;         // Само една активна!

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

## 7. ENUM КЛАСОВЕ

**Местоположение:** `src/main/java/com/logistics/model/enums/`

### 7.1 Role.java
```java
public enum Role {
    EMPLOYEE,   // Пълен достъп до системата
    CUSTOMER    // Ограничен достъп - само собствени пратки
}
```

### 7.2 EmployeeType.java
```java
public enum EmployeeType {
    COURIER,       // Доставчик
    OFFICE_STAFF   // Офис служител
}
```

### 7.3 ShipmentStatus.java
```java
public enum ShipmentStatus {
    REGISTERED,  // Регистрирана (начално състояние)
    IN_TRANSIT,  // В транзит
    DELIVERED,   // Доставена (финално - брои се като приход)
    CANCELLED    // Отказана (финално - без приход)
}
```

---

## 8. ПОМОЩНИ КЛАСОВЕ (Utilities)

**Местоположение:** `src/main/java/com/logistics/util/`

### 8.1 EntityMapper.java
```java
public final class EntityMapper {

    // Приватен конструктор - utility клас
    private EntityMapper() {}

    public static CompanyResponse toCompanyResponse(Company company) {
        // Конвертиране Company -> CompanyResponse
    }

    public static OfficeResponse toOfficeResponse(Office office) {
        // Включва име на компанията
    }

    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        // Обработва nullable company/office
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        // Конвертиране Customer -> CustomerResponse
    }

    public static ShipmentResponse toShipmentResponse(Shipment shipment) {
        // Най-сложен - включва всички свързани данни
        // Имена на подател, получател, офиси и т.н.
    }
}
```

**Защо използвам отделен Mapper клас?**
- **Single Responsibility** - само за конвертиране
- **Централизирана логика** - на едно място
- **Null-safe** - обработва липсващи данни
- **Лесно за тестване**

---

## 9. ЧЕСТО ЗАДАВАНИ ВЪПРОСИ И ОТГОВОРИ

### Q1: Къде са заявките към базата данни?
**Отговор:** В Repository интерфейсите в папка `src/main/java/com/logistics/repository/`

**Видове заявки:**
1. **Автоматични от Spring Data JPA:**
   ```java
   findById(), findAll(), save(), deleteById()
   ```

2. **По конвенция (method naming):**
   ```java
   findByUsername(String username)
   findByCompanyId(Long companyId)
   existsByEmail(String email)
   ```

3. **Custom @Query:**
   ```java
   @Query("SELECT e FROM Employee e WHERE e.user.username = :username")
   Optional<Employee> findByUsername(@Param("username") String username);

   @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.status = 'DELIVERED'")
   BigDecimal calculateTotalRevenue();
   ```

---

### Q2: Имам ли валидация на данните? Къде?

**Отговор:** Да, на няколко нива:

| Ниво | Местоположение | Примери |
|------|----------------|---------|
| **DTO валидация** | Request класове | `@NotNull`, `@Email`, `@Size`, `@DecimalMin` |
| **Entity валидация** | Entity класове | `@NotBlank`, `@Column(unique=true)` |
| **Бизнес валидация** | Service имплементации | `validateDeliveryDestination()`, `validateWeight()`, `validateStatusTransition()` |
| **Database constraints** | Entity анотации | `unique = true`, `nullable = false` |

**Пример за валидация в ShipmentRequest:**
```java
@NotNull(message = "Sender ID is required")
private Long senderId;

@DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
@DecimalMax(value = "10000.00", message = "Weight cannot exceed 10000 kg")
private BigDecimal weight;
```

---

### Q3: Къде са стойностите на цените и как се калкулират?

**Отговор:**

**Къде се съхраняват:**
- Entity: `PricingConfig.java`
- Таблица в БД: `pricing_config`
- Полета: `basePrice`, `pricePerKg`, `addressDeliveryFee`

**Къде се калкулират:**
- Файл: `PricingServiceImpl.java`
- Метод: `calculatePrice(BigDecimal weight, boolean isOfficeDelivery)`

**Формула:**
```
Цена = basePrice + (weight × pricePerKg) + deliveryFee

Където deliveryFee = 0 за офис, addressDeliveryFee за адрес
```

**Кога се извиква:**
- При регистрация: `ShipmentServiceImpl.registerShipment()`
- При обновяване: `ShipmentServiceImpl.updateShipment()`

---

### Q4: Как се определя кой до какво има достъп?

**Отговор:** Чрез Role-Based Access Control (RBAC)

**Нива на контрол:**

1. **На ниво клас (всички методи):**
   ```java
   @RestController
   @PreAuthorize("hasRole('EMPLOYEE')")
   public class CompanyController { ... }
   ```

2. **На ниво метод:**
   ```java
   @PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
   public ResponseEntity<CustomerResponse> getCustomerByUserId(...) { ... }
   ```

3. **Програмно в кода:**
   ```java
   if (isCustomer(authentication)) {
       Long customerId = getCustomerIdFromAuth(authentication);
       if (!shipment.getSenderId().equals(customerId) &&
           !shipment.getRecipientId().equals(customerId)) {
           throw new UnauthorizedException(...);
       }
   }
   ```

**Матрица на достъпа:**

| Ресурс | EMPLOYEE | CUSTOMER |
|--------|----------|----------|
| Companies CRUD | ✓ | ✗ |
| Offices CRUD | ✓ | ✗ |
| Employees CRUD | ✓ | ✗ |
| Customers CRUD | ✓ | ✗ |
| All Shipments | ✓ | Само свои |
| Register Shipment | ✓ | ✗ |
| Update Status | ✓ | ✗ |
| Revenue Report | ✓ | ✗ |
| Own Metrics | ✓ | ✓ |

---

### Q5: Защо използвам BigDecimal вместо double за пари?

**Отговор:**
```java
// ГРЕШНО - double има проблеми с точността
double price = 0.1 + 0.2;  // = 0.30000000000000004

// ПРАВИЛНО - BigDecimal е точен
BigDecimal price = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // = 0.3
```

Използвам `BigDecimal` за всички парични стойности: `price`, `weight`, `basePrice`, `pricePerKg`, `addressDeliveryFee`, `salary`.

---

### Q6: Какво е @Transactional и защо го използвам?

**Отговор:**
```java
@Transactional
public ShipmentResponse registerShipment(...) {
    // Всички операции в този метод са в една транзакция
    // Ако нещо се провали - всичко се отменя (rollback)
}

@Transactional(readOnly = true)
public List<ShipmentResponse> getAllShipmentsReport() {
    // Оптимизация за четене - не се заключва за писане
}
```

---

### Q7: Какво е LAZY loading и защо го използвам?

**Отговор:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sender_id")
private Customer sender;
```

**LAZY** означава, че `sender` се зарежда от БД само когато е нужен, не при всяко зареждане на Shipment. Това подобрява производителността.

---

### Q8: Как работи JWT автентикацията?

**Отговор:**
1. Потребител изпраща `username` и `password` към `/api/auth/login`
2. Сървърът валидира credentials
3. При успех - генерира JWT токен и го връща
4. Клиентът изпраща токена в header при всяка заявка:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```
5. Сървърът валидира токена и определя кой е потребителят

---

### Q9: Защо приходите се броят само от DELIVERED пратки?

**Отговор:** Защото:
- `REGISTERED` - все още не е платена/доставена
- `IN_TRANSIT` - все още не е завършена
- `CANCELLED` - няма плащане
- `DELIVERED` - успешно доставена и платена

```java
@Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s " +
       "WHERE s.status = 'DELIVERED' ...")
BigDecimal calculateRevenueBetweenDates(...);
```

---

### Q10: Какво е Cascade и защо го използвам?

**Отговор:**
```java
@OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
private List<Office> offices;
```

`CascadeType.ALL` означава, че когато запазя/изтрия Company, автоматично се запазват/изтриват и свързаните Office записи.

---

## БЪРЗИ СПРАВКИ

### Местоположение на ключови функционалности:

| Функционалност | Файл | Метод/Място |
|----------------|------|-------------|
| Регистрация | `AuthServiceImpl.java` | `register()` |
| Вход | `AuthServiceImpl.java` | `login()` |
| Валидация на тегло | `ShipmentServiceImpl.java` | `validateWeight()` |
| Валидация на дестинация | `ShipmentServiceImpl.java` | `validateDeliveryDestination()` |
| Валидация на статус | `ShipmentServiceImpl.java` | `validateStatusTransition()` |
| Калкулиране на цена | `PricingServiceImpl.java` | `calculatePrice()` |
| Приходи | `ReportServiceImpl.java` | `getRevenueReport()` |
| Заявка за приходи | `ShipmentRepository.java` | `calculateRevenueBetweenDates()` |
| Контрол на достъп | Контролери | `@PreAuthorize` анотации |
| Entity → DTO | `EntityMapper.java` | `toXxxResponse()` методи |

---

## СЪВЕТИ ЗА ИЗПИТА

1. **Знай къде се намира всеки компонент** - контролер, сървис, repository
2. **Обясни защо използваш слоеста архитектура** - разделение на отговорностите
3. **Покажи валидациите** - DTO, Entity, Service нива
4. **Обясни SOLID принципите** - SRP, OCP, DIP
5. **Знай формулата за цената** и къде се калкулира
6. **Разбирай статус преходите** на пратките
7. **Обясни разликата между EMPLOYEE и CUSTOMER** достъп
8. **Знай защо BigDecimal** за пари
9. **Покажи custom queries** в repositories
10. **Обясни JWT автентикацията** накратко

---

*Документ създаден за подготовка за изпит по проект "Logistics Management System"*
