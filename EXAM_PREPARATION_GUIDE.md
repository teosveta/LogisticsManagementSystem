# ПОДГОТОВКА ЗА ИЗПИТ - Logistics Management System
## Пълно ръководство с подробни обяснения

---

# СЪДЪРЖАНИЕ

1. [Какво представлява проектът?](#1-какво-представлява-проектът)
2. [Архитектура - Как е организиран кодът?](#2-архитектура---как-е-организиран-кодът)
3. [Контролери (Controllers) - Входната точка](#3-контролери-controllers---входната-точка)
4. [Сървиси (Services) - Бизнес логиката](#4-сървиси-services---бизнес-логиката)
5. [Хранилища (Repositories) - Достъп до базата данни](#5-хранилища-repositories---достъп-до-базата-данни)
6. [DTO класове - Пренос на данни](#6-dto-класове---пренос-на-данни)
7. [Entity класове - Моделът на данните](#7-entity-класове---моделът-на-данните)
8. [Enum класове - Фиксирани стойности](#8-enum-класове---фиксирани-стойности)
9. [Помощни класове](#9-помощни-класове)
10. [Сигурност и автентикация](#10-сигурност-и-автентикация)
11. [Пълен списък с въпроси и отговори](#11-пълен-списък-с-въпроси-и-отговори)
12. [Бързи справки](#12-бързи-справки)

---

# 1. КАКВО ПРЕДСТАВЛЯВА ПРОЕКТЪТ?

## 1.1 Описание с прости думи

Това е **система за управление на куриерска фирма** (като Еконт или Спиди).

**Какво може да прави системата:**
- Служители регистрират пратки от клиенти
- Проследява се статуса на пратките (регистрирана → в транзит → доставена)
- Автоматично се калкулира цена на база тегло и тип доставка
- Генерират се справки за приходи, пратки, клиенти

## 1.2 Два типа потребители

| Тип | Какво може да прави | Аналогия |
|-----|---------------------|----------|
| **EMPLOYEE** | Всичко - регистрира пратки, управлява клиенти, вижда справки | Служител на гише в Еконт |
| **CUSTOMER** | Само вижда СВОИТЕ пратки (където е подател или получател) | Клиент, който проверява пратката си онлайн |

## 1.3 Основни понятия

| Понятие | Какво означава | Пример |
|---------|----------------|--------|
| **Company** | Компания (куриерска фирма) | "Еконт ЕООД" |
| **Office** | Офис на компанията | "Офис София Център, ул. Витоша 15" |
| **Employee** | Служител | "Иван Иванов - куриер" |
| **Customer** | Клиент | "Мария Петрова" |
| **Shipment** | Пратка | "Колет 5кг от София до Пловдив" |

---

# 2. АРХИТЕКТУРА - КАК Е ОРГАНИЗИРАН КОДЪТ?

## 2.1 Какво е "слоеста архитектура"?

Представи си **ресторант**:
1. **Клиентът** (Frontend) поръчва на сервитьора
2. **Сервитьорът** (Controller) приема поръчката и я предава на кухнята
3. **Готвачът** (Service) приготвя храната по рецепта
4. **Хладилникът** (Repository) съхранява продуктите
5. **Продуктите** (Entity) са самите съставки

```
┌─────────────────────────────────────────────────────────────────────┐
│                         КЛИЕНТ (Browser/App)                        │
│                    Изпраща заявки, получава отговори                │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ HTTP заявка
┌─────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER (Сервитьор)                         │
│                                                                     │
│  • Приема заявките от клиента                                       │
│  • Проверява дали клиентът има право на достъп                      │
│  • Извиква подходящия Service                                       │
│  • Връща отговор на клиента                                         │
│                                                                     │
│  Папка: src/main/java/com/logistics/controller/                     │
│  Файлове: AuthController, ShipmentController, CompanyController...  │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ извиква метод
┌─────────────────────────────────────────────────────────────────────┐
│                       SERVICE (Готвач)                              │
│                                                                     │
│  • Съдържа БИЗНЕС ЛОГИКАТА (правилата)                              │
│  • Валидира данните                                                 │
│  • Калкулира цени                                                   │
│  • Координира операциите                                            │
│                                                                     │
│  Папка: src/main/java/com/logistics/service/                        │
│         src/main/java/com/logistics/service/impl/                   │
│  Файлове: ShipmentService, PricingService, AuthService...           │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ извиква метод
┌─────────────────────────────────────────────────────────────────────┐
│                     REPOSITORY (Хладилник)                          │
│                                                                     │
│  • Комуникира с базата данни                                        │
│  • Записва, чете, обновява, изтрива данни                           │
│  • Изпълнява SQL заявки                                             │
│                                                                     │
│  Папка: src/main/java/com/logistics/repository/                     │
│  Файлове: ShipmentRepository, UserRepository, CustomerRepository... │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ SQL заявка
┌─────────────────────────────────────────────────────────────────────┐
│                       DATABASE (База данни)                         │
│                                                                     │
│  • Съхранява всички данни                                           │
│  • Таблици: users, shipments, customers, offices, companies...      │
│                                                                     │
│  Entity класове: src/main/java/com/logistics/model/entity/          │
└─────────────────────────────────────────────────────────────────────┘
```

## 2.2 Защо използвам тази архитектура?

**Аналогия с ресторанта:**
- Ако готвачът се разболее, наемаме нов готвач - сервитьорът не се променя
- Ако сменим хладилника, готвачът продължава да готви по същия начин
- Всеки знае какво прави и не се меси в работата на другите

**В програмирането:**
1. **Разделение на отговорностите** - всеки клас има ЕДНА задача
2. **Лесна поддръжка** - промяна в един слой не чупи другите
3. **Лесно тестване** - можем да тестваме всеки слой отделно
4. **Преизползваемост** - един Service може да се извиква от много Controllers

## 2.3 Пълна структура на папките

```
src/main/java/com/logistics/
├── controller/          ← REST endpoints (8 файла)
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── OfficeController.java
│   ├── PricingController.java
│   ├── ReportController.java
│   └── ShipmentController.java
│
├── service/             ← Интерфейси (8 файла)
│   ├── AuthService.java
│   ├── CompanyService.java
│   ├── CustomerService.java
│   ├── EmployeeService.java
│   ├── OfficeService.java
│   ├── PricingService.java
│   ├── ReportService.java
│   └── ShipmentService.java
│
├── service/impl/        ← Имплементации (8 файла)
│   ├── AuthServiceImpl.java
│   ├── CompanyServiceImpl.java
│   ├── CustomerServiceImpl.java
│   ├── EmployeeServiceImpl.java
│   ├── OfficeServiceImpl.java
│   ├── PricingServiceImpl.java
│   ├── ReportServiceImpl.java
│   └── ShipmentServiceImpl.java
│
├── repository/          ← Достъп до БД (7 файла)
│   ├── UserRepository.java
│   ├── CompanyRepository.java
│   ├── OfficeRepository.java
│   ├── EmployeeRepository.java
│   ├── CustomerRepository.java
│   ├── ShipmentRepository.java
│   └── PricingConfigRepository.java
│
├── dto/                 ← Data Transfer Objects (15+ файла)
│   ├── auth/
│   ├── company/
│   ├── customer/
│   ├── employee/
│   ├── office/
│   ├── pricing/
│   ├── report/
│   └── shipment/
│
├── model/
│   ├── entity/          ← Модели за БД (7 файла)
│   │   ├── User.java
│   │   ├── Company.java
│   │   ├── Office.java
│   │   ├── Employee.java
│   │   ├── Customer.java
│   │   ├── Shipment.java
│   │   └── PricingConfig.java
│   │
│   └── enums/           ← Изброени типове (3 файла)
│       ├── Role.java
│       ├── EmployeeType.java
│       └── ShipmentStatus.java
│
└── util/                ← Помощни класове (1 файл)
    └── EntityMapper.java
```

---

# 3. КОНТРОЛЕРИ (Controllers) - ВХОДНАТА ТОЧКА

## 3.1 Какво е Controller?

**Controller** е като **рецепционист в хотел**:
- Посреща "гостите" (HTTP заявките)
- Проверява дали имат резервация (автентикация)
- Насочва ги към правилната стая (Service)
- Връща им отговор

**Технически:** Controller е клас, който дефинира **REST endpoints** - URL адресите, на които приложението отговаря.

## 3.2 Как изглежда един Controller?

```java
@RestController                              // Казва на Spring: "Това е REST контролер"
@RequestMapping("/api/companies")            // Базов път: всички URL-и започват с /api/companies
@PreAuthorize("hasRole('EMPLOYEE')")         // СИГУРНОСТ: само EMPLOYEE има достъп
public class CompanyController {

    private final CompanyService companyService;  // Инжектиран Service

    // Конструктор - Spring автоматично подава CompanyService
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // POST /api/companies - Създаване на компания
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyRequest request) {    // @Valid = валидирай данните
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/companies - Всички компании
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    // GET /api/companies/5 - Компания с ID=5
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    // PUT /api/companies/5 - Обновяване на компания с ID=5
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    // DELETE /api/companies/5 - Изтриване на компания с ID=5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 3.3 Обяснение на анотациите

| Анотация | Какво прави | Пример |
|----------|-------------|--------|
| `@RestController` | Казва на Spring, че това е REST контролер | На класа |
| `@RequestMapping("/api/xxx")` | Базов URL път | `/api/companies` |
| `@GetMapping` | HTTP GET заявка (четене) | Взимане на данни |
| `@PostMapping` | HTTP POST заявка (създаване) | Създаване на нов запис |
| `@PutMapping` | HTTP PUT заявка (обновяване) | Редактиране на съществуващ |
| `@DeleteMapping` | HTTP DELETE заявка (изтриване) | Изтриване на запис |
| `@PathVariable` | Взима стойност от URL | `/api/companies/{id}` → id=5 |
| `@RequestBody` | Взима данни от тялото на заявката | JSON обект |
| `@Valid` | Валидира данните преди обработка | Проверява анотациите в DTO |
| `@PreAuthorize` | Проверява правата за достъп | `hasRole('EMPLOYEE')` |

## 3.4 Всички контролери подробно

### 3.4.1 AuthController.java
**Път:** `/api/auth`
**Предназначение:** Регистрация и вход в системата
**Достъп:** Публичен (без автентикация)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // POST /api/auth/register - Регистрация на нов потребител
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 1. Валидира данните (username, email, password)
        // 2. Проверява дали username/email вече съществуват
        // 3. Криптира паролата
        // 4. Създава User + Customer/Employee запис
        // 5. Генерира JWT токен
        // 6. Връща токена
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    // POST /api/auth/login - Вход в системата
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Проверява username и password
        // 2. Ако са верни - генерира JWT токен
        // 3. Връща токена
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**Защо няма @PreAuthorize?**
Защото потребителят трябва да може да се регистрира и влезе БЕЗ да има токен. Това е "входната врата" на системата.

---

### 3.4.2 ShipmentController.java (НАЙ-ВАЖЕН!)
**Път:** `/api/shipments`
**Предназначение:** Управление на пратки
**Достъп:** Смесен (EMPLOYEE за всичко, CUSTOMER само за своите пратки)

```java
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    // POST /api/shipments - Регистриране на нова пратка
    // САМО EMPLOYEE може да регистрира пратки!
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ShipmentResponse> registerShipment(
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {     // Authentication = кой е влязъл

        String employeeUsername = authentication.getName();
        ShipmentResponse response = shipmentService.registerShipment(request, employeeUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/shipments - Всички пратки (за EMPLOYEE) или само моите (за CUSTOMER)
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('CUSTOMER')")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(Authentication authentication) {

        // Проверяваме дали е клиент
        if (isCustomer(authentication)) {
            // Клиентът вижда САМО своите пратки
            Long customerId = getCustomerIdFromAuth(authentication);
            return ResponseEntity.ok(shipmentService.getShipmentsByCustomerId(customerId));
        }

        // Служителят вижда ВСИЧКИ пратки
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    // GET /api/shipments/5 - Конкретна пратка
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('CUSTOMER')")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable Long id,
            Authentication authentication) {

        ShipmentResponse shipment = shipmentService.getShipmentById(id);

        // Ако е клиент - проверяваме дали пратката е негова
        if (isCustomer(authentication)) {
            Long customerId = getCustomerIdFromAuth(authentication);

            // Клиентът може да види пратка само ако е подател ИЛИ получател
            boolean isSender = shipment.getSenderId().equals(customerId);
            boolean isRecipient = shipment.getRecipientId().equals(customerId);

            if (!isSender && !isRecipient) {
                throw new UnauthorizedException("Нямате достъп до тази пратка");
            }
        }

        return ResponseEntity.ok(shipment);
    }

    // PATCH /api/shipments/5/status - Промяна на статус (само EMPLOYEE)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request));
    }

    // Помощен метод: проверява дали е клиент
    private boolean isCustomer(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    // Помощен метод: взима ID на клиента от токена
    private Long getCustomerIdFromAuth(Authentication auth) {
        String username = auth.getName();
        return customerService.getCustomerIdByUsername(username);
    }
}
```

---

### 3.4.3 PricingController.java
**Път:** `/api/pricing`
**Предназначение:** Управление на цените

```java
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    // GET /api/pricing - Текущите цени (за всички)
    @GetMapping
    @PreAuthorize("isAuthenticated()")  // Всеки влязъл потребител
    public ResponseEntity<PricingInfoResponse> getPricingInfo() {
        return ResponseEntity.ok(pricingService.getPricingInfo());
    }

    // GET /api/pricing/config - Пълна конфигурация (само EMPLOYEE)
    @GetMapping("/config")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PricingConfigResponse> getPricingConfig() {
        return ResponseEntity.ok(pricingService.getActivePricingConfig());
    }

    // PUT /api/pricing/config - Обновяване на цените (само EMPLOYEE)
    @PutMapping("/config")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PricingConfigResponse> updatePricingConfig(
            @Valid @RequestBody PricingConfigRequest request) {
        return ResponseEntity.ok(pricingService.updatePricingConfig(request));
    }
}
```

---

### 3.4.4 ReportController.java
**Път:** `/api/reports`
**Предназначение:** Справки и отчети

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    // GET /api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31
    // Справка за приходи за период (само EMPLOYEE)
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<RevenueResponse> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getRevenueReport(startDate, endDate));
    }

    // GET /api/reports/dashboard - Метрики за dashboard (само EMPLOYEE)
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        return ResponseEntity.ok(reportService.getDashboardMetrics());
    }

    // GET /api/reports/customer-metrics - Метрики за клиент (само своите)
    @GetMapping("/customer-metrics")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerMetricsResponse> getCustomerMetrics(
            Authentication authentication) {
        Long customerId = getCustomerIdFromAuth(authentication);
        return ResponseEntity.ok(reportService.getCustomerMetrics(customerId));
    }
}
```

---

## 3.5 Таблица с всички endpoints

| Controller | Endpoint | Метод | Описание | Достъп |
|------------|----------|-------|----------|--------|
| **Auth** | `/api/auth/register` | POST | Регистрация | Публичен |
| | `/api/auth/login` | POST | Вход | Публичен |
| **Company** | `/api/companies` | POST | Създай компания | EMPLOYEE |
| | `/api/companies` | GET | Всички компании | EMPLOYEE |
| | `/api/companies/{id}` | GET | Компания по ID | EMPLOYEE |
| | `/api/companies/{id}` | PUT | Обнови компания | EMPLOYEE |
| | `/api/companies/{id}` | DELETE | Изтрий компания | EMPLOYEE |
| **Office** | `/api/offices` | POST | Създай офис | EMPLOYEE |
| | `/api/offices` | GET | Всички офиси | EMPLOYEE |
| | `/api/offices/{id}` | GET | Офис по ID | EMPLOYEE |
| | `/api/offices/company/{id}` | GET | Офиси на компания | EMPLOYEE |
| | `/api/offices/{id}` | PUT | Обнови офис | EMPLOYEE |
| | `/api/offices/{id}` | DELETE | Изтрий офис | EMPLOYEE |
| **Employee** | `/api/employees` | POST | Създай служител | EMPLOYEE |
| | `/api/employees` | GET | Всички служители | EMPLOYEE |
| | `/api/employees/{id}` | GET | Служител по ID | EMPLOYEE |
| | `/api/employees/{id}` | PUT | Обнови служител | EMPLOYEE |
| | `/api/employees/{id}` | DELETE | Изтрий служител | EMPLOYEE |
| **Customer** | `/api/customers` | POST | Създай клиент | EMPLOYEE |
| | `/api/customers` | GET | Всички клиенти | EMPLOYEE |
| | `/api/customers/{id}` | GET | Клиент по ID | EMPLOYEE |
| | `/api/customers/user/{id}` | GET | Клиент по User ID | EMPLOYEE/CUSTOMER |
| | `/api/customers/{id}` | PUT | Обнови клиент | EMPLOYEE |
| | `/api/customers/{id}` | DELETE | Изтрий клиент | EMPLOYEE |
| **Shipment** | `/api/shipments` | POST | Регистрирай пратка | EMPLOYEE |
| | `/api/shipments` | GET | Всички/Мои пратки | EMPLOYEE/CUSTOMER |
| | `/api/shipments/{id}` | GET | Пратка по ID | EMPLOYEE/CUSTOMER* |
| | `/api/shipments/{id}/status` | PATCH | Промени статус | EMPLOYEE |
| | `/api/shipments/{id}` | PUT | Обнови пратка | EMPLOYEE |
| | `/api/shipments/{id}` | DELETE | Изтрий пратка | EMPLOYEE |
| **Pricing** | `/api/pricing` | GET | Текущи цени | Всички влезли |
| | `/api/pricing/config` | GET | Конфигурация | EMPLOYEE |
| | `/api/pricing/config` | PUT | Обнови цени | EMPLOYEE |
| **Report** | `/api/reports/employees` | GET | Всички служители | EMPLOYEE |
| | `/api/reports/customers` | GET | Всички клиенти | EMPLOYEE |
| | `/api/reports/shipments` | GET | Всички пратки | EMPLOYEE/CUSTOMER |
| | `/api/reports/revenue` | GET | Приходи | EMPLOYEE |
| | `/api/reports/dashboard` | GET | Dashboard | EMPLOYEE |
| | `/api/reports/customer-metrics` | GET | Клиентски метрики | CUSTOMER |

*CUSTOMER може да вижда само пратки, където е подател или получател

---

# 4. СЪРВИСИ (Services) - БИЗНЕС ЛОГИКАТА

## 4.1 Какво е Service?

**Service** е като **готвач в ресторант**:
- Знае рецептите (бизнес правилата)
- Приготвя храната (обработва данните)
- Не знае откъде идват продуктите (не се интересува от Controller)
- Не знае как работи хладилникът (не знае SQL)

**Технически:** Service съдържа цялата **бизнес логика** - правилата на приложението.

## 4.2 Защо има интерфейс И имплементация?

```
ShipmentService (интерфейс)     →    ShipmentServiceImpl (имплементация)
       ↑                                        ↑
   "Какво прави"                           "Как го прави"
```

**Аналогия:**
- **Интерфейс** = Меню в ресторанта (какво можеш да поръчаш)
- **Имплементация** = Рецептата (как се приготвя)

**Защо е полезно:**
1. Можем да сменим имплементацията без да променяме кода, който я използва
2. Лесно тестване с mock обекти
3. Следва SOLID принципа **Dependency Inversion**

## 4.3 ShipmentServiceImpl - Подробно обяснение

Това е НАЙ-ВАЖНИЯТ service, защото съдържа цялата логика за пратки.

### Метод: registerShipment() - Регистриране на пратка

```java
@Service
@Transactional  // Всичко в един метод е една транзакция
public class ShipmentServiceImpl implements ShipmentService {

    @Override
    public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername) {

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 1: ВАЛИДАЦИЯ НА ВХОДНИТЕ ДАННИ
        // ════════════════════════════════════════════════════════════════

        // Проверка: трябва да има ИЛИ адрес, ИЛИ офис за доставка (не и двете)
        validateDeliveryDestination(request);

        // Проверка: теглото трябва да е между 0.01 и 10000 кг
        validateWeight(request.getWeight());

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 2: НАМИРАНЕ НА СВЪРЗАНИТЕ ОБЕКТИ
        // ════════════════════════════════════════════════════════════════

        // Намери подателя (sender)
        Customer sender = customerRepository.findById(request.getSenderId())
            .orElseThrow(() -> new ResourceNotFoundException("Подателят не е намерен"));

        // Намери получателя (recipient)
        Customer recipient = customerRepository.findById(request.getRecipientId())
            .orElseThrow(() -> new ResourceNotFoundException("Получателят не е намерен"));

        // Намери служителя, който регистрира пратката
        Employee employee = employeeRepository.findByUsername(employeeUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Служителят не е намерен"));

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 3: СЪЗДАВАНЕ НА ПРАТКАТА
        // ════════════════════════════════════════════════════════════════

        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(request.getWeight());
        shipment.setStatus(ShipmentStatus.REGISTERED);  // Начален статус

        // Задаване на офис на произход (от служителя)
        if (employee.getOffice() != null) {
            shipment.setOriginOffice(employee.getOffice());
        }

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 4: ЗАДАВАНЕ НА ДЕСТИНАЦИЯ
        // ════════════════════════════════════════════════════════════════

        boolean isOfficeDelivery = false;

        if (request.isOfficeDelivery()) {
            // Доставка до офис
            Office deliveryOffice = officeRepository.findById(request.getDeliveryOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Офисът не е намерен"));
            shipment.setDeliveryOffice(deliveryOffice);
            isOfficeDelivery = true;
        } else {
            // Доставка до адрес
            shipment.setDeliveryAddress(request.getDeliveryAddress());
        }

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 5: КАЛКУЛИРАНЕ НА ЦЕНАТА
        // ════════════════════════════════════════════════════════════════

        // Делегираме на PricingService (Dependency Inversion!)
        BigDecimal price = pricingService.calculatePrice(request.getWeight(), isOfficeDelivery);
        shipment.setPrice(price);

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 6: ЗАПИС В БАЗАТА ДАННИ
        // ════════════════════════════════════════════════════════════════

        Shipment savedShipment = shipmentRepository.save(shipment);

        // ════════════════════════════════════════════════════════════════
        // СТЪПКА 7: КОНВЕРТИРАНЕ КЪМ DTO И ВРЪЩАНЕ
        // ════════════════════════════════════════════════════════════════

        return EntityMapper.toShipmentResponse(savedShipment);
    }
}
```

### Валидация на дестинация

```java
/**
 * Проверява, че е зададена ТОЧНО ЕДНА дестинация:
 * - ИЛИ адрес за доставка
 * - ИЛИ офис за доставка
 * - НЕ и двете едновременно
 * - НЕ нито едното
 */
private void validateDeliveryDestination(ShipmentRequest request) {
    boolean hasAddress = request.isAddressDelivery();  // deliveryAddress != null
    boolean hasOffice = request.isOfficeDelivery();    // deliveryOfficeId != null

    if (!hasAddress && !hasOffice) {
        // Няма нито адрес, нито офис
        throw new InvalidDataException(
            "Трябва да посочите или адрес за доставка, или офис за доставка");
    }

    if (hasAddress && hasOffice) {
        // Има И адрес, И офис - не може
        throw new InvalidDataException(
            "Не можете да посочите едновременно адрес и офис за доставка");
    }
}
```

### Валидация на тегло

```java
/**
 * Проверява, че теглото е в допустимите граници:
 * - Минимум: 0.01 кг
 * - Максимум: 10000 кг
 */
private void validateWeight(BigDecimal weight) {
    if (weight == null) {
        throw new InvalidDataException("Теглото е задължително");
    }

    // Сравнение с BigDecimal се прави с compareTo, НЕ с < или >
    if (weight.compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidDataException("Теглото трябва да е по-голямо от 0");
    }

    BigDecimal maxWeight = new BigDecimal("10000.00");
    if (weight.compareTo(maxWeight) > 0) {
        throw new InvalidDataException("Теглото не може да надвишава 10000 кг");
    }
}
```

### Валидация на преходи между статуси

```java
/**
 * Проверява дали преходът от един статус към друг е валиден.
 *
 * Валидни преходи:
 *   REGISTERED → IN_TRANSIT (пратката тръгва)
 *   REGISTERED → CANCELLED (пратката се отказва преди изпращане)
 *   IN_TRANSIT → DELIVERED (пратката е доставена)
 *   IN_TRANSIT → CANCELLED (пратката се отказва по време на доставка)
 *
 * Невалидни преходи:
 *   DELIVERED → (нищо) - финален статус
 *   CANCELLED → (нищо) - финален статус
 */
private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {

    // Финални статуси - не могат да се променят
    if (currentStatus == ShipmentStatus.DELIVERED) {
        throw new InvalidStatusTransitionException(
            "Доставената пратка не може да променя статуса си");
    }

    if (currentStatus == ShipmentStatus.CANCELLED) {
        throw new InvalidStatusTransitionException(
            "Отказаната пратка не може да променя статуса си");
    }

    // От REGISTERED може да отиде само към IN_TRANSIT или CANCELLED
    if (currentStatus == ShipmentStatus.REGISTERED) {
        if (newStatus != ShipmentStatus.IN_TRANSIT && newStatus != ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "Регистрирана пратка може да премине само към 'В транзит' или 'Отказана'");
        }
    }

    // От IN_TRANSIT може да отиде само към DELIVERED или CANCELLED
    if (currentStatus == ShipmentStatus.IN_TRANSIT) {
        if (newStatus != ShipmentStatus.DELIVERED && newStatus != ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "Пратка в транзит може да премине само към 'Доставена' или 'Отказана'");
        }
    }
}
```

**Диаграма на статусите:**
```
                    ┌─────────────┐
                    │ REGISTERED  │  (Начален статус)
                    │ (Регистрирана)│
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            │            ▼
      ┌───────────┐        │    ┌───────────┐
      │IN_TRANSIT │        │    │ CANCELLED │
      │(В транзит)│        │    │ (Отказана)│
      └─────┬─────┘        │    └───────────┘
            │              │          ▲
            │              └──────────┘
            │
    ┌───────┼───────┐
    │               │
    ▼               ▼
┌───────────┐ ┌───────────┐
│ DELIVERED │ │ CANCELLED │
│(Доставена)│ │ (Отказана)│
└───────────┘ └───────────┘
 (Финален)     (Финален)
```

---

## 4.4 PricingServiceImpl - Калкулиране на цени

### Формула за изчисляване

```java
@Service
public class PricingServiceImpl implements PricingService {

    /**
     * Изчислява цената на пратка.
     *
     * ФОРМУЛА:
     *   Цена = Базова цена + (Тегло × Цена на кг) + Такса за доставка
     *
     * Където:
     *   - Базова цена: фиксирана сума за всяка пратка
     *   - Тегло × Цена на кг: зависи от теглото
     *   - Такса за доставка: 0 за офис, addressDeliveryFee за адрес
     */
    @Override
    public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {

        // Взимаме активната конфигурация от базата данни
        PricingConfig config = getActiveConfig();

        // Започваме с базовата цена
        BigDecimal total = config.getBasePrice();

        // Добавяме: тегло × цена на кг
        BigDecimal weightCost = weight.multiply(config.getPricePerKg());
        total = total.add(weightCost);

        // Добавяме такса за доставка до адрес (ако не е до офис)
        if (!isOfficeDelivery) {
            total = total.add(config.getAddressDeliveryFee());
        }

        // Закръгляме до 2 знака след десетичната точка
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Взима активната ценова конфигурация.
     * Винаги има ТОЧНО ЕДНА активна конфигурация в базата.
     */
    private PricingConfig getActiveConfig() {
        return pricingConfigRepository.findByActiveTrue()
            .orElseThrow(() -> new InvalidDataException(
                "Няма активна ценова конфигурация!"));
    }
}
```

### Примерни изчисления

Нека конфигурацията е:
- `basePrice` = 5.00 лв
- `pricePerKg` = 2.00 лв
- `addressDeliveryFee` = 10.00 лв

| Тегло | Дестинация | Изчисление | Резултат |
|-------|------------|------------|----------|
| 1 кг | Офис | 5.00 + (1 × 2.00) + 0 | **7.00 лв** |
| 1 кг | Адрес | 5.00 + (1 × 2.00) + 10.00 | **17.00 лв** |
| 5 кг | Офис | 5.00 + (5 × 2.00) + 0 | **15.00 лв** |
| 5 кг | Адрес | 5.00 + (5 × 2.00) + 10.00 | **25.00 лв** |
| 10 кг | Офис | 5.00 + (10 × 2.00) + 0 | **25.00 лв** |
| 10 кг | Адрес | 5.00 + (10 × 2.00) + 10.00 | **35.00 лв** |
| 0.5 кг | Офис | 5.00 + (0.5 × 2.00) + 0 | **6.00 лв** |

---

## 4.5 ReportServiceImpl - Справки

```java
@Service
@Transactional(readOnly = true)  // Оптимизация: само четене, без заключване
public class ReportServiceImpl implements ReportService {

    /**
     * Изчислява общите приходи за период.
     * ВАЖНО: Само DELIVERED пратки се броят като приход!
     */
    @Override
    public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {

        // Конвертираме датите към LocalDateTime (начало и край на деня)
        LocalDateTime startDateTime = startDate.atStartOfDay();           // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);           // 23:59:59

        // Извикваме repository метода (SQL заявка)
        BigDecimal totalRevenue = shipmentRepository
            .calculateRevenueBetweenDates(startDateTime, endDateTime);

        Long deliveredCount = shipmentRepository
            .countDeliveredBetweenDates(startDateTime, endDateTime);

        return new RevenueResponse(startDate, endDate, totalRevenue, deliveredCount);
    }

    /**
     * Метрики за dashboard.
     */
    @Override
    public DashboardMetricsResponse getDashboardMetrics() {
        Long totalShipments = shipmentRepository.count();
        Long pendingShipments = shipmentRepository.countInTransitShipments();  // REGISTERED + IN_TRANSIT
        Long deliveredShipments = shipmentRepository.countByStatus(ShipmentStatus.DELIVERED);
        BigDecimal totalRevenue = shipmentRepository.calculateTotalRevenue();

        return new DashboardMetricsResponse(
            totalShipments, pendingShipments, deliveredShipments, totalRevenue);
    }
}
```

---

## 4.6 Таблица на всички Service методи

| Service | Метод | Описание |
|---------|-------|----------|
| **AuthService** | `register(request)` | Регистрира нов потребител |
| | `login(request)` | Влизане и получаване на токен |
| **ShipmentService** | `registerShipment(request, username)` | Регистрира нова пратка |
| | `getShipmentById(id)` | Връща пратка по ID |
| | `getAllShipments()` | Всички пратки |
| | `getShipmentsByCustomerId(id)` | Пратки на клиент (подател ИЛИ получател) |
| | `updateShipmentStatus(id, request)` | Променя статуса |
| | `updateShipment(id, request)` | Обновява пратка |
| | `deleteShipment(id)` | Изтрива пратка |
| **PricingService** | `calculatePrice(weight, isOffice)` | Калкулира цена |
| | `getActivePricingConfig()` | Текуща конфигурация |
| | `updatePricingConfig(request)` | Обновява цените |
| **ReportService** | `getRevenueReport(start, end)` | Приходи за период |
| | `getDashboardMetrics()` | Метрики за dashboard |
| | `getCustomerMetrics(id)` | Метрики за клиент |
| **CompanyService** | CRUD операции | Управление на компании |
| **OfficeService** | CRUD операции | Управление на офиси |
| **EmployeeService** | CRUD операции | Управление на служители |
| **CustomerService** | CRUD операции | Управление на клиенти |

---

# 5. ХРАНИЛИЩА (Repositories) - ДОСТЪП ДО БАЗАТА ДАННИ

## 5.1 Какво е Repository?

**Repository** е като **библиотекар**:
- Знае къде се намират книгите (данните)
- Може да намери, добави или премахне книга
- Не се интересува КОЙ иска книгата или ЗАЩО

**Технически:** Repository е интерфейс, който наследява `JpaRepository` и предоставя методи за работа с базата данни.

## 5.2 Как работи Spring Data JPA?

Spring Data JPA е магия! Ти дефинираш САМО интерфейс, а Spring създава имплементацията автоматично.

```java
// Ти пишеш само това:
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

// Spring автоматично създава SQL:
// SELECT * FROM users WHERE username = ?
```

## 5.3 Видове заявки

### Тип 1: Автоматични (от JpaRepository)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Тези методи идват БЕЗПЛАТНО от JpaRepository:

    // SELECT * FROM users WHERE id = ?
    Optional<User> findById(Long id);

    // SELECT * FROM users
    List<User> findAll();

    // INSERT INTO users (...) VALUES (...)
    // или UPDATE users SET ... WHERE id = ?
    User save(User user);

    // DELETE FROM users WHERE id = ?
    void deleteById(Long id);

    // SELECT COUNT(*) FROM users
    long count();

    // SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)
    boolean existsById(Long id);
}
```

### Тип 2: По конвенция (method naming)

Spring разбира какво искаш от ИМЕТО на метода!

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // findBy + FieldName → WHERE field_name = ?
    Optional<Customer> findByUserId(Long userId);
    // SQL: SELECT * FROM customers WHERE user_id = ?

    // findBy + Field + And + Field → WHERE field1 = ? AND field2 = ?
    List<Customer> findByPhoneAndAddress(String phone, String address);
    // SQL: SELECT * FROM customers WHERE phone = ? AND address = ?

    // existsBy + Field → SELECT EXISTS(...)
    boolean existsByUserId(Long userId);
    // SQL: SELECT EXISTS(SELECT 1 FROM customers WHERE user_id = ?)

    // countBy + Field → SELECT COUNT(*)
    long countByAddress(String address);
    // SQL: SELECT COUNT(*) FROM customers WHERE address = ?
}
```

### Тип 3: Custom @Query (когато конвенцията не стига)

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Заявка с JOIN към друга таблица
    @Query("SELECT e FROM Employee e WHERE e.user.username = :username")
    Optional<Employee> findByUsername(@Param("username") String username);
    // SQL: SELECT e.* FROM employees e
    //      JOIN users u ON e.user_id = u.id
    //      WHERE u.username = ?

    // Заявка с агрегация
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.employeeType = :type")
    long countByType(@Param("type") EmployeeType type);
}
```

## 5.4 ShipmentRepository - Най-сложният

```java
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // ═══════════════════════════════════════════════════════════════════
    // ПРОСТИ ЗАЯВКИ (по конвенция)
    // ═══════════════════════════════════════════════════════════════════

    // Пратки, регистрирани от служител
    List<Shipment> findByRegisteredById(Long employeeId);
    // SQL: SELECT * FROM shipments WHERE registered_by_id = ?

    // Пратки, изпратени от клиент
    List<Shipment> findBySenderId(Long senderId);
    // SQL: SELECT * FROM shipments WHERE sender_id = ?

    // Пратки, получени от клиент
    List<Shipment> findByRecipientId(Long recipientId);
    // SQL: SELECT * FROM shipments WHERE recipient_id = ?

    // Пратки по статус
    List<Shipment> findByStatus(ShipmentStatus status);
    // SQL: SELECT * FROM shipments WHERE status = ?

    // Брой пратки по статус
    Long countByStatus(ShipmentStatus status);
    // SQL: SELECT COUNT(*) FROM shipments WHERE status = ?

    // ═══════════════════════════════════════════════════════════════════
    // СЛОЖНИ ЗАЯВКИ (с @Query)
    // ═══════════════════════════════════════════════════════════════════

    // Пратки на клиент (подател ИЛИ получател)
    @Query("SELECT s FROM Shipment s WHERE s.sender.id = :customerId OR s.recipient.id = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);
    // SQL: SELECT * FROM shipments WHERE sender_id = ? OR recipient_id = ?

    // Чакащи пратки (НЕ доставени и НЕ отказани)
    @Query("SELECT s FROM Shipment s WHERE s.status != 'DELIVERED' AND s.status != 'CANCELLED'")
    List<Shipment> findAllPendingShipments();

    // Доставени пратки за период
    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    List<Shipment> findDeliveredBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // ═══════════════════════════════════════════════════════════════════
    // ЗАЯВКИ ЗА ИЗЧИСЛЕНИЯ (връщат числа, не обекти)
    // ═══════════════════════════════════════════════════════════════════

    // ИЗЧИСЛЯВАНЕ НА ПРИХОДИ ЗА ПЕРИОД
    // COALESCE връща 0 ако няма резултати (вместо NULL)
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s " +
           "WHERE s.status = 'DELIVERED' " +
           "AND s.deliveredAt >= :startDate AND s.deliveredAt <= :endDate")
    BigDecimal calculateRevenueBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    // SQL: SELECT COALESCE(SUM(price), 0) FROM shipments
    //      WHERE status = 'DELIVERED'
    //      AND delivered_at >= ? AND delivered_at <= ?

    // ОБЩ ПРИХОД (само от доставени)
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    // БРОЙ ПРАТКИ В ТРАНЗИТ
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'REGISTERED' OR s.status = 'IN_TRANSIT'")
    Long countInTransitShipments();

    // ОБЩО ПОХАРЧЕНО ОТ КЛИЕНТ
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s " +
           "WHERE s.sender.id = :senderId AND s.status = 'DELIVERED'")
    BigDecimal calculateTotalSpentByCustomer(@Param("senderId") Long senderId);
}
```

## 5.5 PricingConfigRepository

```java
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {

    // Намери активната конфигурация (винаги има САМО една)
    Optional<PricingConfig> findByActiveTrue();
    // SQL: SELECT * FROM pricing_config WHERE active = true

    // Деактивирай ВСИЧКИ конфигурации
    // @Modifying = това е UPDATE/DELETE заявка, не SELECT
    @Modifying
    @Query("UPDATE PricingConfig p SET p.active = false WHERE p.active = true")
    void deactivateAll();
    // SQL: UPDATE pricing_config SET active = false WHERE active = true
}
```

**Защо deactivateAll()?**
Когато обновяваме цените:
1. Първо деактивираме старата конфигурация
2. После създаваме нова активна конфигурация

Така имаме история на цените и винаги ТОЧНО ЕДНА активна.

---

# 6. DTO КЛАСОВЕ - ПРЕНОС НА ДАННИ

## 6.1 Какво е DTO?

**DTO (Data Transfer Object)** е като **пощенски плик**:
- Съдържа само информацията, която искаме да изпратим
- Не съдържа бизнес логика
- Различен е от Entity (който е за базата данни)

**Защо не изпращаме директно Entity?**
1. **Сигурност** - Entity може да съдържа парола или други чувствителни данни
2. **Гъвкавост** - Можем да форматираме данните различно за различни клиенти
3. **Разделение** - Entity е за БД, DTO е за API

## 6.2 Request vs Response DTO

```
Клиент изпраща:                    Сървър връща:
┌──────────────────┐               ┌──────────────────┐
│  ShipmentRequest │     →→→       │ ShipmentResponse │
├──────────────────┤               ├──────────────────┤
│ senderId: 1      │               │ id: 100          │
│ recipientId: 2   │               │ senderId: 1      │
│ weight: 5.0      │               │ senderName: "..." │
│ deliveryAddress  │               │ recipientId: 2   │
└──────────────────┘               │ recipientName    │
                                   │ weight: 5.0      │
                                   │ price: 15.00     │ ← Калкулирана!
                                   │ status: REG...   │
                                   │ registeredAt     │
                                   └──────────────────┘
```

## 6.3 Примери за DTO класове

### RegisterRequest.java

```java
/**
 * DTO за регистрация на нов потребител.
 * Съдържа валидационни анотации.
 */
public class RegisterRequest {

    @NotBlank(message = "Потребителското име е задължително")
    @Size(min = 3, max = 50, message = "Потребителското име трябва да е между 3 и 50 символа")
    private String username;

    @NotBlank(message = "Имейлът е задължителен")
    @Email(message = "Невалиден формат на имейл")
    private String email;

    @NotBlank(message = "Паролата е задължителна")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа")
    private String password;

    @NotNull(message = "Ролята е задължителна")
    private Role role;  // EMPLOYEE или CUSTOMER

    // Getters и Setters...
}
```

### ShipmentRequest.java

```java
/**
 * DTO за създаване/обновяване на пратка.
 */
public class ShipmentRequest {

    @NotNull(message = "ID на подател е задължително")
    private Long senderId;

    @NotNull(message = "ID на получател е задължително")
    private Long recipientId;

    private Long originOfficeId;  // Опционално - взима се от служителя

    // ВАЖНО: Едно от двете трябва да е попълнено!
    @Size(max = 255, message = "Адресът не може да надвишава 255 символа")
    private String deliveryAddress;    // За доставка до адрес

    private Long deliveryOfficeId;     // За доставка до офис

    @NotNull(message = "Теглото е задължително")
    @DecimalMin(value = "0.01", message = "Теглото трябва да е поне 0.01 кг")
    @DecimalMax(value = "10000.00", message = "Теглото не може да надвишава 10000 кг")
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

### ShipmentResponse.java

```java
/**
 * DTO за връщане на информация за пратка.
 * Съдържа "разгънати" данни за удобство на клиента.
 */
public class ShipmentResponse {

    private Long id;

    // Подател
    private Long senderId;
    private String senderName;      // Разгънато от Customer → User
    private String senderEmail;

    // Получател
    private Long recipientId;
    private String recipientName;
    private String recipientEmail;

    // Регистрирал
    private Long registeredById;
    private String registeredByName;

    // Офиси
    private Long originOfficeId;
    private String originOfficeName;

    // Дестинация
    private String deliveryAddress;
    private Long deliveryOfficeId;
    private String deliveryOfficeName;
    private String destinationOfficeName;  // Alias за frontend

    // Детайли
    private BigDecimal weight;
    private BigDecimal price;           // Калкулирана автоматично!
    private ShipmentStatus status;
    private boolean deliverToAddress;   // Флаг за frontend

    // Времена
    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;
}
```

## 6.4 Валидационни анотации

| Анотация | Какво проверява | Пример |
|----------|-----------------|--------|
| `@NotNull` | Не е null | `private Long id;` |
| `@NotBlank` | Не е null, не е празен стринг | `private String name;` |
| `@Size(min, max)` | Дължина на стринг | `@Size(min=3, max=50)` |
| `@Email` | Валиден формат на имейл | `@Email private String email;` |
| `@Min(value)` | Минимална стойност (число) | `@Min(0) private int age;` |
| `@Max(value)` | Максимална стойност (число) | `@Max(150) private int age;` |
| `@DecimalMin` | Минимална стойност (BigDecimal) | `@DecimalMin("0.01")` |
| `@DecimalMax` | Максимална стойност (BigDecimal) | `@DecimalMax("10000")` |
| `@Pattern(regexp)` | Съответства на regex | `@Pattern(regexp="[0-9]+")` |
| `@Past` | Дата в миналото | `@Past private LocalDate birthDate;` |
| `@Future` | Дата в бъдещето | `@Future private LocalDate deadline;` |

---

# 7. ENTITY КЛАСОВЕ - МОДЕЛЪТ НА ДАННИТЕ

## 7.1 Какво е Entity?

**Entity** е като **чертеж на таблица** в базата данни:
- Всяко поле = колона в таблицата
- Всеки обект = ред в таблицата
- Релациите показват връзките между таблиците

## 7.2 Релации между Entity класовете

```
┌─────────────┐
│    User     │ ← Потребител (за автентикация)
├─────────────┤
│ id          │
│ username    │
│ email       │
│ password    │
│ role        │
└──────┬──────┘
       │
       │ 1:1 (Един потребител е ИЛИ служител, ИЛИ клиент)
       │
       ├───────────────┬──────────────────┐
       │               │                  │
       ▼               ▼                  │
┌─────────────┐  ┌─────────────┐          │
│  Employee   │  │  Customer   │          │
├─────────────┤  ├─────────────┤          │
│ id          │  │ id          │          │
│ user (FK)   │  │ user (FK)   │          │
│ company (FK)│  │ phone       │          │
│ office (FK) │  │ address     │          │
│ employeeType│  └──────┬──────┘          │
│ hireDate    │         │                 │
│ salary      │         │ 1:M             │
└──────┬──────┘         │                 │
       │                │                 │
       │                ▼                 │
       │         ┌─────────────┐          │
       │         │  Shipment   │◄─────────┘
       │         ├─────────────┤    1:M (регистрирал)
       │         │ id          │
       └────────►│ sender (FK) │
  1:M (регист.)  │ recipient   │
                 │ registeredBy│
                 │ originOffice│
                 │ deliveryAddr│
                 │ deliveryOff │
                 │ weight      │
                 │ price       │
                 │ status      │
                 └─────────────┘

┌─────────────┐        ┌─────────────┐
│   Company   │ 1:M    │   Office    │
├─────────────┤───────►├─────────────┤
│ id          │        │ id          │
│ name        │        │ company (FK)│
│ regNumber   │        │ name        │
│ address     │        │ address     │
└─────────────┘        │ city        │
                       │ country     │
                       └─────────────┘

┌─────────────────┐
│ PricingConfig   │ ← Конфигурация на цените
├─────────────────┤
│ id              │
│ basePrice       │
│ pricePerKg      │
│ addressDelFee   │
│ active          │
└─────────────────┘
```

## 7.3 User.java - Детайлно обяснение

```java
@Entity                          // Казва на JPA: "Това е Entity клас"
@Table(name = "users")           // Името на таблицата в БД
public class User {

    @Id                                              // Първичен ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @NotBlank                    // Валидация: не може да е празно
    @Size(min = 3, max = 50)     // Валидация: 3-50 символа
    @Column(unique = true, nullable = false)  // DB constraint: уникално, не NULL
    private String username;

    @NotBlank
    @Email                       // Валидация: валиден email формат
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;     // BCrypt криптирана парола

    @NotNull
    @Enumerated(EnumType.STRING) // Запазва enum като текст ('EMPLOYEE', 'CUSTOMER')
    @Column(nullable = false)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════════
    // РЕЛАЦИИ
    // ═══════════════════════════════════════════════════════════════════

    // Един User има ЕДИН Employee (или нищо)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Employee employee;

    // Един User има ЕДИН Customer (или нищо)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Customer customer;

    // ═══════════════════════════════════════════════════════════════════
    // LIFECYCLE CALLBACKS
    // ═══════════════════════════════════════════════════════════════════

    @PrePersist  // Извиква се ПРЕДИ запис в БД
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate   // Извиква се ПРЕДИ обновяване в БД
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## 7.4 Shipment.java - Най-важният Entity

```java
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════════════
    // РЕЛАЦИИ - КОЙ УЧАСТВА В ПРАТКАТА
    // ═══════════════════════════════════════════════════════════════════

    // Подател (клиент, който изпраща)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)  // Много пратки от един подател
    @JoinColumn(name = "sender_id", nullable = false)
    private Customer sender;

    // Получател (клиент, който получава)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Customer recipient;

    // Служител, регистрирал пратката
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id", nullable = false)
    private Employee registeredBy;

    // ═══════════════════════════════════════════════════════════════════
    // ОФИСИ
    // ═══════════════════════════════════════════════════════════════════

    // Офис, от който е изпратена (опционално)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_office_id")
    private Office originOffice;

    // ═══════════════════════════════════════════════════════════════════
    // ДЕСТИНАЦИЯ - ИЛИ АДРЕС, ИЛИ ОФИС (не и двете!)
    // ═══════════════════════════════════════════════════════════════════

    // За доставка до адрес
    @Column(name = "delivery_address")
    private String deliveryAddress;

    // За доставка до офис
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_office_id")
    private Office deliveryOffice;

    // ═══════════════════════════════════════════════════════════════════
    // ДЕТАЙЛИ НА ПРАТКАТА
    // ═══════════════════════════════════════════════════════════════════

    // Тегло в килограми
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("10000.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    // Цена (калкулира се автоматично!)
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Статус
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.REGISTERED;

    // ═══════════════════════════════════════════════════════════════════
    // ВРЕМЕНА
    // ═══════════════════════════════════════════════════════════════════

    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════════
    // ПОМОЩНИ МЕТОДИ
    // ═══════════════════════════════════════════════════════════════════

    public boolean isOfficeDelivery() {
        return deliveryOffice != null;
    }

    public boolean isAddressDelivery() {
        return deliveryAddress != null && !deliveryAddress.isBlank();
    }

    public String getDeliveryDestination() {
        if (isOfficeDelivery()) {
            return "Офис: " + deliveryOffice.getName();
        } else if (isAddressDelivery()) {
            return "Адрес: " + deliveryAddress;
        }
        return "Не е посочено";
    }

    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == ShipmentStatus.CANCELLED;
    }

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Ако статусът е DELIVERED, запиши времето
        if (status == ShipmentStatus.DELIVERED && deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
    }
}
```

## 7.5 Защо BigDecimal за пари?

```java
// ГРЕШНО - double има проблеми с точността
double price1 = 0.1;
double price2 = 0.2;
double total = price1 + price2;
System.out.println(total);  // 0.30000000000000004  ← ГРЕШНО!

// ПРАВИЛНО - BigDecimal е точен
BigDecimal price1 = new BigDecimal("0.1");
BigDecimal price2 = new BigDecimal("0.2");
BigDecimal total = price1.add(price2);
System.out.println(total);  // 0.3  ← ПРАВИЛНО!
```

**Правило:** Винаги използвай `BigDecimal` за пари, тегла и други стойности, които изискват точност!

## 7.6 Какво е FetchType.LAZY?

```java
@ManyToOne(fetch = FetchType.LAZY)
private Customer sender;
```

**LAZY** означава: "Не зареждай `sender` от базата данни, докато не го поискам."

**Аналогия:** Като да кажеш на библиотекаря "Донеси ми книгата чак когато реша да я чета."

**Защо е полезно:** Ако имаш 1000 пратки и искаш само ID-тата им, няма смисъл да зареждаш всички sender/recipient обекти.

---

# 8. ENUM КЛАСОВЕ - ФИКСИРАНИ СТОЙНОСТИ

## 8.1 Какво е Enum?

**Enum** е списък от **фиксирани, предварително дефинирани стойности**.

**Аналогия:** Светофарът има само 3 цвята - ЧЕРВЕНО, ЖЪЛТО, ЗЕЛЕНО. Не може да е СИНЬО.

## 8.2 Role.java

```java
/**
 * Роли на потребителите в системата.
 * Определя какво може да прави потребителят.
 */
public enum Role {
    EMPLOYEE,   // Служител - пълен достъп
    CUSTOMER    // Клиент - ограничен достъп (само собствени пратки)
}
```

**Къде се използва:**
- При регистрация: `request.getRole()`
- При проверка на достъп: `@PreAuthorize("hasRole('EMPLOYEE')")`

## 8.3 EmployeeType.java

```java
/**
 * Типове служители.
 */
public enum EmployeeType {
    COURIER,       // Куриер - доставя пратки
    OFFICE_STAFF   // Офис служител - обслужва клиенти на гише
}
```

## 8.4 ShipmentStatus.java

```java
/**
 * Статуси на пратка.
 * Определя в какво състояние е пратката.
 */
public enum ShipmentStatus {
    REGISTERED,  // Регистрирана - току-що създадена
    IN_TRANSIT,  // В транзит - на път към получателя
    DELIVERED,   // Доставена - успешно получена (ФИНАЛЕН)
    CANCELLED    // Отказана - анулирана (ФИНАЛЕН)
}
```

**Жизнен цикъл:**
```
Нова пратка
     │
     ▼
REGISTERED ──────► IN_TRANSIT ──────► DELIVERED ✓
     │                   │
     │                   │
     ▼                   ▼
CANCELLED ✗         CANCELLED ✗
```

---

# 9. ПОМОЩНИ КЛАСОВЕ

## 9.1 EntityMapper.java

**Какво прави:** Конвертира Entity обекти към DTO обекти.

**Защо е нужен:**
- Entity съдържа много повече информация (релации, lazy loading)
- DTO съдържа само това, което искаме да покажем на клиента

```java
/**
 * Помощен клас за конвертиране на Entity към DTO.
 * Следва Single Responsibility Principle.
 */
public final class EntityMapper {

    // Приватен конструктор - не може да се инстанцира
    private EntityMapper() {}

    /**
     * Конвертира Shipment Entity към ShipmentResponse DTO.
     */
    public static ShipmentResponse toShipmentResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();

        // Основни полета
        response.setId(shipment.getId());
        response.setWeight(shipment.getWeight());
        response.setPrice(shipment.getPrice());
        response.setStatus(shipment.getStatus());

        // Подател - "разгъваме" данните
        response.setSenderId(shipment.getSender().getId());
        response.setSenderName(shipment.getSender().getUser().getUsername());
        response.setSenderEmail(shipment.getSender().getUser().getEmail());

        // Получател
        response.setRecipientId(shipment.getRecipient().getId());
        response.setRecipientName(shipment.getRecipient().getUser().getUsername());
        response.setRecipientEmail(shipment.getRecipient().getUser().getEmail());

        // Служител (null-safe проверка)
        if (shipment.getRegisteredBy() != null) {
            response.setRegisteredById(shipment.getRegisteredBy().getId());
            response.setRegisteredByName(shipment.getRegisteredBy().getUser().getUsername());
        }

        // Офиси (null-safe)
        if (shipment.getOriginOffice() != null) {
            response.setOriginOfficeId(shipment.getOriginOffice().getId());
            response.setOriginOfficeName(shipment.getOriginOffice().getName());
        }

        // Дестинация
        if (shipment.isOfficeDelivery()) {
            response.setDeliveryOfficeId(shipment.getDeliveryOffice().getId());
            response.setDeliveryOfficeName(shipment.getDeliveryOffice().getName());
            response.setDeliverToAddress(false);
        } else {
            response.setDeliveryAddress(shipment.getDeliveryAddress());
            response.setDeliverToAddress(true);
        }

        // Времена
        response.setRegisteredAt(shipment.getRegisteredAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setUpdatedAt(shipment.getUpdatedAt());

        return response;
    }

    // Подобни методи за Company, Office, Employee, Customer...
}
```

---

# 10. СИГУРНОСТ И АВТЕНТИКАЦИЯ

## 10.1 Как работи JWT автентикацията?

**JWT (JSON Web Token)** е като **пропуск за концерт**:
1. Купуваш билет (login) → получаваш пропуск (token)
2. На всеки вход показваш пропуска
3. Охраната проверява дали е истински
4. Ако е валиден - пускат те

```
┌────────────────────────────────────────────────────────────────────────┐
│                         ПРОЦЕС НА АВТЕНТИКАЦИЯ                         │
└────────────────────────────────────────────────────────────────────────┘

1. ВХОД (Login)
   ┌─────────┐                              ┌─────────┐
   │ Клиент  │ ──── POST /api/auth/login ──►│ Сървър  │
   │         │      {username, password}    │         │
   │         │◄──── {token: "eyJ..."}  ─────│         │
   └─────────┘                              └─────────┘

2. ЗАЩИТЕНА ЗАЯВКА (Authenticated Request)
   ┌─────────┐                              ┌─────────┐
   │ Клиент  │ ──── GET /api/shipments ────►│ Сървър  │
   │         │      Header: Authorization:  │         │
   │         │      Bearer eyJ...           │         │
   │         │◄──── [списък с пратки] ──────│         │
   └─────────┘                              └─────────┘

3. НЕВАЛИДЕН/ЛИПСВАЩ ТОКЕН
   ┌─────────┐                              ┌─────────┐
   │ Клиент  │ ──── GET /api/shipments ────►│ Сървър  │
   │         │      (без токен)             │         │
   │         │◄──── 401 Unauthorized ───────│         │
   └─────────┘                              └─────────┘
```

## 10.2 Какво съдържа JWT токенът?

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwicm9sZSI6IkVNUExPWUVFIiwiZXhwIjoxNzA0MDY3MjAwfQ.xyz

       │                              │                                           │
       ▼                              ▼                                           ▼
   HEADER                          PAYLOAD                                   SIGNATURE
   (алгоритъм)                    (данни)                                (подпис за валидация)

PAYLOAD декодиран:
{
  "sub": "john",           ← username
  "role": "EMPLOYEE",      ← роля
  "exp": 1704067200        ← изтича на (Unix timestamp)
}
```

## 10.3 @PreAuthorize анотации

```java
// Само EMPLOYEE има достъп
@PreAuthorize("hasRole('EMPLOYEE')")

// Само CUSTOMER има достъп
@PreAuthorize("hasRole('CUSTOMER')")

// EMPLOYEE или CUSTOMER
@PreAuthorize("hasRole('EMPLOYEE') or hasRole('CUSTOMER')")

// Всеки влязъл потребител
@PreAuthorize("isAuthenticated()")

// Само за конкретен потребител (по-сложно)
@PreAuthorize("#username == authentication.name")
```

## 10.4 Матрица на достъпа

| Ресурс | EMPLOYEE | CUSTOMER | Неавтентикиран |
|--------|:--------:|:--------:|:--------------:|
| Регистрация | - | - | ✓ |
| Вход | - | - | ✓ |
| Компании CRUD | ✓ | ✗ | ✗ |
| Офиси CRUD | ✓ | ✗ | ✗ |
| Служители CRUD | ✓ | ✗ | ✗ |
| Клиенти CRUD | ✓ | ✗ | ✗ |
| Регистрирай пратка | ✓ | ✗ | ✗ |
| Виж всички пратки | ✓ | Само свои | ✗ |
| Промени статус | ✓ | ✗ | ✗ |
| Приходи | ✓ | ✗ | ✗ |
| Dashboard | ✓ | ✗ | ✗ |
| Клиентски метрики | ✓ | Само свои | ✗ |

---

# 11. ПЪЛЕН СПИСЪК С ВЪПРОСИ И ОТГОВОРИ

## Въпрос 1: Къде са заявките към базата данни?

**Отговор:**

Заявките са в **Repository** файловете в папка `src/main/java/com/logistics/repository/`

**3 типа заявки:**

| Тип | Пример | Обяснение |
|-----|--------|-----------|
| **Автоматични** | `findById(1L)` | Идват от JpaRepository |
| **По конвенция** | `findByUsername("john")` | Spring разбира от името |
| **Custom @Query** | `@Query("SELECT...")` | Пишем ръчно JPQL |

**Пример за custom query:**
```java
// Файл: ShipmentRepository.java
@Query("SELECT COALESCE(SUM(s.price), 0) FROM Shipment s WHERE s.status = 'DELIVERED'")
BigDecimal calculateTotalRevenue();
```

---

## Въпрос 2: Имам ли валидация? Къде?

**Отговор:**

Да, на **3 нива:**

| Ниво | Къде | Какво проверява | Пример |
|------|------|-----------------|--------|
| **DTO** | Request класове | Формат на входни данни | `@NotNull`, `@Email`, `@Size` |
| **Service** | ServiceImpl | Бизнес правила | `validateWeight()`, `validateStatusTransition()` |
| **Entity** | Entity класове | Database constraints | `@Column(unique=true)` |

**Конкретни примери:**

```java
// DTO валидация (ShipmentRequest.java)
@DecimalMin(value = "0.01", message = "Теглото трябва да е поне 0.01 кг")
private BigDecimal weight;

// Service валидация (ShipmentServiceImpl.java)
private void validateDeliveryDestination(ShipmentRequest request) {
    if (!hasAddress && !hasOffice) {
        throw new InvalidDataException("Трябва да посочите дестинация");
    }
}

// Entity валидация (User.java)
@Column(unique = true, nullable = false)
private String username;
```

---

## Въпрос 3: Къде са цените и как се калкулират?

**Отговор:**

**Къде се съхраняват цените:**
- **Таблица:** `pricing_config`
- **Entity:** `PricingConfig.java` (`src/main/java/com/logistics/model/entity/`)
- **Полета:** `basePrice`, `pricePerKg`, `addressDeliveryFee`

**Къде се калкулират:**
- **Файл:** `PricingServiceImpl.java`
- **Метод:** `calculatePrice(BigDecimal weight, boolean isOfficeDelivery)`

**Формула:**
```
Цена = Базова цена + (Тегло × Цена на кг) + Такса за адрес

Такса за адрес:
  - До офис: 0 лв
  - До адрес: addressDeliveryFee (напр. 10 лв)
```

**Пример:**
```
Конфигурация: basePrice=5, pricePerKg=2, addressFee=10

5 кг до офис:  5 + (5×2) + 0  = 15 лв
5 кг до адрес: 5 + (5×2) + 10 = 25 лв
```

---

## Въпрос 4: Как се определя кой има достъп до какво?

**Отговор:**

Чрез **Role-Based Access Control (RBAC):**

**Стъпка 1:** При регистрация се задава роля (EMPLOYEE или CUSTOMER)

**Стъпка 2:** При всяка заявка се проверява ролята:
```java
@PreAuthorize("hasRole('EMPLOYEE')")  // Само служители
@PreAuthorize("hasRole('CUSTOMER')")  // Само клиенти
@PreAuthorize("hasRole('EMPLOYEE') or hasRole('CUSTOMER')")  // И двете
```

**Стъпка 3:** За по-фина проверка (напр. клиент да вижда само своите пратки):
```java
// В ShipmentController.java
if (isCustomer(authentication)) {
    Long customerId = getCustomerIdFromAuth(authentication);
    if (!shipment.getSenderId().equals(customerId) &&
        !shipment.getRecipientId().equals(customerId)) {
        throw new UnauthorizedException("Нямате достъп");
    }
}
```

---

## Въпрос 5: Защо използвам интерфейс + имплементация за Service?

**Отговор:**

Това е **Dependency Inversion Principle (DIP)** от SOLID.

**Аналогия:** Представи си, че имаш контакт (интерфейс) в стената. Можеш да включиш различни уреди (имплементации) - лампа, телевизор, зарядно. Контактът не се интересува КОЙ уред е включен.

**Ползи:**
1. **Лесна смяна** - можем да сменим имплементацията без да променяме контролерите
2. **Тестване** - можем да използваме mock обекти
3. **Разделение** - контролерът знае КАКВО може да прави service, не КАК го прави

```java
// Controller зависи от ИНТЕРФЕЙСА, не от имплементацията
@RestController
public class ShipmentController {
    private final ShipmentService shipmentService;  // Интерфейс!

    // Spring автоматично инжектира ShipmentServiceImpl
}
```

---

## Въпрос 6: Какво е @Transactional?

**Отговор:**

`@Transactional` казва: "Всички операции в този метод са ЕДНА транзакция."

**Аналогия:** Като банков превод - или се изпълнява ЦЕЛИЯТ, или НИЩО. Не може да вземеш пари от едната сметка, но да не стигнат до другата.

```java
@Transactional
public ShipmentResponse registerShipment(...) {
    // 1. Създай пратка
    // 2. Запиши в БД
    // 3. Ако стъпка 2 се провали → стъпка 1 се отменя
}
```

**@Transactional(readOnly = true):**
Оптимизация за методи, които само ЧЕТАТ данни. Базата не заключва записите за писане.

---

## Въпрос 7: Защо само DELIVERED пратки се броят като приход?

**Отговор:**

Защото само доставените пратки са **реално платени**:

| Статус | Парите платени ли са? | Брои ли се като приход? |
|--------|:---------------------:|:-----------------------:|
| REGISTERED | Не | Не |
| IN_TRANSIT | Не | Не |
| DELIVERED | Да | **Да** |
| CANCELLED | Не | Не |

```java
// В ShipmentRepository.java
@Query("SELECT SUM(s.price) FROM Shipment s WHERE s.status = 'DELIVERED' ...")
BigDecimal calculateRevenueBetweenDates(...);
```

---

## Въпрос 8: Какво е LAZY loading?

**Отговор:**

**LAZY** = "Зареди данните ЧРЕЗ когато ги поискам"
**EAGER** = "Зареди данните ВЕДНАГА"

```java
@ManyToOne(fetch = FetchType.LAZY)
private Customer sender;
```

**Аналогия:** Като меню в ресторант:
- EAGER: Поръчваш пица и веднага ти носят целия списък със съставки, доставчици, ферми...
- LAZY: Поръчваш пица и получаваш пицата. Ако попиташ "откъде е сиренето?" - тогава ти казват.

**Защо LAZY е по-добре:**
- По-малко заявки към БД
- По-малко памет
- По-бързо зареждане

---

## Въпрос 9: Какво прави EntityMapper?

**Отговор:**

EntityMapper конвертира **Entity** към **DTO**.

**Защо е нужно:**
1. Entity има много повече данни (релации, lazy fields)
2. DTO съдържа само това, което искаме да покажем
3. Entity има анотации за БД, DTO има анотации за API

```java
// Entity (от БД)
Shipment shipment = shipmentRepository.findById(1L);
// shipment.getSender() → Customer обект
// shipment.getRecipient() → Customer обект
// ... много релации

// DTO (за API)
ShipmentResponse response = EntityMapper.toShipmentResponse(shipment);
// response.getSenderName() → "Иван Иванов" (просто текст)
// response.getRecipientName() → "Мария Петрова"
// ... само нужните полета
```

---

## Въпрос 10: Как се променя статусът на пратка?

**Отговор:**

Чрез `PATCH /api/shipments/{id}/status` endpoint.

**Стъпки:**
1. Controller получава заявката
2. Извиква `ShipmentService.updateShipmentStatus()`
3. Service валидира прехода (`validateStatusTransition()`)
4. Ако е валиден - обновява и записва

**Валидни преходи:**
```
REGISTERED → IN_TRANSIT ✓
REGISTERED → CANCELLED ✓
IN_TRANSIT → DELIVERED ✓
IN_TRANSIT → CANCELLED ✓
DELIVERED → (нищо) ✗ (финален)
CANCELLED → (нищо) ✗ (финален)
```

---

# 12. БЪРЗИ СПРАВКИ

## 12.1 Къде се намира какво?

| Търсиш... | Файл/Папка |
|-----------|------------|
| REST endpoints | `controller/` |
| Бизнес логика | `service/impl/` |
| SQL заявки | `repository/` |
| Валидация на вход | DTO Request класове |
| Бизнес валидации | Service методи (validateXxx) |
| Модел на данни | `model/entity/` |
| Калкулиране на цена | `PricingServiceImpl.calculatePrice()` |
| Контрол на достъп | `@PreAuthorize` в контролерите |
| Статуси на пратка | `ShipmentStatus.java` |
| Роли на потребители | `Role.java` |

## 12.2 Важни методи

| Метод | Къде | Какво прави |
|-------|------|-------------|
| `register()` | AuthServiceImpl | Регистрира потребител |
| `login()` | AuthServiceImpl | Вход и JWT токен |
| `registerShipment()` | ShipmentServiceImpl | Създава пратка |
| `calculatePrice()` | PricingServiceImpl | Калкулира цена |
| `validateDeliveryDestination()` | ShipmentServiceImpl | Проверява дестинация |
| `validateWeight()` | ShipmentServiceImpl | Проверява тегло |
| `validateStatusTransition()` | ShipmentServiceImpl | Проверява преход |
| `calculateRevenueBetweenDates()` | ShipmentRepository | SQL за приходи |
| `toShipmentResponse()` | EntityMapper | Entity → DTO |

## 12.3 Формула за цена

```
ЦЕНА = basePrice + (weight × pricePerKg) + deliveryFee

Където:
  deliveryFee = 0           (ако е до офис)
  deliveryFee = addressFee  (ако е до адрес)
```

## 12.4 Статуси на пратка

```
REGISTERED ──► IN_TRANSIT ──► DELIVERED (край, брои се като приход)
     │              │
     └──► CANCELLED ◄──┘ (край, НЕ се брои като приход)
```

---

*Документ създаден за подготовка за изпит*
*Последна актуализация: Януари 2025*
