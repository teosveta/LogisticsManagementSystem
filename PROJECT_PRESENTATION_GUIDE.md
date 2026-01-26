# Система за управление на логистика - Ръководство за презентация

## Преглед на проекта

**Име на проекта:** Система за управление на логистика
**Технологичен стек:** Java 17, Spring Boot 3.2.0, Spring Security, JWT, MySQL, HTML/CSS/JavaScript
**Цел:** Пълноценно уеб приложение за управление на логистични операции, включително пратки, служители, клиенти, компании и офиси.

---

# РАЗПРЕДЕЛЕНИЕ НА РАБОТАТА

Проектът е разделен на **3 части** за презентация:

| Презентатор | Тема | Брой файлове |
|-------------|------|--------------|
| **Човек 1** | Фронтенд (UI слой) | ~13 файла |
| **Човек 2** | Бекенд ядро (Бизнес логика) | ~45 файла |
| **Човек 3** | Бекенд инфраструктура (Сигурност, API, Конфигурация) | ~25 файла |

---

# ЧОВЕК 1: ФРОНТЕНД (UI слой)

## Вашата отговорност
Вие сте отговорни за представянето на **потребителския интерфейс** на приложението - всичко, което потребителят вижда и с което взаимодейства в браузъра си.

## Файлове, които ще представите

### 1. HTML страници (3 файла)

#### `src/main/resources/static/login.html`
**Цел:** Страница за вход и регистрация за всички потребители.

**Ключови функции за обяснение:**
- Интерфейс с две форми: Форма за вход и Форма за регистрация
- Функционалност за превключване между формите (превключване между вход/регистрация)
- Полета за въвеждане: потребителско име, имейл, парола, избор на роля
- Контейнери за съобщения за грешки и успех
- Връзки към CSS и JavaScript файлове

**Кодови точки за подчертаване:**
```html
<!-- Форма за вход -->
<form id="loginForm">
    <input type="text" id="loginUsername" placeholder="Потребителско име" required>
    <input type="password" id="loginPassword" placeholder="Парола" required>
    <button type="submit">Вход</button>
</form>

<!-- Форма за регистрация -->
<form id="signupForm">
    <input type="text" id="signupUsername" placeholder="Потребителско име" required>
    <input type="email" id="signupEmail" placeholder="Имейл" required>
    <input type="password" id="signupPassword" placeholder="Парола" required>
    <select id="signupRole">
        <option value="CUSTOMER">Клиент</option>
        <option value="EMPLOYEE">Служител</option>
    </select>
    <button type="submit">Регистрация</button>
</form>
```

---

#### `src/main/resources/static/employee.html`
**Цел:** Табло за служители с пълни възможности за управление.

**Ключови функции за обяснение:**
- Странична навигация с множество секции
- Основна област за съдържание за динамично зареждане на изгледи
- Показване на информация за потребителя
- Функционалност за изход

**Секции в таблото:**
1. **Табло** - Преглед и метрики
2. **Регистриране на пратка** - Създаване на нови пратки
3. **Всички пратки** - Преглед и управление на всички пратки
4. **Компании** - Управление на логистични компании
5. **Офиси** - Управление на локации на офиси
6. **Служители** - Управление на персонала
7. **Клиенти** - Управление на клиентски акаунти
8. **Справки** - Преглед на бизнес справки и приходи
9. **Конфигурация** - Системни настройки (ценообразуване)

**Кодови точки за подчертаване:**
```html
<!-- Структура на странична навигация -->
<nav class="sidebar">
    <div class="nav-item" data-section="dashboard">Табло</div>
    <div class="nav-item" data-section="shipments">Пратки</div>
    <div class="nav-item" data-section="companies">Компании</div>
    <!-- ... още елементи -->
</nav>

<!-- Област за динамично съдържание -->
<main class="content">
    <div id="dashboard-view"></div>
    <div id="shipments-view"></div>
    <!-- ... още изгледи -->
</main>
```

---

#### `src/main/resources/static/customer.html`
**Цел:** Ограничено табло за клиенти.

**Ключови функции за обяснение:**
- Опростен интерфейс в сравнение със служителския
- Преглед само на собствени пратки (изпратени и получени)
- Проследяване на статус на пратка
- Не може да регистрира нови пратки (само за четене)

---

### 2. CSS стилове (3 файла)

#### `src/main/resources/static/css/login.css`
**Цел:** Стилизиране на страницата за вход/регистрация.

**Ключови функции за обяснение:**
- Стилизиране на контейнер за форма
- Стилизиране на полета за въвеждане
- Ефекти при задържане на бутон
- Цветове за съобщения за грешки/успех
- Адаптивен дизайн

---

#### `src/main/resources/static/css/employee.css`
**Цел:** Стилизиране на таблото за служители.

**Ключови функции за обяснение:**
- Оформление на страничната лента (фиксирана позиция)
- Адаптивна решетка за областта на съдържанието
- Стилизиране на таблици за показване на данни
- Компоненти карти за метрики
- Стилизиране на модални/изскачащи прозорци
- Активно състояние на навигация

---

#### `src/main/resources/static/css/customer.css`
**Цел:** Стилизиране на таблото за клиенти.

**Ключови функции за обяснение:**
- По-просто оформление от служителското
- Стилизиране на карти за пратки
- Значки за статус (цветове за различни статуси)

