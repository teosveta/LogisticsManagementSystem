# Logistics Management System

A **Spring Boot REST API** for managing a logistics company's operations including shipments, employees, customers, and offices.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.2.0 | Application Framework |
| Spring Data JPA | - | Database ORM |
| Spring Security | - | Authentication & Authorization |
| MySQL | 8.0 | Database |
| JWT (jjwt) | 0.12.3 | Token-based Authentication |
| Maven | - | Build Tool |
| Swagger/OpenAPI | 2.3.0 | API Documentation |

## Project Structure

```
src/main/java/com/logistics/
├── LogisticsApplication.java       # Main entry point
├── config/                         # Configuration classes
│   ├── SecurityConfig.java         # Spring Security config
│   └── OpenApiConfig.java          # Swagger config
├── controller/                     # REST Controllers
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── OfficeController.java
│   ├── ReportController.java
│   └── ShipmentController.java
├── dto/                            # Data Transfer Objects
│   ├── auth/
│   ├── company/
│   ├── customer/
│   ├── employee/
│   ├── office/
│   ├── report/
│   └── shipment/
├── exception/                      # Custom Exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── InvalidDataException.java
├── model/
│   ├── entity/                     # JPA Entities
│   └── enums/                      # Enumerations
├── repository/                     # JPA Repositories
├── security/                       # JWT Security
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/                        # Service Interfaces
│   └── impl/                       # Service Implementations
└── util/                           # Utility Classes
    └── EntityMapper.java
```

## Prerequisites

1. **Java 17** or higher
2. **MySQL 8.0** running on localhost:3306
3. **Maven** (or use the included wrapper)

## Database Setup

1. Start MySQL server
2. Create the database:
```sql
CREATE DATABASE logistics_db;
```

3. The application will auto-create tables on startup (ddl-auto=update)

## Configuration

Database settings in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/logistics_db
spring.datasource.username=root
spring.datasource.password=
```

## Running the Application

### Option 1: Using Maven
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Option 2: Using JAR
```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/logistics-management-system-1.0.0.jar
```

The application will start at: **http://localhost:8080**

## Loading Sample Data

After the application starts, run the SQL in `src/main/resources/data.sql` to populate sample data.

### Sample Users (Password: `password123` for all)

| Username | Role | Description |
|----------|------|-------------|
| admin | EMPLOYEE | Office staff at Downtown |
| john_courier | EMPLOYEE | Courier |
| jane_courier | EMPLOYEE | Courier |
| mike_staff | EMPLOYEE | Office staff at Airport |
| sarah_staff | EMPLOYEE | Office staff at Uptown |
| customer_alice | CUSTOMER | Customer |
| customer_bob | CUSTOMER | Customer |
| customer_carol | CUSTOMER | Customer |
| customer_david | CUSTOMER | Customer |
| customer_emma | CUSTOMER | Customer |

## API Documentation

### Swagger UI
Access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Quick Start: Authentication

1. **Login to get JWT token:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'
```

2. **Use the token in subsequent requests:**
```bash
curl -X GET http://localhost:8080/api/shipments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@email.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

### Create Shipment (Employee only)
```bash
curl -X POST http://localhost:8080/api/shipments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": 1,
    "recipientId": 2,
    "deliveryOfficeId": 1,
    "weight": 5.0
  }'
```

### Update Shipment Status (Employee only)
```bash
curl -X PATCH http://localhost:8080/api/shipments/1/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_TRANSIT"}'
```

### Get Revenue Report (Employee only)
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Access Control

| Feature | EMPLOYEE | CUSTOMER |
|---------|----------|----------|
| View all shipments | ✓ | ✗ |
| View own shipments | ✓ | ✓ |
| Create shipments | ✓ | ✗ |
| Update shipment status | ✓ | ✗ |
| Manage companies/offices | ✓ | ✗ |
| Manage employees/customers | ✓ | ✗ |
| Access all reports | ✓ | ✗ |
| Access own sent/received reports | ✓ | ✓ |

## Pricing Formula

```
Total Price = Base Price + (Weight × Price per kg) + Delivery Type Fee

Default values:
- Base Price: 5.00
- Price per kg: 2.00
- Address Delivery Fee: 10.00
- Office Delivery Fee: 0.00

Examples:
- 5kg to office:  5.00 + (5 × 2.00) + 0.00  = 15.00
- 5kg to address: 5.00 + (5 × 2.00) + 10.00 = 25.00
```

## SOLID Principles

This project strictly follows SOLID principles. See [SOLID_PRINCIPLES.md](SOLID_PRINCIPLES.md) for detailed explanations with code examples.

## API Endpoints

See [API_ENDPOINTS.md](API_ENDPOINTS.md) for complete endpoint documentation with request/response examples.

## Error Handling

All errors return a consistent JSON format:
```json
{
  "timestamp": "2024-01-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment not found with id: 999",
  "path": "/api/shipments/999"
}
```

| HTTP Status | Meaning |
|-------------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Resource Not Found |
| 409 | Conflict (duplicate resource) |
| 500 | Internal Server Error |

## License

MIT License
