# Система за управление на логистика - Ръководство за презентация

## Пълно ръководство за подготовка и скрипт за презентация

Този документ ви учи как да представите вашата Система за управление на логистика пред преподавателя. Използвайте го както като скрипт за презентация, така и като ръководство за защита на техническите ви решения.

---

## Съдържание

1. [Въведение в проекта (2-3 минути)](#секция-1-въведение-в-проекта)
2. [Преглед на архитектурата (5 минути)](#секция-2-преглед-на-архитектурата)
3. [Имплементация на SOLID принципите (10 минути)](#секция-3-имплементация-на-solid-принципите)
4. [Дизайн на базата данни (5 минути)](#секция-4-дизайн-на-базата-данни)
5. [Имплементация на сигурност (5 минути)](#секция-5-имплементация-на-сигурност)
6. [Система за ценообразуване (5 минути)](#секция-6-система-за-ценообразуване)
7. [Стратегия за валидация (4 минути)](#секция-7-стратегия-за-валидация)
8. [Логика за контрол на достъпа (3 минути)](#секция-8-логика-за-контрол-на-достъпа)
9. [Генериране на справки (3 минути)](#секция-9-генериране-на-справки)
10. [Подход към тестването (3 минути)](#секция-10-подход-към-тестването)
11. [Акценти върху качеството на кода (2 минути)](#секция-11-акценти-върху-качеството-на-кода)
12. [Скрипт за демонстрация на живо](#секция-12-скрипт-за-демонстрация-на-живо)
13. [Често задавани въпроси и отговори](#секция-13-често-задавани-въпроси-и-отговори)
14. [Силни страни на проекта за подчертаване](#секция-14-силни-страни-на-проекта-за-подчертаване)
15. [Обосновка на техническите решения](#секция-15-обосновка-на-техническите-решения)

---

## СЕКЦИЯ 1: Въведение в проекта

**Продължителност: 2-3 минути**

### Какво да кажете

> "Изградих REST API за система за управление на логистична компания, използвайки Spring Boot 3.2 и Java 17. Системата обработва пълния жизнен цикъл на управлението на пратки - от регистрация и изчисляване на цена до проследяване и потвърждаване на доставка.
>
> Ключовите бизнес функции включват:
> - Автентикация на потребители с контрол на достъпа, базиран на роли (Служители и Клиенти)
> - Управление на компании и офиси
> - Регистрация на пратки с автоматично изчисляване на цена
> - Проследяване на статус на пратка през жизнения ѝ цикъл
> - Изчерпателни справки, включително анализ на приходите
>
> Избрах този технологичен стек, защото Spring Boot е индустриален стандарт за enterprise Java приложения, а Java 17 е текущата версия с дългосрочна поддръжка (LTS) с модерни езикови функции."

### Структура на проекта за показване

```
src/main/java/com/logistics/
├── config/                    # Конфигурация за сигурност и OpenAPI
│   ├── SecurityConfig.java
│   └── OpenApiConfig.java
├── controller/                # 8 REST контролера
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── OfficeController.java
│   ├── PricingController.java
│   ├── ShipmentController.java
│   └── ReportController.java
├── dto/                       # Request/Response DTO
├── exception/                 # Глобална обработка на изключения
├── model/
│   ├── entity/               # 7 JPA entities
│   └── enums/                # 3 enum-а (Role, ShipmentStatus, EmployeeType)
├── repository/               # 7 Spring Data JPA хранилища
├── security/                 # JWT автентикация
├── service/
│   ├── interfaces           # 8 service интерфейса
│   └── impl/                # 8 service имплементации
└── util/
    └── EntityMapper.java    # Конвертиране от Entity към DTO
```

### Ключови зависимости (pom.xml)

**Покажете този откъс:**

```xml
<properties>
    <java.version>17</java.version>
    <jjwt.version>0.12.3</jjwt.version>
</properties>

<dependencies>
    <!-- Spring Boot Web - поддръжка на REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA - Database ORM с Hibernate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security - Автентикация и оторизация -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Spring Validation - DTO валидация (@Valid, @NotNull и т.н.) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JWT зависимости - Автентикация, базирана на токени -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt.version}</version>
    </dependency>

    <!-- MySQL Connector - Драйвер за база данни -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
</dependencies>
```

### Ключови точки за разговор

- Java 17 е текущата LTS (Long-Term Support) версия с подобрения в производителността
- Spring Boot 3.2 осигурява бърза разработка с функции, готови за продукция
- MySQL 8.0 за ACID-съвместимо съхранение на релационни данни
- JWT за stateless, мащабируема автентикация
- Spring Data JPA с Hibernate за ORM

---

## СЕКЦИЯ 2: Преглед на архитектурата

**Продължителност: 5 минути**

### Какво да обясните

> "Системата следва класическа трислойна архитектура, която налага разделяне на отговорностите. Всеки слой има специфична отговорност и слоевете комуникират само чрез добре дефинирани интерфейси."

### Диаграма на архитектурата

```
┌─────────────────────────────────────────────────────────────┐
│                    ПРЕЗЕНТАЦИОНЕН СЛОЙ                       │
│              (Контролери - REST крайни точки)                │
│   Обработва: HTTP заявки, валидация, форматиране на отговори │
│   Пример: ShipmentController, AuthController                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    СЛОЙ НА БИЗНЕС ЛОГИКА                     │
│                (Услуги - Основна логика)                     │
│   Обработва: Бизнес правила, оркестрация, транзакции         │
│   Пример: ShipmentServiceImpl, PricingServiceImpl            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    СЛОЙ ЗА ДОСТЪП ДО ДАННИ                   │
│              (Хранилища - Достъп до база данни)              │
│   Обработва: CRUD операции, персонализирани заявки           │
│   Пример: ShipmentRepository, CustomerRepository             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    СЛОЙ НА БАЗА ДАННИ                        │
│                      (MySQL 8.0)                             │
│   Съхранява: Постоянни данни с ACID съвместимост             │
└─────────────────────────────────────────────────────────────┘
```

### Примерен код: ShipmentController (Презентационен слой)

**Файл: `controller/ShipmentController.java` (редове 38-52)**

```java
/**
 * Приложени SOLID принципи:
 * - Единствена отговорност (SRP): Обработва само HTTP крайни точки, свързани с пратки.
 *   Не съдържа бизнес логика - делегира към ShipmentService.
 * - Обръщане на зависимостите (DIP): Зависи от service интерфейси (ShipmentService,
 *   CustomerService), не от конкретни имплементации или хранилища.
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;    // ИНТЕРФЕЙС, не имплементация!
    private final CustomerService customerService;    // ИНТЕРФЕЙС, не имплементация!

    public ShipmentController(ShipmentService shipmentService, CustomerService customerService) {
        this.shipmentService = shipmentService;
        this.customerService = customerService;
    }
```

### Какво да кажете за този код

> "Забележете, че контролерът инжектира `ShipmentService` и `CustomerService` - това са **интерфейси**, не конкретни имплементации. Контролерът не знае и не се интересува от `ShipmentServiceImpl`. Това е **Принципът на обръщане на зависимостите** в действие.
>
> Контролерът обработва само HTTP проблеми - парсване на заявки, извикване на услугата и форматиране на отговори. Цялата бизнес логика е делегирана към service слоя. Това е **Принципът на единствената отговорност**."

### Примерен код: Делегиране на Service метод

**Файл: `controller/ShipmentController.java` (редове 58-70)**

```java
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")  // Анотация за сигурност - само служители могат да регистрират
public ResponseEntity<ShipmentResponse> registerShipment(
        @Valid @RequestBody ShipmentRequest request,  // @Valid задейства DTO валидация
        Authentication authentication) {

    String employeeUsername = authentication.getName();

    // Контролерът делегира ЦЯЛАТА бизнес логика към услугата
    ShipmentResponse response = shipmentService.registerShipment(request, employeeUsername);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Преподавателят може да попита

**В: "Защо използвате интерфейси вместо конкретни класове?"**

**О:** "Използването на интерфейси осигурява три ключови предимства:
1. **Тестваемост**: Мога лесно да мокна интерфейса в unit тестове без да имам нужда от реалната имплементация
2. **Гъвкавост**: Мога да заменям имплементации без да променям кода на контролера (напр. различни стратегии за ценообразуване)
3. **Слаба свързаност**: Контролерът не зависи от детайли на имплементацията, само от контракта, дефиниран от интерфейса"

**В: "Какво се случва, ако инжектирате хранилището директно в контролера?"**

**О:** "Това би нарушило слоестата архитектура и би създало няколко проблема:
1. Бизнес логиката би изтекла в контролера
2. Контролерът би имал твърде много отговорности (нарушение на SRP)
3. Управлението на транзакции става по-трудно, защото `@Transactional` принадлежи на услугите
4. Тестването става по-трудно, защото не можете лесно да мокнете операции с база данни"

---

## СЕКЦИЯ 3: Имплементация на SOLID принципите

**Продължителност: 10 минути** (Най-важната секция!)

### Преглед

> "Имплементирах всичките пет SOLID принципа в цялата кодова база. Позволете ми да демонстрирам всеки един с конкретни примери от кода."

---

### А. Принцип на единствената отговорност (SRP)

**Дефиниция:** Всеки клас трябва да има една и само една причина за промяна.

#### Примерен код: PricingServiceImpl

**Файл: `service/impl/PricingServiceImpl.java` (редове 15-31)**

```java
/**
 * Имплементация на PricingService, която зарежда конфигурация от БАЗАТА ДАННИ.
 *
 * Приложени SOLID принципи:
 * - Единствена отговорност (SRP): Този клас САМО обработва изчисления за ценообразуване.
 *   Не запазва данни и не валидира пратки - това са задачи на други услуги.
 * - Отворен/Затворен (OCP): Конфигурацията се съхранява в база данни, така че ценообразуването
 *   може да се промени без модификация на кода или повторно разгръщане.
 * - Обръщане на зависимостите (DIP): Зависи от интерфейса PricingConfigRepository.
 *
 * Формула за ценообразуване:
 * Общо = Базова цена + (Тегло × Цена на кг) + Такса за тип доставка
 */
@Service
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;

    public PricingServiceImpl(PricingConfigRepository pricingConfigRepository) {
        this.pricingConfigRepository = pricingConfigRepository;
    }
```

#### Какво да кажете

> "PricingService има **една отговорност**: да изчислява цени на пратки. НЕ:
> - Създава пратки - това е работата на ShipmentService
> - Записва в база данни - това е работата на хранилището
> - Обработва HTTP заявки - това е работата на контролера
>
> Ако правилата за ценообразуване се променят, модифицирам само този един клас. Ако логиката за създаване на пратки се промени, PricingService остава непроменен. Тази изолация прави кода по-лесен за тестване и поддръжка."

#### Повече SRP примери

| Клас | Единствена отговорност |
|------|----------------------|
| `ShipmentController` | Обработка на HTTP крайни точки за пратки |
| `ShipmentServiceImpl` | Бизнес логика за пратки |
| `ShipmentRepository` | Операции с база данни за пратки |
| `GlobalExceptionHandler` | Конвертиране на изключения в отговори |
| `JwtTokenProvider` | Генериране и валидиране на JWT токени |
| `EntityMapper` | Конвертиране от Entity към DTO |

---

### Б. Принцип отворен/затворен (OCP)

**Дефиниция:** Софтуерните обекти трябва да са отворени за разширение, но затворени за модификация.

#### Примерен код: Ценообразуване, управлявано от база данни

**Файл: `service/impl/PricingServiceImpl.java` (редове 64-88)**

```java
@Override
@Transactional(readOnly = true)
public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
    // Конфигурацията идва от БАЗАТА ДАННИ, не от хардкоднати стойности
    PricingConfig config = getActiveConfig();

    // Стъпка 1: Започваме с базовата цена
    BigDecimal total = config.getBasePrice();

    // Стъпка 2: Добавяме разход, базиран на теглото (тегло × цена на кг)
    BigDecimal weightCost = weight.multiply(config.getPricePerKg());
    total = total.add(weightCost);

    // Стъпка 3: Добавяме такса за тип доставка (0 за офис, addressDeliveryFee за адрес)
    if (!isOfficeDelivery) {
        total = total.add(config.getAddressDeliveryFee());
    }

    // Закръгляме до 2 десетични знака за валута
    total = total.setScale(2, RoundingMode.HALF_UP);

    return total;
}
```

#### Какво да кажете

> "Системата за ценообразуване е **отворена за разширение** - мога да променям цените по всяко време чрез актуализиране на базата данни, без да пипам кода. Тя е **затворена за модификация** - логиката за изчисление не трябва да се променя, когато стойностите на цените се променят.
>
> Ако бях хардкоднал цени като `basePrice = 5.00`, щях да трябва да модифицирам кода и да правя повторно разгръщане всеки път, когато цените се променят. С конфигурация от база данни просто актуализирам ред и новите цени влизат в сила незабавно."

#### Разширяемост на Enum

**Файл: `model/enums/ShipmentStatus.java`**

```java
public enum ShipmentStatus {
    REGISTERED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
    // Могат да се добавят нови статуси (напр. PROCESSING, RETURNED) без промяна на съществуващия код
}
```

> "Ако имаме нужда от нови статуси на пратки като PROCESSING или RETURNED, просто ги добавяме към enum-а. Съществуващият код за обработка на статуси продължава да работи непроменен."

---

### В. Принцип на заместване на Лисков (LSP)

**Дефиниция:** Обектите от суперклас трябва да могат да бъдат заменени с обекти от неговите подкласове, без да се засяга коректността.

#### Дизайнерско решение: Композиция вместо наследяване

> "Съзнателно избягвах проблематични йерархии на наследяване. Вместо Employee и Customer да наследяват User, използвах **композиция**."

**Файл: `model/entity/Employee.java` (редове 20-25)**

```java
@Entity
public class Employee {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // КОМПОЗИЦИЯ - Employee ИМА User, не наследява User

    private String employeeType;
    private BigDecimal salary;
    // ...
}
```

**Файл: `model/entity/Customer.java` (редове 20-25)**

```java
@Entity
public class Customer {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // КОМПОЗИЦИЯ - Customer ИМА User, не наследява User

    private String phone;
    private String address;
    // ...
}
```

#### Какво да кажете

> "Избягвах класическия капан на наследяването, където Employee и Customer биха наследили User. Това би причинило LSP нарушения, защото:
> 1. Не всички User операции имат смисъл за двата подтипа
> 2. Employee и Customer имат много различни допълнителни полета
> 3. Един User може да бъде едновременно Employee И Customer
>
> Вместо това използвах композиция: Employee и Customer и двете **референцират** User entity. Това е по-гъвкаво и избягва LSP нарушения."

#### LSP при имплементация на интерфейс

```java
// Всяка имплементация на ShipmentService може да бъде заменена
public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse getShipmentById(Long id);
    List<ShipmentResponse> getAllShipments();
    // ...
}

// Тази имплементация може да бъде заменена с друга без да се счупят контролерите
public class ShipmentServiceImpl implements ShipmentService {
    // ...
}
```

---

### Г. Принцип на разделяне на интерфейса (ISP)

**Дефиниция:** Никой клиент не трябва да бъде принуждаван да зависи от методи, които не използва.

#### Примерен код: Фокусирани Service интерфейси

**Файл: `service/PricingService.java`**

```java
public interface PricingService {
    BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery);
    BigDecimal getBasePrice();
    BigDecimal getPricePerKg();
    BigDecimal getAddressDeliveryFee();
    // Само 4 метода - всички свързани с ценообразуване. Без излишества!
}
```

**Файл: `service/ShipmentService.java`**

```java
public interface ShipmentService {
    ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
    ShipmentResponse getShipmentById(Long id);
    List<ShipmentResponse> getAllShipments();
    List<ShipmentResponse> getShipmentsByCustomerId(Long customerId);
    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatusUpdateRequest request);
    // Само методи, свързани с пратки
}
```

#### Какво да кажете

> "Всеки service интерфейс съдържа само методи, свързани със своя домейн. PricingService има методи за ценообразуване. ShipmentService има методи за пратки. Няма 'GodService' с 50 несвързани метода.
>
> Това означава, че когато контролер има нужда от ценообразуване, той зависи само от PricingService - не е принуден да знае за пратки, клиенти или нещо друго. Това е Разделяне на интерфейса в действие."

#### Контраст с анти-шаблон

```java
// ЛОШО - Дебел интерфейс (ISP нарушение)
public interface LogisticsService {
    void calculatePrice();
    void registerShipment();
    void createCustomer();
    void createEmployee();
    void generateReport();
    void sendEmail();
    // 50+ още несвързани метода...
}

// ДОБРЕ - Разделени интерфейси (това, което имплементирах)
public interface PricingService { /* методи за ценообразуване */ }
public interface ShipmentService { /* методи за пратки */ }
public interface CustomerService { /* методи за клиенти */ }
public interface ReportService { /* методи за справки */ }
```

---

### Д. Принцип на обръщане на зависимостите (DIP)

**Дефиниция:** Модулите от високо ниво не трябва да зависят от модули от ниско ниво. И двете трябва да зависят от абстракции.

#### Примерен код: ShipmentServiceImpl

**Файл: `service/impl/ShipmentServiceImpl.java` (редове 55-79)**

```java
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;  // Интерфейс
    private final CustomerRepository customerRepository;   // Интерфейс
    private final EmployeeRepository employeeRepository;   // Интерфейс
    private final OfficeRepository officeRepository;       // Интерфейс

    /**
     * PricingService инжектиран чрез конструктор.
     * Това е Обръщане на зависимостите (DIP) в действие:
     * - Зависим от ИНТЕРФЕЙСА PricingService
     * - Не знаем и не се интересуваме от имплементацията
     * - Това позволява лесна смяна на стратегии за ценообразуване
     */
    private final PricingService pricingService;  // Интерфейс, НЕ PricingServiceImpl!

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               CustomerRepository customerRepository,
                               EmployeeRepository employeeRepository,
                               OfficeRepository officeRepository,
                               PricingService pricingService) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.officeRepository = officeRepository;
        this.pricingService = pricingService;  // Spring инжектира реалната имплементация
    }
```

#### Какво да кажете

> "ShipmentServiceImpl зависи от **PricingService** интерфейса, не от PricingServiceImpl конкретния клас. Spring решава коя имплементация да инжектира по време на изпълнение.
>
> Това означава:
> 1. Мога да сменя имплементации за ценообразуване без да променям ShipmentServiceImpl
> 2. В тестове мога да мокна PricingService без да имам нужда от реална база данни
> 3. Бизнес логиката от високо ниво (регистрация на пратки) не зависи от детайли от ниско ниво (как се изчислява ценообразуването)"

#### Диаграма на потока на зависимости

```
┌─────────────────────────────────────────────────────────────┐
│                    МОДУЛ ОТ ВИСОКО НИВО                      │
│                  ShipmentController                          │
│           (зависи от интерфейса ShipmentService)             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      АБСТРАКЦИЯ                              │
│                  ShipmentService (интерфейс)                 │
└─────────────────────────────────────────────────────────────┘
                              ↑
┌─────────────────────────────────────────────────────────────┐
│                    МОДУЛ ОТ НИСКО НИВО                        │
│                  ShipmentServiceImpl                         │
│           (имплементира интерфейса ShipmentService)          │
└─────────────────────────────────────────────────────────────┘
```

#### Преподавателят може да попита

**В: "Какво ако инжектирате конкретния клас директно?"**

**О:** "Ако инжектирах `ShipmentServiceImpl` директно вместо интерфейса `ShipmentService`:
1. **Тестването става по-трудно** - Не мога лесно да мокна конкретния клас
2. **Свързаността се увеличава** - Контролерът сега зависи от детайли на имплементацията
3. **Гъвкавостта намалява** - Не мога да сменям имплементации без промени в кода
4. **Нарушава DIP** - Модул от високо ниво зависи от модул от ниско ниво"

---

## СЕКЦИЯ 4: Дизайн на базата данни

**Продължителност: 5 минути**

### Диаграма на връзки между обектите

```
┌──────────────┐         ┌─────────────────┐
│    users     │         │   customers     │
│   (авт.)     │1───────1│  (тел., адрес)  │
└──────────────┘         └─────────────────┘
       │1                        │1
       │                         │
       │1        ┌──────────────┐│
       └────────→│  employees   ││
                 │ (запл.,тип)  ││
                 └──────────────┘│
                       │M        │M
                       │         │
                ┌──────▼─────┐   │
                │  companies │   │
                │ (рег_ном)  │   │
                └──────┬─────┘   │
                       │1        │
                       │M        │
                ┌──────▼──────┐  │         ┌─────────────────┐
                │   offices   │  └────────→│    shipments    │
                │  (адрес)    │←───────────│ (тегло, цена)   │
                └─────────────┘            └─────────────────┘
                                                   │
                                           ┌───────▼───────┐
                                           │ pricing_config│
                                           │(база, на_кг)  │
                                           └───────────────┘
```

### Акценти върху схемата

**Файл: `resources/schema.sql` (редове 110-136)**

```sql
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    registered_by_id BIGINT NOT NULL,
    origin_office_id BIGINT,
    delivery_address VARCHAR(255),
    delivery_office_id BIGINT,
    weight DECIMAL(10,2) NOT NULL,      -- BigDecimal в Java
    price DECIMAL(10,2) NOT NULL,       -- BigDecimal в Java
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

### Какво да кажете

> "Всички парични стойности използват `DECIMAL(10,2)`, никога `FLOAT` или `DOUBLE`. Това предотвратява грешки в точността при финансови изчисления. В Java те се съпоставят на `BigDecimal`.
>
> Конфигурацията за ценообразуване се съхранява в базата данни, така че може да се променя без повторно разгръщане на приложението. Само един ред конфигурация за ценообразуване трябва да е активен едновременно.
>
> Външните ключове налагат референтна цялост - не можете да създадете пратка за несъществуващ клиент."

### Предложение за демонстрация на живо

1. Отворете MySQL Workbench или командния ред
2. Покажете таблицата `pricing_config` с текущите стойности
3. Актуализирайте стойност за ценообразуване
4. Създайте нова пратка
5. Покажете, че новата цена е приложена

---

## СЕКЦИЯ 5: Имплементация на сигурност

**Продължителност: 5 минути**

### Диаграма на потока на автентикация

```
┌─────────┐                                    ┌──────────────┐
│  Клиент │                                    │   Сървър     │
└────┬────┘                                    └──────┬───────┘
     │                                                │
     │  1. POST /api/auth/login                       │
     │     {username, password}                       │
     │───────────────────────────────────────────────>│
     │                                                │
     │                        2. Валидиране на данни  │
     │                        3. Генериране на JWT    │
     │                                                │
     │  4. Отговор: {token: "eyJhbG...", role: ...}   │
     │<───────────────────────────────────────────────│
     │                                                │
     │  5. GET /api/shipments                         │
     │     Authorization: Bearer eyJhbG...            │
     │───────────────────────────────────────────────>│
     │                                                │
     │                        6. Валидиране на JWT    │
     │                        7. Извличане на user/role│
     │                        8. Проверка @PreAuthorize│
     │                                                │
     │  9. Отговор: [данни за пратки]                 │
     │<───────────────────────────────────────────────│
```

### Код на SecurityConfig

**Файл: `config/SecurityConfig.java` (редове 67-113)**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // Деактивиране на CSRF - използваме stateless JWT автентикация
            .csrf(csrf -> csrf.disable())

            // Задаване на управление на сесии на stateless
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Конфигуриране на правила за оторизация
            .authorizeHttpRequests(auth -> auth
                    // Статични файлове - публичен достъп
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/css/**", "/js/**").permitAll()

                    // Auth крайни точки - публичен достъп
                    .requestMatchers("/api/auth/**").permitAll()

                    // Swagger документация
                    .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()

                    // Всички други крайни точки изискват автентикация
                    .anyRequest().authenticated()
            )

            // Добавяне на JWT филтър преди стандартния филтър за автентикация
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### JWT Token Provider

**Файл: `security/JwtTokenProvider.java` (редове 62-77)**

```java
public String generateToken(String username, Role role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);  // 24 часа

    String token = Jwts.builder()
            .subject(username)           // За кого е токенът
            .claim("role", role.name())  // Ролята на потребителя, вградена в токена
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)         // HMAC-SHA256 подпис
            .compact();

    return token;
}
```

### Контрол на достъпа, базиран на роли

**Файл: `controller/ShipmentController.java`**

```java
// Само служители могат да регистрират пратки
@PostMapping
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<ShipmentResponse> registerShipment(...) { }

// Само служители могат да актуализират статуса на пратка
@PatchMapping("/{id}/status")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<ShipmentResponse> updateShipmentStatus(...) { }
```

### Какво да кажете

> "Използвам JWT за stateless автентикация. Това означава, че сървърът не съхранява данни за сесии - цялата информация, необходима за валидиране на заявка, е в самия токен.
>
> Потокът е:
> 1. Потребителят влиза с потребителско име/парола
> 2. Сървърът валидира данните и генерира JWT, съдържащ потребителското име и роля
> 3. Клиентът съхранява токена и го изпраща в Authorization хедъра за последващи заявки
> 4. Сървърът валидира подписа и изтичането на токена при всяка заявка
> 5. Анотацията `@PreAuthorize` на Spring Security проверява дали потребителят има необходимата роля"

---

## СЕКЦИЯ 6: Система за ценообразуване

**Продължителност: 5 минути**

### Формула за ценообразуване

```
Обща цена = Базова цена + (Тегло × Цена на кг) + Такса за доставка

Където:
├── Базова цена:       Фиксиран разход за пратка (по подразбиране: $5.00)
├── Цена на кг:        Разход на килограм (по подразбиране: $2.00/кг)
└── Такса за доставка:
    ├── Доставка до офис:  $0.00 (безплатно)
    └── Доставка до адрес: $10.00 (допълнителна такса)
```

### Примерни изчисления

```
Пример 1: 5кг пакет до офис
  Общо = $5.00 + (5 × $2.00) + $0.00 = $15.00

Пример 2: 5кг пакет до адрес
  Общо = $5.00 + (5 × $2.00) + $10.00 = $25.00

Пример 3: 2.75кг пакет до офис
  Общо = $5.00 + (2.75 × $2.00) + $0.00 = $10.50
```

### Имплементация на кода

**Файл: `service/impl/PricingServiceImpl.java` (редове 64-88)**

```java
@Override
@Transactional(readOnly = true)
public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
    PricingConfig config = getActiveConfig();  // От базата данни

    // Стъпка 1: Започваме с базовата цена
    BigDecimal total = config.getBasePrice();

    // Стъпка 2: Добавяме разход, базиран на теглото (тегло × цена на кг)
    BigDecimal weightCost = weight.multiply(config.getPricePerKg());
    total = total.add(weightCost);

    // Стъпка 3: Добавяме такса за тип доставка (0 за офис, addressDeliveryFee за адрес)
    if (!isOfficeDelivery) {
        total = total.add(config.getAddressDeliveryFee());
    }

    // Закръгляме до 2 десетични знака за валута
    total = total.setScale(2, RoundingMode.HALF_UP);

    return total;
}
```

### Какво да кажете

> "Ценообразуването се изчислява **само от страна на сървъра**. Фронтендът никога не изчислява цени, защото:
> 1. Цените биха могли да бъдат манипулирани от потребители
> 2. Сървърът е източникът на истината
>
> Използвам `BigDecimal` за всички парични изчисления, защото `double` може да има грешки в точността. Например, `0.1 + 0.2` при числа с плаваща запетая е равно на `0.30000000000000004`, не на `0.3`.
>
> Конфигурацията идва от базата данни, така че цените могат да се променят без модификации на кода. Това следва Принципа отворен/затворен."

### Демонстрация на живо

1. Покажете текущата `pricing_config` в базата данни
2. Регистрирайте пратка, отбележете цената
3. Актуализирайте ценообразуването в базата данни (напр. увеличете базовата цена)
4. Регистрирайте друга пратка, покажете, че новата цена е приложена

---

## СЕКЦИЯ 7: Стратегия за валидация

**Продължителност: 4 минути**

### Многослойна валидация

```
┌─────────────────────────────────────────────────────────────┐
│  Слой 1: ФРОНТЕНД (Незабавна обратна връзка към потребителя)│
│  validation.js - validateShipmentForm()                     │
│  "Теглото трябва да е по-голямо от 0"                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Слой 2: DTO (анотации @Valid)                              │
│  ShipmentRequest.java - @NotNull, @DecimalMin, @DecimalMax  │
│  Автоматичен HTTP 400 ако валидацията не мине               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Слой 3: SERVICE (Бизнес правила)                           │
│  ShipmentServiceImpl - validateDeliveryDestination()        │
│  "Не може да се зададе едновременно адрес и офис"           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Слой 4: EXCEPTION HANDLER (Последователни отговори)        │
│  GlobalExceptionHandler - форматира всички грешки           │
│  Връща структуриран JSON отговор за грешка                  │
└─────────────────────────────────────────────────────────────┘
```

### Слой 1: Фронтенд валидация

**Файл: `resources/static/js/validation.js` (редове 202-242)**

```javascript
export function validateShipmentForm(data) {
    const errors = [];

    // Валидация на подател
    if (!data.senderId) {
        errors.push('Моля, изберете подател.');
    }

    // Валидация на тегло - трябва да е между 0.01 и 10000 кг
    if (!data.weight || data.weight <= 0) {
        errors.push('Теглото трябва да е по-голямо от 0.');
    } else if (data.weight < 0.01) {
        errors.push('Теглото трябва да е поне 0.01 кг.');
    } else if (data.weight > 10000) {
        errors.push('Теглото не може да надвишава 10000 кг.');
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}
```

### Слой 2: DTO валидация

**Файл: `dto/shipment/ShipmentRequest.java` (редове 24-59)**

```java
public class ShipmentRequest {

    @NotNull(message = "ID на подател е задължително")
    private Long senderId;

    @NotNull(message = "ID на получател е задължително")
    private Long recipientId;

    @Size(max = 255, message = "Адресът за доставка не трябва да надвишава 255 символа")
    private String deliveryAddress;

    @NotNull(message = "Теглото е задължително")
    @DecimalMin(value = "0.01", message = "Теглото трябва да е поне 0.01 кг")
    @DecimalMax(value = "10000.00", message = "Теглото не може да надвишава 10000 кг")
    private BigDecimal weight;
}
```

### Слой 3: Service валидация

**Файл: `service/impl/ShipmentServiceImpl.java` (редове 282-300)**

```java
private void validateDeliveryDestination(ShipmentRequest request) {
    boolean hasAddress = request.isAddressDelivery();
    boolean hasOffice = request.isOfficeDelivery();

    if (!hasAddress && !hasOffice) {
        throw new InvalidDataException(
            "Трябва да се предостави или deliveryAddress, или deliveryOfficeId");
    }

    if (hasAddress && hasOffice) {
        throw new InvalidDataException(
            "Не може да се зададе едновременно deliveryAddress и deliveryOfficeId");
    }
}
```

### Слой 4: Глобален Exception Handler

**Файл: `exception/GlobalExceptionHandler.java` (редове 148-169)**

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    // Събиране на всички грешки от валидация на полета
    Map<String, String> validationErrors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
        validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Валидацията се провали",
            "Едно или повече полета имат грешки при валидация",
            request.getRequestURI()
    );
    errorResponse.setValidationErrors(validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
}
```

### Какво да кажете

> "Валидирам на **множество слоеве** за защита в дълбочина:
>
> 1. **Фронтенд валидация** дава незабавна обратна връзка към потребителя без заявка до сървъра
> 2. **DTO валидация** улавя заявки, които заобикалят фронтенда (напр. API инструменти като Postman)
> 3. **Service валидация** налага бизнес правила като 'или адрес ИЛИ офис, не и двете'
> 4. **Exception handler** осигурява, че всички грешки връщат последователен JSON формат
>
> Дори ако злонамерен потребител заобиколи фронтенд валидацията, бекендът все още валидира всичко. Това е най-добра практика за сигурност."

---

## СЕКЦИЯ 8: Логика за контрол на достъпа

**Продължителност: 3 минути**

### Филтриране на данни, базирано на роли

**Файл: `controller/ShipmentController.java` (редове 102-119)**

```java
@GetMapping
@Operation(summary = "Получаване на всички пратки",
           description = "Служителите виждат всички. Клиентите виждат само собствените си.")
public ResponseEntity<List<ShipmentResponse>> getAllShipments(
        Authentication authentication) {

    List<ShipmentResponse> shipments;

    if (isCustomer(authentication)) {
        // Клиент: само техните пратки (като подател ИЛИ получател)
        Long customerId = getCustomerIdFromAuth(authentication);
        shipments = shipmentService.getShipmentsByCustomerId(customerId);
    } else {
        // Служител: всички пратки
        shipments = shipmentService.getAllShipments();
    }

    return ResponseEntity.ok(shipments);
}
```

### Филтриране в Service слоя

**Файл: `service/impl/ShipmentServiceImpl.java` (редове 166-180)**

```java
@Override
@Transactional(readOnly = true)
public List<ShipmentResponse> getShipmentsByCustomerId(Long customerId) {
    // Валидиране, че клиентът съществува
    if (!customerRepository.existsById(customerId)) {
        throw new ResourceNotFoundException("Customer", "id", customerId);
    }

    // Връщане на пратки, където клиентът е подател ИЛИ получател
    return shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId)
            .stream()
            .map(EntityMapper::toShipmentResponse)
            .collect(Collectors.toList());
}
```

### Отказ на достъп за неоторизиран достъп

**Файл: `controller/ShipmentController.java` (редове 77-95)**

```java
@GetMapping("/{id}")
public ResponseEntity<ShipmentResponse> getShipmentById(
        @PathVariable Long id,
        Authentication authentication) {

    ShipmentResponse shipment = shipmentService.getShipmentById(id);

    // Ако е клиент, проверяваме дали има достъп до тази пратка
    if (isCustomer(authentication)) {
        Long customerId = getCustomerIdFromAuth(authentication);
        if (!shipment.getSenderId().equals(customerId) &&
                !shipment.getRecipientId().equals(customerId)) {
            throw new UnauthorizedException(
                "Можете да преглеждате само пратки, където сте подател или получател");
        }
    }

    return ResponseEntity.ok(shipment);
}
```

### Какво да кажете

> "Контролът на достъпа се случва на **множество нива**:
>
> 1. `@PreAuthorize("hasRole('EMPLOYEE')")` напълно блокира клиентите от крайни точки само за служители
> 2. За споделени крайни точки като преглед на пратки, **контролерът проверява ролята** и **услугите филтрират данните**
> 3. Клиент може да види само пратки, където е подател ИЛИ получател
> 4. Служителите заобикалят тези филтри и виждат всичко
>
> Това осигурява изолация на данните - клиентите никога случайно не виждат пратки на други клиенти."

### Демонстрация на живо

1. Влезте като клиент
2. Прегледайте пратките - виждате само собствените си пратки
3. Опитайте да достъпите друга пратка по ID - получавате 403 Forbidden
4. Влезте като служител - виждате всички пратки

---

## СЕКЦИЯ 9: Генериране на справки

**Продължителност: 3 минути**

### Налични справки (общо 8)

| Справка | Крайна точка | Достъп |
|---------|--------------|--------|
| Всички служители | GET /api/reports/employees | Само служител |
| Всички клиенти | GET /api/reports/customers | Само служител |
| Всички пратки | GET /api/reports/shipments | Служител: всички, Клиент: собствени |
| По служител | GET /api/reports/shipments/employee/{id} | Само служител |
| Чакащи пратки | GET /api/reports/shipments/pending | Само служител |
| Изпратени от клиент | GET /api/reports/shipments/customer/{id}/sent | Собствени данни |
| Получени от клиент | GET /api/reports/shipments/customer/{id}/received | Собствени данни |
| Приходи | GET /api/reports/revenue?startDate=...&endDate=... | Само служител |

### Имплементация на справка за приходи

**Файл: `service/impl/ReportServiceImpl.java` (редове 137-171)**

```java
/**
 * ВАЖНО: Отчита само ДОСТАВЕНИ пратки като приход.
 * Отменени или чакащи пратки НЕ се отчитат като приход, защото:
 * - Отменени: Плащането не е завършено
 * - Чакащи: Плащането не е потвърдено/завършено
 *
 * Приход = СУМА(цена) за всички ДОСТАВЕНИ пратки в периода.
 */
@Override
public RevenueResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
    logger.info("Генериране на справка за приходи от {} до {}", startDate, endDate);

    // Конвертиране на LocalDate към LocalDateTime за заявката
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    // Изчисляване на общ приход (сума от цени само за ДОСТАВЕНИ пратки)
    BigDecimal totalRevenue = shipmentRepository
        .calculateRevenueBetweenDates(startDateTime, endDateTime);

    // Ако няма доставени пратки, приходът е 0
    if (totalRevenue == null) {
        totalRevenue = BigDecimal.ZERO;
    }

    // Броене на доставени пратки в периода
    long deliveredCount = shipmentRepository
        .findDeliveredShipmentsBetweenDates(startDateTime, endDateTime).size();

    return new RevenueResponse(startDate, endDate, totalRevenue, deliveredCount);
}
```

### Какво да кажете

> "Справката за приходи отчита само **ДОСТАВЕНИ** пратки. Отменени или чакащи пратки не се отчитат, защото плащането не е завършено.
>
> Справката филтрира по **дата на доставка**, не по дата на регистрация. Това дава точна картина кога приходът действително е спечелен.
>
> Използвам `BigDecimal.add()` за сумиране на цени, за да поддържам точност - никога `double` за финансови изчисления."

---

## СЕКЦИЯ 10: Подход към тестването

**Продължителност: 3 минути**

### Структура на тестовете

```
src/test/java/com/logistics/
├── controller/     # Интеграционни тестове (8 тестови класа)
├── service/        # Unit тестове (6 тестови класа)
├── repository/     # Repository тестове
└── security/       # Security тестове
```

### Пример за unit тест

**Файл: `service/PricingServiceTest.java` (редове 47-65)**

```java
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingConfigRepository pricingConfigRepository;

    @InjectMocks
    private PricingServiceImpl pricingService;

    @Test
    @DisplayName("Трябва да изчисли правилна цена за доставка до офис")
    void calculatePrice_OfficeDelivery_ReturnsCorrectPrice() {
        // Подготовка
        when(pricingConfigRepository.findByActiveTrue())
            .thenReturn(Optional.of(defaultConfig));
        BigDecimal weight = new BigDecimal("5.00");

        // Действие
        // Формула: 5.00 + (5.00 × 2.00) + 0.00 = 15.00
        BigDecimal price = pricingService.calculatePrice(weight, true);

        // Проверка
        assertEquals(new BigDecimal("15.00"), price);
        verify(pricingConfigRepository).findByActiveTrue();
    }
}
```

### Какво да кажете

> "Използвам **Mockito** за мокване на зависимости, което позволява тестване на бизнес логика в изолация. Анотацията `@Mock` създава мок на хранилището, а `@InjectMocks` го инжектира в услугата.
>
> Тестът следва шаблона **Arrange-Act-Assert**:
> 1. **Arrange (Подготовка)**: Настройване на тестови данни и поведение на мокове
> 2. **Act (Действие)**: Извикване на метода, който се тества
> 3. **Assert (Проверка)**: Проверка на резултата и взаимодействията с мокове
>
> Тестовете покриват гранични случаи като нулево тегло, отрицателни стойности и липсваща конфигурация."

### Ключови тестови случаи

- Изчисляване на ценообразуване за доставка до офис vs. до адрес
- Обработка на грешки, когато няма конфигурация за ценообразуване
- Валидация на бизнес правила
- Потоци на автентикация и оторизация

---

## СЕКЦИЯ 11: Акценти върху качеството на кода

**Продължителност: 2 минути**

### Практики за чист код

**Описателни имена на методи:**
```java
// ДОБРЕ - Самодокументиращи се
private void validateDeliveryDestination(ShipmentRequest request)
private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next)
public boolean isOfficeDelivery()

// ЛОШО - Неясна цел
private void check(Request r)
private void validate(Status s)
public boolean flag1()
```

### Изчерпателен JavaDoc

```java
/**
 * Регистрира нова пратка в системата.
 *
 * @param request           детайли за пратката, включително подател, получател, тегло
 * @param employeeUsername  потребителско име на служителя, регистриращ пратката
 * @return регистрирана пратка с изчислена цена
 * @throws ResourceNotFoundException ако подател/получател/служител не е намерен
 * @throws InvalidDataException ако дестинацията за доставка е невалидна
 */
public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername);
```

### Никаква бизнес логика във фронтенда

```javascript
// Фронтендът просто показва отговора от бекенда
const price = response.price;  // Изчислена от бекенда

// НЕ: const price = calculatePrice(weight, deliveryType);  // НЕ ПРАВЕТЕ ТОВА!
```

---

## СЕКЦИЯ 12: Скрипт за демонстрация на живо

**Продължителност: 10 минути**

### Стъпка по стъпка демонстрация

#### 1. Регистрация и вход (2 мин)

```bash
# Регистрация на служител
POST /api/auth/register
{
  "username": "ivan_employee",
  "email": "ivan@company.com",
  "password": "password123",
  "role": "EMPLOYEE"
}

# Покажете JWT токена в отговора
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "ivan_employee",
  "role": "EMPLOYEE"
}

# Регистрация на клиент
POST /api/auth/register
{
  "username": "maria_customer",
  "email": "maria@email.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

#### 2. Операции на служител (3 мин)

```bash
# Създаване на компания (като служител)
POST /api/companies
Authorization: Bearer <employee_token>
{
  "name": "БързаПратка Логистика",
  "registrationNumber": "REG-12345",
  "address": "ул. Главна 123",
  "phone": "+359-555-0100"
}

# Създаване на офис
POST /api/offices
{
  "companyId": 1,
  "name": "Централен офис",
  "address": "бул. Център 456",
  "city": "София",
  "country": "България"
}

# Регистриране на пратка - покажете автоматично изчислената цена
POST /api/shipments
{
  "senderId": 1,
  "recipientId": 2,
  "originOfficeId": 1,
  "deliveryAddress": "бул. Клиент 789",
  "weight": 5.00
}

# Обърнете внимание: Цената е изчислена автоматично!
# 5.00 база + (5 × 2.00) + 10.00 такса за адрес = 25.00
```

#### 3. Изглед на клиент (2 мин)

```bash
# Вход като клиент
POST /api/auth/login
{ "username": "maria_customer", "password": "password123" }

# Преглед на пратки - вижда само собствените си
GET /api/shipments
Authorization: Bearer <customer_token>
# Връща само пратки, където maria е подател или получател

# Опит за достъп до друга пратка
GET /api/shipments/999
# Връща 403 Forbidden: "Можете да преглеждате само собствените си пратки"
```

#### 4. Справки (2 мин)

```bash
# Генериране на справка за приходи (като служител)
GET /api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer <employee_token>

# Отговор:
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "totalRevenue": 1250.00,
  "deliveredCount": 47
}
```

#### 5. Промяна на конфигурация (1 мин)

```sql
-- В MySQL, актуализирайте ценообразуването
UPDATE pricing_config SET base_price = 7.00 WHERE active = true;
```

```bash
# Регистрирайте нова пратка
POST /api/shipments
{ ... weight: 5.00 ... }

# Покажете новата цена: 7.00 + (5 × 2.00) + 10.00 = 27.00
# (преди беше 25.00)
```

---

## СЕКЦИЯ 13: Често задавани въпроси и отговори

### В1: "Защо използвате BigDecimal вместо double?"

**О:** "Double използва IEEE 754 представяне с плаваща запетая, което не може да представи точно всички десетични стойности. Например, `0.1 + 0.2` в double е равно на `0.30000000000000004`, не на `0.3`. При финансови изчисления тези малки грешки могат да се натрупат и да причинят значителни несъответствия. BigDecimal използва представяне с произволна точност, осигурявайки точни изчисления. Затова всички парични стойности в моята система - цени, заплати, такси - използват BigDecimal."

### В2: "Обяснете разликата между автентикация и оторизация във вашата система."

**О:** "Автентикацията проверява КОЙ е потребителят. В моята система това се случва, когато потребителят влезе:
- Потребителят предоставя потребителско име/парола
- AuthService валидира данните спрямо базата данни
- Ако са валидни, се генерира JWT токен, съдържащ потребителското име и роля

Оторизацията определя КАКВО може да прави потребителят. Това се случва при всяка заявка:
- JwtAuthenticationFilter извлича и валидира токена
- `@PreAuthorize("hasRole('EMPLOYEE')")` проверява дали потребителят има необходимата роля
- Service слоят допълнително филтрира данни въз основа на идентичността на потребителя"

### В3: "Какво се случва, ако двама служители се опитат да актуализират една и съща пратка едновременно?"

**О:** "Анотацията `@Transactional` на Spring осигурява, че операциите с базата данни са атомарни. Ако двама служители актуализират едновременно:
1. Всяка транзакция чете текущото състояние
2. Всяка прави своите промени
3. Първата, която commit-не, успява
4. Втората или презаписва (last-write-wins), или се проваля, ако има конфликт

За истинско оптимистично заключване бих могъл да добавя поле `@Version` към entity-то, което би хвърлило `OptimisticLockException` при конкурентни актуализации. Това не е имплементирано, тъй като не беше в изискванията, но съм наясно с това подобрение."

### В4: "Защо ценообразуването е в базата данни вместо в кода?"

**О:** "Съхранението на ценообразуването в базата данни предоставя няколко предимства:
1. **Без повторно разгръщане**: Цените могат да се променят без повторно изграждане и разгръщане на приложението
2. **Одитна следа**: Можем да проследяваме промените в ценообразуването във времето, като запазваме стари конфигурации
3. **Гъвкавост по време на изпълнение**: Бизнес потребителите биха могли да актуализират цени през админ интерфейс
4. **Принцип отворен/затворен**: Логиката за ценообразуване е затворена за модификация, но отворена за разширение чрез конфигурация"

### В5: "Покажете ми как предотвратявате SQL инжекция."

**О:** "Използвам Spring Data JPA, което генерира параметризирани заявки. Например:

```java
shipmentRepository.findBySenderIdOrRecipientId(customerId, customerId)
```

Това генерира:
```sql
SELECT * FROM shipments WHERE sender_id = ? OR recipient_id = ?
```

Заместителите `?` се попълват с правилно ескейпнати стойности на параметри, правейки SQL инжекцията невъзможна. Никога не конкатенирам потребителски вход в SQL низове."

### В6: "Какво бихте променили, ако това влезе в продукция?"

**О:** "Ще са необходими няколко подобрения:
1. **Кеширане**: Добавяне на Redis за често достъпвани данни като конфигурация за ценообразуване
2. **Ограничаване на заявките**: Предотвратяване на злоупотреба с API чрез throttling на заявки
3. **Логиране**: Структурирано логиране (JSON) с correlation ID-та за проследяване на заявки
4. **Мониторинг**: Metrics endpoint за Prometheus, табла в Grafana
5. **HTTPS**: Налагане на TLS за всички връзки
6. **Конфигурация на средата**: Изнасяне на чувствителна конфигурация (пароли за база данни, JWT secret) в променливи на средата или vault
7. **Миграции на база данни**: Използване на Flyway или Liquibase за контрол на версиите на схемата"

### В7: "Обяснете как работи валидацията на JWT токена."

**О:** "Потокът е:
1. Клиентът изпраща заявка с хедър `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter.doFilterInternal()` прихваща заявката
3. Токенът се извлича от хедъра
4. Извиква се `JwtTokenProvider.validateToken()`:
   - Парсва токена, използвайки секретния ключ
   - Проверява, че подписът не е манипулиран
   - Проверява датата на изтичане
5. Ако е валиден, потребителското име се извлича и `UserDetailsService` зарежда потребителя
6. Създава се `UsernamePasswordAuthenticationToken` и се задава в SecurityContext
7. Заявката продължава към контролера с налична автентикация"

### В8: "Защо разделяте DTO от entities?"

**О:** "DTO (Data Transfer Objects) служат за различни цели от entities:
1. **Декуплиране**: API контрактите не се променят, когато схемата на базата данни се промени
2. **Сигурност**: Entities могат да имат полета, които не искаме да излагаме (password hash)
3. **Валидация**: DTO имат анотации за валидация на данни от заявки
4. **Гъвкавост**: Response DTO могат да комбинират данни от множество entities
5. **Производителност**: Можем да включим само полетата, необходими за конкретен отговор"

### В9: "Как осигурявате консистентност на данните?"

**О:** "Множество механизми осигуряват консистентност:
1. **Ограничения на базата данни**: Външни ключове, NOT NULL, UNIQUE
2. **`@Transactional`**: Service методите се изпълняват в транзакции, които се отменят при неуспех
3. **Валидация**: Многослойна валидация предотвратява запазването на невалидни данни
4. **Бизнес правила**: Service слоят налага правила като 'или адрес ИЛИ офис'
5. **Entity callbacks**: `@PrePersist` и `@PreUpdate` задават timestamps автоматично"

### В10: "Какво ако клиент се опита да регистрира пратка?"

**О:** "Анотацията `@PreAuthorize("hasRole('EMPLOYEE')")` върху крайната точка `registerShipment` напълно блокира клиентите. Spring Security прихваща заявката преди да достигне контролера и връща:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Нямате право на достъп до този ресурс"
}
```

Клиентът никога не преминава отвъд филтъра за сигурност."

---

## СЕКЦИЯ 14: Силни страни на проекта за подчертаване

### Кажете това уверено

- "Пълна имплементация на всичките 8 изисквани справки с контрол на достъпа, базиран на роли"
- "Стриктни SOLID принципи с документация във всеки service клас"
- "Многослойна валидация: фронтенд, DTO анотации, service слой, exception handler"
- "Конфигурация на ценообразуване, управлявана от база данни, за гъвкавост по време на изпълнение"
- "Контрол на достъпа, базиран на роли, с правилна изолация на данни на клиенти"
- "Използване на BigDecimal навсякъде за финансова точност"
- "Чиста слоеста архитектура с правилно разделяне на отговорностите"
- "JWT stateless автентикация с 24-часово изтичане на токена"
- "RESTful API дизайн, следващ HTTP конвенции"
- "Изчерпателно тестово покритие на критичната бизнес логика"

---

## СЕКЦИЯ 15: Обосновка на техническите решения

### Бъдете готови да обясните ЗАЩО

| Решение | Обосновка |
|---------|-----------|
| **Java 17** | Текуща LTS версия, модерни функции (records, sealed classes налични), дългосрочна поддръжка |
| **Spring Boot 3.2** | Индустриален стандарт, автоматична конфигурация, вграден сървър, обширна екосистема |
| **JWT автентикация** | Stateless (без сесия от страна на сървъра), мащабируема, работи добре с REST API |
| **MySQL 8.0** | ACID съвместимост, релационни данни с външни ключове, зряла и надеждна |
| **Слоеста архитектура** | Разделяне на отговорностите, по-лесно тестване, поддръжка |
| **Services, базирани на интерфейси** | Обръщане на зависимостите, тестваемост с мокове, гъвкавост на имплементацията |
| **BigDecimal** | Финансова точност, без грешки с плаваща запетая |
| **Ценообразуване от база данни** | Конфигурируемост по време на изпълнение, без повторно разгръщане за промяна на цени |
| **Spring Data JPA** | Намалява шаблонен код, type-safe заявки, автоматично генериране на заявки |
| **BCrypt хеширане на пароли** | Индустриален стандарт, включва сол, настройваем work factor |

---

## Резюме на времето за презентация

| Секция | Продължителност |
|--------|-----------------|
| 1. Въведение | 2-3 мин |
| 2. Архитектура | 5 мин |
| **3. SOLID принципи** | **10 мин** (най-важна!) |
| 4. База данни | 5 мин |
| 5. Сигурност | 5 мин |
| 6. Ценообразуване | 5 мин |
| 7. Валидация | 4 мин |
| 8. Контрол на достъпа | 3 мин |
| 9. Справки | 3 мин |
| 10. Тестване | 3 мин |
| 11. Качество на кода | 2 мин |
| 12. Демонстрация на живо | 10 мин |
| 13. Въпроси и отговори | 15 мин |
| **Общо** | **~60 минути** |

---

## Бърза референтна карта

### Ключови файлове за познаване

| Файл | Цел | Показване за |
|------|-----|--------------|
| `pom.xml` | Зависимости | Въведение |
| `SecurityConfig.java` | JWT настройка | Секция за сигурност |
| `ShipmentController.java` | REST + DIP | Архитектура |
| `PricingServiceImpl.java` | SRP + OCP | SOLID |
| `ShipmentRequest.java` | Валидация | Валидация |
| `GlobalExceptionHandler.java` | Обработка на грешки | Валидация |
| `PricingServiceTest.java` | Unit тестване | Тестване |
| `schema.sql` | Дизайн на база данни | База данни |

### SOLID бързо напомняне

- **S**ingle Responsibility: Един клас = една причина за промяна
- **O**pen/Closed: Отворен за разширение, затворен за модификация
- **L**iskov Substitution: Подтиповете трябва да могат да заместват базовите типове
- **I**nterface Segregation: Никакви дебели интерфейси, фокусирани контракти
- **D**ependency Inversion: Зависимост от абстракции, не от конкретни реализации

---

**Успех с презентацията!**