---

### 3. JavaScript файлове (7 файла)

#### `src/main/resources/static/js/login.js`
**Цел:** Обработва логиката за вход и регистрация.

**Ключови функции за обяснение:**
- Обработка на изпращане на форма
- API извиквания към `/api/auth/register` и `/api/auth/login`
- Съхранение на JWT токен в localStorage
- Пренасочване към съответното табло според ролята
- Показване на съобщения за грешки

**Кодови точки за подчертаване:**
```javascript
// Функция за вход
async function login(username, password) {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });

    if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('role', data.role);
        // Пренасочване според ролята
        window.location.href = data.role === 'EMPLOYEE'
            ? '/employee.html'
            : '/customer.html';
    }
}
```

---

#### `src/main/resources/static/js/api.js`
**Цел:** Централизиран API клиент за всички HTTP заявки.

**Ключови функции за обяснение:**
- Конфигурация на базов URL
- Инжектиране на Authorization хедър (Bearer токен)
- HTTP методи: GET, POST, PUT, DELETE, PATCH
- Обработка на грешки
- Парсване на отговори

**Кодови точки за подчертаване:**
```javascript
// API обвивка с автентикация
const api = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` })
        };

        const response = await fetch(endpoint, { ...options, headers });
        return response.json();
    },

    get: (url) => api.request(url, { method: 'GET' }),
    post: (url, data) => api.request(url, { method: 'POST', body: JSON.stringify(data) }),
    put: (url, data) => api.request(url, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (url) => api.request(url, { method: 'DELETE' })
};
```

---

#### `src/main/resources/static/js/employee.js`
**Цел:** Основна логика за таблото на служителя.

**Ключови функции за обяснение:**
- Зареждане и показване на данни от API
- Динамично генериране на форми за създаване/редактиране на обекти
- Рендериране на таблици с данни
- CRUD операции за всички обекти
- Актуализация на статус на пратки
- Генериране на справки

**Кодови точки за подчертаване:**
```javascript
// Зареждане и показване на всички пратки
async function loadShipments() {
    const shipments = await api.get('/api/shipments');
    renderShipmentsTable(shipments);
}

// Регистриране на нова пратка
async function registerShipment(shipmentData) {
    await api.post('/api/shipments', shipmentData);
    showSuccessMessage('Пратката е регистрирана успешно!');
    loadShipments();
}

// Актуализиране на статус на пратка
async function updateStatus(shipmentId, newStatus) {
    await api.patch(`/api/shipments/${shipmentId}/status`, { status: newStatus });
}
```

---

#### `src/main/resources/static/js/customer.js`
**Цел:** Логика за таблото на клиента.

**Ключови функции за обяснение:**
- Зареждане на собствените пратки на клиента
- Показване на изпратени и получени пратки
- Изглед за проследяване на пратка
- Операции само за четене (без създаване/редактиране)

---

#### `src/main/resources/static/js/navigation.js`
**Цел:** Обработва навигация по страници и превключване на изгледи.

**Ключови функции за обяснение:**
- Обработчици на кликване върху странична лента
- Логика за превключване на изгледи
- URL хеш маршрутизиране
- Управление на активно състояние

---

#### `src/main/resources/static/js/ui.js`
**Цел:** UI помощни функции.

**Ключови функции за обяснение:**
- Помощни функции за манипулация на DOM
- Управление на индикатор за зареждане
- Функции за показване/скриване на модални прозорци
- Помощни функции за генериране на таблици
- Помощни функции за валидация на форми

---

#### `src/main/resources/static/js/validation.js`
**Цел:** Валидация на входни данни от форми.

**Ключови функции за обяснение:**
- Валидация на формат на имейл
- Изисквания за сила на парола
- Проверки за задължителни полета
- Валидация на числови полета
- Показване на съобщения за грешки

---

## Как протичат данните във фронтенда

```
Действие на потребител (клик/изпращане)
        ↓
JavaScript обработчик на събитие
        ↓
API извикване (fetch с JWT токен)
        ↓
Отговор от бекенда (JSON)
        ↓
