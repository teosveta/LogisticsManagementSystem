# Logistics Management System

A Spring Boot REST API for managing a logistics company's shipments, employees, customers, and offices with JWT authentication and role-based access control.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.2.0 | Application Framework |
| Spring Security | - | Authentication & Authorization |
| Spring Data JPA | - | Database ORM |
| MySQL | 8.0 | Database |
| JWT (jjwt) | 0.12.3 | Token Authentication |
| Maven | - | Build Tool |
| Swagger/OpenAPI | 2.3.0 | API Documentation |

## Features

- User authentication with JWT tokens
- Role-based access control (EMPLOYEE / CUSTOMER)
- Complete CRUD for Companies, Offices, Employees, Customers, Shipments
- 8 comprehensive report endpoints
- Database-driven pricing configuration
- Automatic price calculation
- Responsive frontend interface

## Project Structure

```
src/main/java/com/logistics/
├── config/                     # Security & Swagger configuration
├── controller/                 # REST endpoints (7 controllers)
├── dto/                        # Request/Response objects with validation
├── exception/                  # Global exception handling
├── model/
│   ├── entity/                 # JPA entities (7 entities)
│   └── enums/                  # Role, EmployeeType, ShipmentStatus
├── repository/                 # Spring Data JPA repositories
├── security/                   # JWT token provider & filter
├── service/
│   ├── *Service.java           # Service interfaces
│   └── impl/                   # Service implementations
└── util/                       # EntityMapper utility

src/main/resources/
├── static/                     # Frontend (HTML, CSS, JS)
├── application.properties      # Configuration
└── schema.sql                  # Database schema reference
```

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0

### Installation

1. **Clone the repository**

2. **Create database**
   ```sql
   CREATE DATABASE logistics_db;
   ```

3. **Configure database** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://127.0.0.1:3306/logistics_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - Frontend: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Companies (Employee Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/companies` | Create company |
| GET | `/api/companies` | Get all companies |
| GET | `/api/companies/{id}` | Get company by ID |
| PUT | `/api/companies/{id}` | Update company |
| DELETE | `/api/companies/{id}` | Delete company |

### Offices, Employees, Customers
Same CRUD pattern as Companies at `/api/offices`, `/api/employees`, `/api/customers`

### Shipments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/shipments` | Register shipment (Employee) |
| GET | `/api/shipments` | Get shipments (filtered by role) |
| GET | `/api/shipments/{id}` | Get shipment by ID |
| PATCH | `/api/shipments/{id}/status` | Update status (Employee) |
| PUT | `/api/shipments/{id}` | Update shipment (Employee) |
| DELETE | `/api/shipments/{id}` | Delete shipment (Employee) |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/employees` | All employees (Employee) |
| GET | `/api/reports/customers` | All customers (Employee) |
| GET | `/api/reports/shipments` | All/own shipments |
| GET | `/api/reports/shipments/employee/{id}` | By employee (Employee) |
| GET | `/api/reports/shipments/pending` | Pending shipments (Employee) |
| GET | `/api/reports/shipments/customer/{id}/sent` | Customer's sent |
| GET | `/api/reports/shipments/customer/{id}/received` | Customer's received |
| GET | `/api/reports/revenue?startDate=X&endDate=Y` | Revenue (Employee) |

### Pricing (Employee Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pricing/config` | Get pricing configuration |
| PUT | `/api/pricing/config` | Update pricing |

## Pricing Formula

```
Total Price = Base Price + (Weight × Price Per Kg) + Delivery Fee

Default values (stored in database):
- Base Price: 5.00
- Price Per Kg: 2.00
- Address Delivery Fee: 10.00
- Office Delivery Fee: 0.00

Examples:
- 5kg to office:  5.00 + (5 × 2.00) + 0.00  = 15.00
- 5kg to address: 5.00 + (5 × 2.00) + 10.00 = 25.00
```

## Access Control

| Feature | EMPLOYEE | CUSTOMER |
|---------|----------|----------|
| View all shipments | Yes | No |
| View own shipments | Yes | Yes |
| Create/edit/delete shipments | Yes | No |
| Manage companies/offices | Yes | No |
| Manage employees/customers | Yes | No |
| Access all reports | Yes | No |
| Access own sent/received reports | Yes | Yes |

## SOLID Principles

This project follows SOLID principles:

**Single Responsibility (SRP):** Each service handles one domain. PricingService only calculates prices, ShipmentService only manages shipments, ReportService only generates reports.

**Open/Closed (OCP):** Pricing is configurable via database without code changes. New exception handlers can be added without modifying existing ones.

**Liskov Substitution (LSP):** All service implementations are interchangeable via interfaces. Controllers depend on interfaces, allowing easy substitution for testing.

**Interface Segregation (ISP):** Small, focused interfaces for each domain. Separate DTOs for different operations (ShipmentRequest vs ShipmentStatusUpdateRequest).

**Dependency Inversion (DIP):** Controllers depend on service interfaces, not implementations. Services depend on repository interfaces. This ensures proper layer separation.

## Database Schema

```
users          → Authentication data (username, email, password, role)
companies      → Logistics companies
offices        → Company branches (belongs to company)
employees      → Staff members (belongs to user, company, office)
customers      → Clients (belongs to user)
shipments      → Deliveries (links sender, recipient, employee, offices)
pricing_config → Configurable pricing values
```

See `src/main/resources/schema.sql` for complete DDL.

## Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
# View report: target/site/jacoco/index.html
```

Test classes:
- `PricingServiceTest` - Unit tests for pricing calculations
- `ShipmentServiceTest` - Unit tests for shipment operations
- `AuthControllerTest` - Integration tests for authentication
- `CustomerControllerTest` - Integration tests for customer endpoints

## Error Responses

All errors return consistent JSON format:
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment not found with id: 999",
  "path": "/api/shipments/999"
}
```

| Status | Meaning |
|--------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Resource Not Found |
| 409 | Conflict (duplicate resource) |

## License

University Project