Актуализиране на DOM (показване на данни)
```

---

## Ключови концепции за обяснение

1. **Управление на JWT токен:**
   - Съхранява се в localStorage след вход
   - Изпраща се с всяка API заявка в Authorization хедър
   - Изчиства се при изход

2. **Потребителски интерфейс базиран на роли:**
   - Различни табла за СЛУЖИТЕЛ vs КЛИЕНТ
   - Служителите виждат всички функции
   - Клиентите виждат ограничени функции

3. **Динамично зареждане на съдържание:**
   - Подход на приложение с една страница
   - Съдържанието се зарежда чрез JavaScript
   - Не е необходимо опресняване на страницата

4. **Адаптивен дизайн:**
   - Работи на различни размери на екрана
   - Страничната лента се свива на мобилни устройства
   - Таблиците се превъртат хоризонтално

---

## Примерен скрипт за презентация

*"Аз бях отговорен за фронтенд частта на нашата Система за управление на логистика. Фронтендът се състои от 3 HTML страници, 3 CSS файла и 7 JavaScript файла.*

*Нека започна със страницата за вход. Когато потребител посети нашето приложение, той вижда форма за вход. Може или да влезе със съществуващи данни, или да превключи към формата за регистрация, за да създаде нов акаунт. Формата валидира входните данни и комуникира с нашия бекенд API.*

*След успешен вход, JWT токенът се съхранява в localStorage и потребителят се пренасочва към съответното табло според ролята си.*

*За служителите има изчерпателно табло със странична навигация. Те могат да управляват пратки, компании, офиси, служители, клиенти и да преглеждат справки. Всички операции се извършват чрез AJAX извиквания към нашия REST API.*

*Клиентите имат по-просто табло, където могат само да преглеждат собствените си пратки - както изпратени, така и получени.*

*JavaScript файловете обработват цялата логика: api.js управлява HTTP заявките с автентикация, employee.js обработва всички CRUD операции за таблото на служителя, а validation.js осигурява целостта на данните преди изпращане."*

---

---

# ЧОВЕК 2: БЕКЕНД ЯДРО (Слой на бизнес логика)

## Вашата отговорност
Вие сте отговорни за представянето на **бизнес логиката** на приложението - обекти, обекти за трансфер на данни, хранилища и услуги.

## Файлове, които ще представите

### 1. Entity класове (7 файла)
Местоположение: `src/main/java/com/logistics/model/entity/`

#### `User.java`
**Цел:** Представлява системни потребители за автентикация.

**Ключови функции за обяснение:**
- JPA Entity анотации (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`)
- Полета: id, username, email, password, role
- Времеви печати: createdAt, updatedAt с lifecycle callbacks
- Връзка: OneToOne със Employee ИЛИ Customer
- Role enum за EMPLOYEE/CUSTOMER

**Кодови точки за подчертаване:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt хеширана

    @Enumerated(EnumType.STRING)
    private Role role;

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

#### `Company.java`
**Цел:** Представлява организации на логистични компании.

**Ключови функции за обяснение:**
- Уникален регистрационен номер
- Информация за контакт (адрес, телефон, имейл)
- OneToMany връзка с Office
- OneToMany връзка с Employee
- Помощни методи за добавяне на офиси/служители

**Кодови точки за подчертаване:**
```java
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Office> offices = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<Employee> employees = new ArrayList<>();

    // Помощен метод
    public void addOffice(Office office) {
        offices.add(office);
        office.setCompany(this);
    }
}
```

---

#### `Office.java`
**Цел:** Представлява местоположения на клонове на компанията.

**Ключови функции за обяснение:**
- Детайли за местоположение (адрес, град, държава)
- ManyToOne връзка с Company
- OneToMany връзка с Employee
- Използва се като начало/дестинация за пратки
- Помощен метод: getFullAddress()

---

#### `Employee.java`
**Цел:** Представлява членове на персонала.

**Ключови функции за обяснение:**
- OneToOne връзка с User
- ManyToOne връзка с Company и Office
- Тип служител: COURIER или OFFICE_STAFF
- Поле за заплата (BigDecimal за точност)
- Помощни методи: isCourier(), isOfficeStaff()

**Кодови точки за подчертаване:**
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    private EmployeeType employeeType;

    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    public boolean isCourier() {
        return EmployeeType.COURIER.equals(employeeType);
    }
}
```

---

#### `Customer.java`
**Цел:** Представлява клиенти, които изпращат/получават пратки.

**Ключови функции за обяснение:**
- OneToOne връзка с User
- Информация за контакт (телефон, адрес)
- OneToMany връзки с Shipment (като подател И получател)
- Помощен метод: getAllShipments()

---

#### `Shipment.java`
**Цел:** Основен обект - представлява доставки на пакети.

**Ключови функции за обяснение:**
- Комплексни връзки:
  - ManyToOne с Customer (подател)
  - ManyToOne с Customer (получател)
  - ManyToOne с Employee (регистриран от)
  - ManyToOne с Office (начало)
  - ManyToOne с Office (доставка - опционално)
- Опции за доставка: до офис ИЛИ до адрес
- Status enum: REGISTERED → IN_TRANSIT → DELIVERED (или CANCELLED)
- Тегло и цена (BigDecimal)
- Времеви печати: registeredAt, deliveredAt

**Кодови точки за подчертаване:**
```java
@Entity
@Table(name = "shipments")
public class Shipment {
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Customer sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Customer recipient;

    @ManyToOne
    @JoinColumn(name = "delivery_office_id")
    private Office deliveryOffice;  // NULL ако доставка до адрес

    private String deliveryAddress;  // NULL ако доставка до офис

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.REGISTERED;

    public boolean isOfficeDelivery() {
        return deliveryOffice != null;
    }

    public String getDeliveryDestination() {
        return isOfficeDelivery()
            ? deliveryOffice.getFullAddress()
            : deliveryAddress;
    }
}
```

---

#### `PricingConfig.java`
**Цел:** Съхранява конфигурируеми стойности за ценообразуване.

**Ключови функции за обяснение:**
- Конфигурация, управлявана от база данни (не са необходими промени в кода)
- Полета: basePrice, pricePerKg, addressDeliveryFee
- Флаг за активност (само една конфигурация трябва да е активна)
- Всички парични полета използват BigDecimal

---

### 2. Enum класове (3 файла)
Местоположение: `src/main/java/com/logistics/model/enums/`

#### `Role.java`
```java
public enum Role {
    EMPLOYEE,
    CUSTOMER
}
```
**Цел:** Потребителски роли за оторизация.

---

#### `EmployeeType.java`
```java
public enum EmployeeType {
    COURIER,
    OFFICE_STAFF
}
```
**Цел:** Типове служители.

---

#### `ShipmentStatus.java`
```java
public enum ShipmentStatus {
    REGISTERED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
```
**Цел:** Състояния от жизнения цикъл на пратката.

---

### 3. DTO класове (15+ файла)
Местоположение: `src/main/java/com/logistics/dto/`

**Цел на DTO:**
- Отделяне на API контрактите от database entities
- Контрол на това какви данни се излагат
- Валидиране на входящи заявки
- Оформяне на данните в отговорите

**Ключови DTO за обяснение:**

#### Request DTO (вход от клиента):
- `RegisterRequest.java` - username, email, password, role
- `LoginRequest.java` - username, password
- `ShipmentRequest.java` - senderId, recipientId, weight, deliveryAddress/officeId
- `CompanyRequest.java`, `OfficeRequest.java`, `EmployeeRequest.java`, `CustomerRequest.java`

#### Response DTO (изход към клиента):
- `AuthResponse.java` - token, username, role
- `ShipmentResponse.java` - всички детайли на пратката с вложена информация за подател/получател
- `CompanyResponse.java`, `OfficeResponse.java`, `EmployeeResponse.java`, `CustomerResponse.java`

**Кодови точки за подчертаване:**
```java
// Request DTO с валидация
public class ShipmentRequest {
    @NotNull(message = "ID на подател е задължително")
    private Long senderId;

    @NotNull(message = "ID на получател е задължително")
    private Long recipientId;

    @NotNull(message = "Теглото е задължително")
    @DecimalMin(value = "0.1", message = "Теглото трябва да е поне 0.1 кг")
    private BigDecimal weight;

    // Или deliveryOfficeId ИЛИ deliveryAddress (валидира се в service)
    private Long deliveryOfficeId;
    private String deliveryAddress;
}

// Response DTO
public class ShipmentResponse {
    private Long id;
    private CustomerResponse sender;
    private CustomerResponse recipient;
    private EmployeeResponse registeredBy;
    private BigDecimal weight;
    private BigDecimal price;
    private String status;
    private String deliveryDestination;
    private LocalDateTime registeredAt;
    private LocalDateTime deliveredAt;
}
```

---

### 4. Repository интерфейси (7 файла)
Местоположение: `src/main/java/com/logistics/repository/`

**Цел:** Слой за достъп до данни, използвайки Spring Data JPA.

**Ключови хранилища:**

#### `UserRepository.java`
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### `ShipmentRepository.java`
```java
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findBySenderId(Long senderId);
    List<Shipment> findByRecipientId(Long recipientId);
    List<Shipment> findByRegisteredById(Long employeeId);
    List<Shipment> findByStatus(ShipmentStatus status);

    @Query("SELECT s FROM Shipment s WHERE s.sender.id = :customerId OR s.recipient.id = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED' AND s.deliveredAt BETWEEN :start AND :end")
    List<Shipment> findDeliveredBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
```

**Ключови функции за обяснение:**
- Наследява JpaRepository за CRUD операции
- Производни методи за заявки (findBy...)
- Персонализирани JPQL заявки с @Query
- Не е необходима имплементация - Spring я генерира

---

### 5. Service интерфейси и имплементации (16 файла)
Местоположение: `src/main/java/com/logistics/service/` и `src/main/java/com/logistics/service/impl/`

#### `PricingService.java` / `PricingServiceImpl.java`
**Цел:** Изчисляване на цени на пратки.

**Ключови функции за обяснение:**
- Единствена отговорност: обработва само ценообразуване
- Формула: Базова + (Тегло × ЦенаНаКг) + ТаксаЗаДоставка
- Чете конфигурация от база данни
- Доставка до офис = без допълнителна такса
- Доставка до адрес = допълнителна такса

**Кодови точки за подчертаване:**
```java
@Service
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;

    @Override
    public BigDecimal calculatePrice(BigDecimal weight, boolean isOfficeDelivery) {
        PricingConfig config = getActiveConfig();

        BigDecimal price = config.getBasePrice()
            .add(weight.multiply(config.getPricePerKg()));

        if (!isOfficeDelivery) {
            price = price.add(config.getAddressDeliveryFee());
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private PricingConfig getActiveConfig() {
        return pricingConfigRepository.findByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Няма активна конфигурация за ценообразуване"));
    }
}
```

---

#### `ShipmentService.java` / `ShipmentServiceImpl.java`
**Цел:** Управление на жизнения цикъл на пратките.

**Ключови функции за обяснение:**
- Регистриране на нови пратки (автоматично изчисляване на цена)
- Актуализиране на статус на пратка (валидиране на преходи)
- Получаване на пратки (всички, по клиент, по статус)
- Изтриване на пратки
- Логика за контрол на достъпа

**Кодови точки за подчертаване:**
```java
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeRepository officeRepository;
    private final PricingService pricingService;
    private final EntityMapper entityMapper;

    @Override
    public ShipmentResponse registerShipment(ShipmentRequest request, String employeeUsername) {
        // Валидиране, че подателят съществува
        Customer sender = customerRepository.findById(request.getSenderId())
            .orElseThrow(() -> new ResourceNotFoundException("Подателят не е намерен"));

        // Валидиране, че получателят съществува
        Customer recipient = customerRepository.findById(request.getRecipientId())
            .orElseThrow(() -> new ResourceNotFoundException("Получателят не е намерен"));

        // Получаване на регистриращия служител
        Employee employee = employeeRepository.findByUserUsername(employeeUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Служителят не е намерен"));

        // Изчисляване на цена
        boolean isOfficeDelivery = request.getDeliveryOfficeId() != null;
        BigDecimal price = pricingService.calculatePrice(request.getWeight(), isOfficeDelivery);

        // Създаване на пратка
        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setRegisteredBy(employee);
        shipment.setWeight(request.getWeight());
        shipment.setPrice(price);
        shipment.setStatus(ShipmentStatus.REGISTERED);

        if (isOfficeDelivery) {
            Office office = officeRepository.findById(request.getDeliveryOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Офисът не е намерен"));
            shipment.setDeliveryOffice(office);
        } else {
            shipment.setDeliveryAddress(request.getDeliveryAddress());
        }

        Shipment saved = shipmentRepository.save(shipment);
        return entityMapper.toShipmentResponse(saved);
    }

    @Override
    public ShipmentResponse updateStatus(Long id, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Пратката не е намерена"));

        // Валидиране на преход на статус
        validateStatusTransition(shipment.getStatus(), newStatus);

        shipment.setStatus(newStatus);
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        return entityMapper.toShipmentResponse(shipmentRepository.save(shipment));
    }
}
```

---

#### Други услуги:
- `AuthService.java` / `AuthServiceImpl.java` - Регистрация и вход
- `CompanyService.java` / `CompanyServiceImpl.java` - CRUD за компании
- `OfficeService.java` / `OfficeServiceImpl.java` - CRUD за офиси
- `EmployeeService.java` / `EmployeeServiceImpl.java` - CRUD за служители
- `CustomerService.java` / `CustomerServiceImpl.java` - CRUD за клиенти
- `ReportService.java` / `ReportServiceImpl.java` - Бизнес справки

---

### 6. Помощни класове (1 файл)
Местоположение: `src/main/java/com/logistics/util/`

#### `EntityMapper.java`
**Цел:** Конвертиране на JPA entities в DTO.

**Ключови функции за обяснение:**
- Разделяне на отговорностите
- Предотвратява проблеми с lazy loading
- Контролира излагането на данни

**Кодови точки за подчертаване:**
```java
@Component
public class EntityMapper {

    public ShipmentResponse toShipmentResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());
        response.setSender(toCustomerResponse(shipment.getSender()));
        response.setRecipient(toCustomerResponse(shipment.getRecipient()));
        response.setRegisteredBy(toEmployeeResponse(shipment.getRegisteredBy()));
        response.setWeight(shipment.getWeight());
        response.setPrice(shipment.getPrice());
        response.setStatus(shipment.getStatus().name());
        response.setDeliveryDestination(shipment.getDeliveryDestination());
        response.setRegisteredAt(shipment.getRegisteredAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        return response;
    }

    // Подобни методи за други обекти...
}
```

---

## Схема на базата данни
Файл: `src/main/resources/schema.sql`

**Таблици (общо 8):**
1. `users` - Данни за автентикация
2. `companies` - Логистични компании
3. `offices` - Местоположения на клонове
4. `employees` - Членове на персонала
5. `customers` - Клиентски акаунти
6. `shipments` - Доставки
7. `pricing_config` - Настройки за ценообразуване

**Ключови връзки:**
```
users ←──────── employees ────────→ companies
  │                │                    │
  │                ↓                    ↓
  └──────→ customers              offices
                │                    │
                ↓                    ↓
           shipments ←───────────────┘
```

---

## Приложени SOLID принципи

### 1. Принцип на единствената отговорност (SRP)
- `PricingService` обработва само логика за ценообразуване
- `EntityMapper` обработва само конвертиране от entity към DTO
- Всяка услуга обработва една област от домейна

### 2. Принцип отворен/затворен (OCP)
- Ценообразуването е конфигурируемо чрез база данни без промени в кода
- Нови услуги могат да се добавят без модификация на съществуващите

### 3. Принцип на заместване на Лисков (LSP)
- Всички имплементации на услуги са взаимозаменяеми чрез интерфейси

### 4. Принцип на разделяне на интерфейса (ISP)
- Малки, фокусирани интерфейси
- Отделни DTO за различни операции

### 5. Принцип на обръщане на зависимостите (DIP)
- Услугите зависят от интерфейси на хранилища, не от имплементации
- Инжектиране на зависимости чрез конструктор за всички зависимости

---

## Примерен скрипт за презентация

*"Аз бях отговорен за бекенд ядрото - слоя на бизнес логика на нашето приложение.*

*Нека започна с нашия модел на обекти. Имаме 7 JPA entities, представящи нашия домейн: User за автентикация, Company и Office за организационна структура, Employee и Customer за хора, Shipment за основния ни бизнес и PricingConfig за гъвкаво ценообразуване.*

*Entity Shipment е най-сложният - има връзки с Customer (подател и получател), Employee (който го е регистрирал) и Office (начало и доставка). Проследява статуса през жизнен цикъл: REGISTERED → IN_TRANSIT → DELIVERED.*

*За трансфер на данни използваме DTO, за да отделим нашите API контракти от database entities. Request DTO включват анотации за валидация, а Response DTO оформят това, което клиентите получават.*

*Repository слоят използва Spring Data JPA. Дефинираме интерфейси със сигнатури на методи и Spring генерира имплементацията. За сложни заявки използваме JPQL с анотацията @Query.*

*Service слоят съдържа нашата бизнес логика. Например, PricingService изчислява цени на пратки, използвайки конфигурируема формула. ShipmentService управлява целия жизнен цикъл на пратката, включително валидиране на преходи на статус и автоматично изчисляване на цени.*

*Следваме SOLID принципите навсякъде - единствена отговорност за всяка услуга, инжектиране на зависимости чрез интерфейси и отворен/затворен чрез конфигурация, управлявана от база данни."*

---

---

# ЧОВЕК 3: БЕКЕНД ИНФРАСТРУКТУРА (Сигурност, API и конфигурация)

## Вашата отговорност
Вие сте отговорни за представянето на **инфраструктурния слой** - сигурност, REST API контролери, конфигурация, обработка на изключения и тестване.

## Файлове, които ще представите

### 1. Слой за сигурност (4 файла)
Местоположение: `src/main/java/com/logistics/security/`

#### `JwtTokenProvider.java`
**Цел:** Генериране и валидиране на JWT токени.

**Ключови функции за обяснение:**
- Генериране на JWT токен с claims (username, role)
- Валидиране на токен
- Извличане на потребителска информация от токена
- Изтичане на токен (24 часа)
- Конфигурация на секретен ключ

**Кодови точки за подчертаване:**
```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

---

#### `JwtAuthenticationFilter.java`
**Цел:** Прихващане на заявки и валидиране на JWT токени.

**Ключови функции за обяснение:**
- Наследява OncePerRequestFilter
- Извлича токен от Authorization хедър
- Валидира токен
- Задава автентикация в SecurityContext
- Предава на следващия филтър

**Кодови точки за подчертаване:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---

#### `CustomUserDetailsService.java`
**Цел:** Зареждане на потребителски детайли за автентикация.

**Ключови функции за обяснение:**
- Имплементира UserDetailsService на Spring Security
- Зарежда потребител от база данни
- Изгражда UserDetails с роли
- Използва се от authentication manager

**Кодови точки за подчертаване:**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Потребителят не е намерен: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();
    }
}
```

---

#### `JwtAuthenticationEntryPoint.java`
**Цел:** Обработка на опити за неоторизиран достъп.

**Ключови функции за обяснение:**
- Имплементира AuthenticationEntryPoint
- Извиква се, когато неавтентикиран потребител се опита да достъпи защитен ресурс
- Връща 401 Unauthorized с JSON грешка

---

### 2. Конфигурационен слой (2 файла)
Местоположение: `src/main/java/com/logistics/config/`

#### `SecurityConfig.java`
**Цел:** Конфигуриране на Spring Security.

**Ключови функции за обяснение:**
- CSRF деактивиран (stateless API)
- Управление на сесии: STATELESS
- Конфигурация на публични крайни точки
- Защитените крайни точки изискват автентикация
- BCrypt password encoder
- Интеграция на JWT филтър

**Кодови точки за подчертаване:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Публични крайни точки
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/", "/*.html", "/css/**", "/js/**").permitAll()
                // Всички други крайни точки изискват автентикация
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

---

#### `OpenApiConfig.java`
**Цел:** Конфигуриране на Swagger/OpenAPI документация.

**Ключови функции за обяснение:**
- Заглавие, описание, версия на API
- Схема за сигурност (Bearer JWT)
- Конфигурация на URL на сървъра

---

### 3. Контролерен слой (8 файла)
Местоположение: `src/main/java/com/logistics/controller/`

#### `AuthController.java`
**Цел:** Обработка на крайни точки за автентикация.

**Крайни точки:**
- POST `/api/auth/register` - Регистрация на нов потребител (публичен)
- POST `/api/auth/login` - Вход и получаване на JWT (публичен)

**Кодови точки за подчертаване:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

---

#### `ShipmentController.java`
**Цел:** Обработка на операции с пратки.

**Ключови функции за обяснение:**
- Контрол на достъпа, базиран на роли с @PreAuthorize
- Получаване на текущия потребител от SecurityContext
- Смесен достъп: служителите виждат всичко, клиентите виждат собствените си
- CRUD операции с правилни HTTP методи

**Кодови точки за подчертаване:**
```java
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ShipmentResponse> registerShipment(
            @Valid @RequestBody ShipmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(shipmentService.registerShipment(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Служителите виждат всичко, клиентите виждат само собствените си
        if (hasRole(userDetails, "ROLE_EMPLOYEE")) {
            return ResponseEntity.ok(shipmentService.getAllShipments());
        } else {
            Long customerId = customerService.getCustomerIdByUsername(userDetails.getUsername());
            return ResponseEntity.ok(shipmentService.getShipmentsByCustomerId(customerId));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return ResponseEntity.ok(
            shipmentService.updateStatus(id, ShipmentStatus.valueOf(request.getStatus())));
    }
}
```

---

#### Други контролери:
- `CompanyController.java` - CRUD за компании (само EMPLOYEE)
- `OfficeController.java` - CRUD за офиси (само EMPLOYEE)
- `EmployeeController.java` - CRUD за служители (само EMPLOYEE)
- `CustomerController.java` - CRUD за клиенти (само EMPLOYEE)
- `PricingController.java` - Конфигурация на ценообразуване (само EMPLOYEE)
- `ReportController.java` - Бизнес справки (смесен достъп)

---

### 4. Обработка на изключения (6 файла)
Местоположение: `src/main/java/com/logistics/exception/`

#### Персонализирани изключения:
- `ResourceNotFoundException.java` - 404 Не е намерено
- `UnauthorizedException.java` - 403 Забранено
- `InvalidDataException.java` - 400 Лоша заявка
- `DuplicateResourceException.java` - 409 Конфликт

#### `ErrorResponse.java`
**Цел:** Последователен формат за отговор при грешка.

```java
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}
```

#### `GlobalExceptionHandler.java`
**Цел:** Централизирана обработка на изключения.

**Ключови функции за обяснение:**
- Анотация @RestControllerAdvice
- Обработчици за всеки тип изключение
- Последователен формат за отговор при грешка
- Правилни HTTP статус кодове

**Кодови точки за подчертаване:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            LocalDateTime.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Невалидно потребителско име или парола",
            LocalDateTime.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
```

---

### 5. Конфигурация на приложението

#### `src/main/resources/application.properties`
**Ключови конфигурации за обяснение:**

```properties
# Приложение
spring.application.name=logistics-management-system

# Връзка с база данни
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/logistics_db
spring.datasource.username=root
spring.datasource.password=****
spring.jpa.hibernate.ddl-auto=update

# JWT сигурност
jwt.secret=LogisticsManagementSystemSecretKeyForJWTTokenGeneration...
jwt.expiration=86400000

# Сървър
server.port=8080

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

### 6. Конфигурация на компилация

#### `pom.xml`
**Ключови зависимости за обяснение:**

```xml
<!-- Основни -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-boot-starter-validation</dependency>

<!-- База данни -->
<dependency>mysql-connector-j</dependency>

<!-- Сигурност -->
<dependency>jjwt-api</dependency>
<dependency>jjwt-impl</dependency>
<dependency>jjwt-jackson</dependency>

<!-- Документация -->
<dependency>springdoc-openapi-starter-webmvc-ui</dependency>

<!-- Разработка -->
<dependency>lombok</dependency>

<!-- Тестване -->
<dependency>spring-boot-starter-test</dependency>
<dependency>spring-security-test</dependency>
<dependency>h2</dependency>
```

---

### 7. Тестване (18 тестови файла)
Местоположение: `src/test/java/com/logistics/`

**Категории тестове:**

#### Service тестове:
- `AuthServiceTest.java`
- `PricingServiceTest.java`
- `ShipmentServiceTest.java`
- `CompanyServiceTest.java`
- `OfficeServiceTest.java`
- `EmployeeServiceTest.java`
- `CustomerServiceTest.java`
- `ReportServiceTest.java`

#### Controller тестове (интеграционни):
- `AuthControllerTest.java`
- `ShipmentControllerTest.java`
- `CompanyControllerTest.java`
- `OfficeControllerTest.java`
- `EmployeeControllerTest.java`
- `CustomerControllerTest.java`
- `PricingControllerTest.java`
- `ReportControllerTest.java`

#### Security тестове:
- `JwtTokenProviderTest.java`

#### Repository тестове:
- `ShipmentRepositoryTest.java`

**Ключови функции за обяснение:**
- JUnit 5 с Spring Boot Test
- Mockito за мокване на зависимости
- MockMvc за controller тестове
- H2 in-memory база данни за тестове
- @WebMvcTest за тестове на controller слоя
- @DataJpaTest за repository тестове

**Кодови точки за подчертаване:**
```java
@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void registerShipment_ValidRequest_ReturnsCreated() throws Exception {
        ShipmentRequest request = new ShipmentRequest();
        request.setSenderId(1L);
        request.setRecipientId(2L);
        request.setWeight(new BigDecimal("5.0"));

        when(shipmentService.registerShipment(any(), any()))
            .thenReturn(new ShipmentResponse());

        mockMvc.perform(post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void registerShipment_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

---

## API документация

**Swagger UI:** http://localhost:8080/swagger-ui.html

**Резюме на API крайни точки:**

| Метод | Крайна точка | Достъп | Описание |
|-------|--------------|--------|----------|
| POST | /api/auth/register | Публичен | Регистрация на потребител |
| POST | /api/auth/login | Публичен | Вход |
| GET/POST/PUT/DELETE | /api/companies/* | EMPLOYEE | CRUD за компании |
| GET/POST/PUT/DELETE | /api/offices/* | EMPLOYEE | CRUD за офиси |
| GET/POST/PUT/DELETE | /api/employees/* | EMPLOYEE | CRUD за служители |
| GET/POST/PUT/DELETE | /api/customers/* | EMPLOYEE | CRUD за клиенти |
| POST | /api/shipments | EMPLOYEE | Регистриране на пратка |
| GET | /api/shipments | Смесен | Получаване на пратки |
| PATCH | /api/shipments/{id}/status | EMPLOYEE | Актуализиране на статус |
| GET/PUT | /api/pricing/config | EMPLOYEE | Конфигурация на ценообразуване |
| GET | /api/reports/* | Смесен | Бизнес справки |

---

## Поток на автентикация

```
1. Потребителят изпраща идентификационни данни
        ↓
2. AuthController получава заявката
        ↓
3. AuthService валидира идентификационните данни
        ↓
4. JwtTokenProvider генерира токен
        ↓
5. Токенът се връща на клиента
        ↓
6. Клиентът съхранява токена (localStorage)
        ↓
7. Клиентът включва токена в заявките
   (Authorization: Bearer <токен>)
        ↓
8. JwtAuthenticationFilter прихваща
        ↓
9. Токенът се валидира, потребителят се автентикира
        ↓
10. Заявката продължава към контролера
```

---

## Примерен скрипт за презентация

*"Аз бях отговорен за бекенд инфраструктурата - сигурност, API контролери, конфигурация, обработка на изключения и тестване.*

*Нека започна със сигурността. Използваме JWT (JSON Web Tokens) за stateless автентикация. Когато потребител влезе, JwtTokenProvider генерира токен, съдържащ неговото потребителско име и роля. Този токен е валиден 24 часа.*

*Всяка следваща заявка включва този токен в Authorization хедъра. JwtAuthenticationFilter прихваща заявките, валидира токена и задава потребителя в SecurityContext на Spring.*

*SecurityConfig свързва всичко заедно - деактивира CSRF, тъй като сме stateless, конфигурира кои крайни точки са публични и добавя нашия JWT филтър към веригата за сигурност.*

*За REST API имаме 8 контролера. Всеки контролер обработва една област от домейна. Използваме @PreAuthorize за контрол на достъпа, базиран на роли - служителите могат да достъпват всичко, клиентите имат ограничен достъп.*

*Обработката на изключения е централизирана в GlobalExceptionHandler. Той прихваща всички изключения и връща последователни JSON отговори за грешки с правилни HTTP статус кодове.*

*Приложението е конфигурирано чрез application.properties - връзка с база данни, JWT настройки и порт на сървъра. Зависимостите се управляват в pom.xml.*

*Накрая, имаме изчерпателни тестове - 18 тестови класа, покриващи услуги, контролери, сигурност и хранилища. Използваме Mockito за unit тестове и MockMvc за интеграционни тестове, с H2 in-memory база данни за изолация на тестовете."*

---

---

# ОБОБЩАВАЩА ТАБЛИЦА

| Компонент | Човек 1 (Фронтенд) | Човек 2 (Бекенд ядро) | Човек 3 (Инфраструктура) |
|-----------|--------------------|-----------------------|--------------------------|
| HTML страници | login.html, employee.html, customer.html | - | - |
| CSS файлове | login.css, employee.css, customer.css | - | - |
| JavaScript | login.js, api.js, employee.js, customer.js, navigation.js, ui.js, validation.js | - | - |
| Entities | - | User, Company, Office, Employee, Customer, Shipment, PricingConfig | - |
| Enums | - | Role, EmployeeType, ShipmentStatus | - |
| DTO | - | Всички Request/Response DTO | - |
| Repositories | - | Всички 7 хранилища | - |
| Services | - | Всички 8 услуги + имплементации | - |
| EntityMapper | - | EntityMapper.java | - |
| Controllers | - | - | Всички 8 контролера |
| Security | - | - | JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService, JwtAuthenticationEntryPoint |
| Config | - | - | SecurityConfig, OpenApiConfig |
| Exceptions | - | - | Всички персонализирани изключения + GlobalExceptionHandler |
| Properties | - | - | application.properties |
| Build | - | - | pom.xml |
| Tests | - | - | Всички 18 тестови файла |
| Schema | - | schema.sql (обяснение на таблици) | - |

---

# ПРЕПОРЪКА ЗА РЕД НА ПРЕЗЕНТАЦИЯ

1. **Човек 3** започва - обяснява архитектурата на проекта, потока на сигурност и как се обработват заявките
2. **Човек 2** продължава - обяснява бизнес логиката, entities и как се обработват данните
3. **Човек 1** завършва - демонстрира потребителския интерфейс и показва как всичко работи заедно

Този ред има смисъл, защото:
- Сигурност/Инфраструктура задава контекста
- Бизнес логиката показва какво прави системата
- Фронтендът демонстрира крайния продукт

---

# ЧЕСТО ЗАДАВАНИ ВЪПРОСИ ЗА ПОДГОТОВКА

1. **Защо JWT вместо сесии?**
   - Stateless, мащабируем, не е необходимо съхранение от страна на сървъра

2. **Защо отделни DTO от entities?**
   - Сигурност (контрол на излаганите данни), гъвкавост, версиониране на API

3. **Защо Spring Data JPA?**
   - По-малко шаблонен код, автоматично генериране на заявки, лесна пагинация

4. **Как се изчислява ценообразуването?**
   - Базова + (Тегло × ЦенаНаКг) + ТаксаЗаДоставка

5. **Как обработвате грешки при автентикация?**
   - JwtAuthenticationEntryPoint връща 401 с JSON грешка

6. **Как осигурявате целостта на данните?**
   - Анотации за валидация, външни ключове, транзакционни услуги

7. **Как тествате приложението?**
   - Unit тестове (Mockito), интеграционни тестове (MockMvc), H2 база данни

8. **Какво се случва, ако токенът изтече?**
   - 401 Unauthorized, потребителят трябва да влезе отново

---

Успех с презентацията!
