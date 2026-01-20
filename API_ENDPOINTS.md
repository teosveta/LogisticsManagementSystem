# API Endpoints Documentation

Base URL: `http://localhost:8080`

## Authentication

### Register New User
```
POST /api/auth/register
```

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@email.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 11,
  "username": "newuser",
  "email": "newuser@email.com",
  "role": "CUSTOMER"
}
```

### Login
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@logistics.com",
  "role": "EMPLOYEE"
}
```

---

## Companies (Employee Only)

### Create Company
```
POST /api/companies
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "Express Logistics",
  "registrationNumber": "EL-2024-002",
  "address": "200 Commerce Street, Boston, MA",
  "phone": "+1-555-400-0001",
  "email": "contact@expresslogistics.com"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "name": "Express Logistics",
  "registrationNumber": "EL-2024-002",
  "address": "200 Commerce Street, Boston, MA",
  "phone": "+1-555-400-0001",
  "email": "contact@expresslogistics.com",
  "createdAt": "2024-01-19T10:00:00",
  "updatedAt": "2024-01-19T10:00:00"
}
```

### Get Company by ID
```
GET /api/companies/{id}
Authorization: Bearer <token>
```

### Get All Companies
```
GET /api/companies
Authorization: Bearer <token>
```

### Update Company
```
PUT /api/companies/{id}
Authorization: Bearer <token>
```

### Delete Company
```
DELETE /api/companies/{id}
Authorization: Bearer <token>
```

---

## Offices (Employee Only)

### Create Office
```
POST /api/offices
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "companyId": 1,
  "name": "Brooklyn Branch",
  "address": "100 Atlantic Ave",
  "city": "Brooklyn",
  "country": "USA",
  "phone": "+1-555-500-0001"
}
```

**Response (201 Created):**
```json
{
  "id": 4,
  "companyId": 1,
  "companyName": "Swift Logistics Inc.",
  "name": "Brooklyn Branch",
  "address": "100 Atlantic Ave",
  "city": "Brooklyn",
  "country": "USA",
  "phone": "+1-555-500-0001",
  "fullAddress": "100 Atlantic Ave, Brooklyn, USA",
  "createdAt": "2024-01-19T10:00:00",
  "updatedAt": "2024-01-19T10:00:00"
}
```

### Get Office by ID
```
GET /api/offices/{id}
Authorization: Bearer <token>
```

### Get All Offices
```
GET /api/offices
Authorization: Bearer <token>
```

### Get Offices by Company
```
GET /api/offices/company/{companyId}
Authorization: Bearer <token>
```

### Update Office
```
PUT /api/offices/{id}
Authorization: Bearer <token>
```

### Delete Office
```
DELETE /api/offices/{id}
Authorization: Bearer <token>
```

---

## Employees (Employee Only)

### Create Employee
```
POST /api/employees
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "userId": 11,
  "companyId": 1,
  "employeeType": "COURIER",
  "officeId": null,
  "hireDate": "2024-01-19",
  "salary": 45000.00
}
```

**Response (201 Created):**
```json
{
  "id": 6,
  "userId": 11,
  "username": "newemployee",
  "email": "newemployee@logistics.com",
  "companyId": 1,
  "companyName": "Swift Logistics Inc.",
  "employeeType": "COURIER",
  "officeId": null,
  "officeName": null,
  "hireDate": "2024-01-19",
  "salary": 45000.00,
  "createdAt": "2024-01-19T10:00:00",
  "updatedAt": "2024-01-19T10:00:00"
}
```

### Get Employee by ID
```
GET /api/employees/{id}
Authorization: Bearer <token>
```

### Get All Employees
```
GET /api/employees
Authorization: Bearer <token>
```

### Update Employee
```
PUT /api/employees/{id}
Authorization: Bearer <token>
```

### Delete Employee
```
DELETE /api/employees/{id}
Authorization: Bearer <token>
```

---

## Customers (Employee Only)

### Create Customer
```
POST /api/customers
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "userId": 12,
  "phone": "+1-555-600-0001",
  "address": "123 New Street, New York, NY"
}
```

### Get Customer by ID
```
GET /api/customers/{id}
Authorization: Bearer <token>
```

### Get All Customers
```
GET /api/customers
Authorization: Bearer <token>
```

### Update Customer
```
PUT /api/customers/{id}
Authorization: Bearer <token>
```

### Delete Customer
```
DELETE /api/customers/{id}
Authorization: Bearer <token>
```

---

## Shipments

### Register Shipment (Employee Only)
```
POST /api/shipments
Authorization: Bearer <token>
```

**Request Body (Office Delivery):**
```json
{
  "senderId": 1,
  "recipientId": 2,
  "deliveryOfficeId": 1,
  "weight": 5.00
}
```

**Request Body (Address Delivery):**
```json
{
  "senderId": 1,
  "recipientId": 2,
  "deliveryAddress": "123 Main Street, Apt 4B, New York, NY 10001",
  "weight": 5.00
}
```

**Response (201 Created):**
```json
{
  "id": 13,
  "senderId": 1,
  "senderName": "customer_alice",
  "senderEmail": "alice@email.com",
  "recipientId": 2,
  "recipientName": "customer_bob",
  "recipientEmail": "bob@email.com",
  "registeredById": 1,
  "registeredByName": "admin",
  "deliveryAddress": null,
  "deliveryOfficeId": 1,
  "deliveryOfficeName": "Downtown Office",
  "deliveryDestination": "Office: Downtown Office - 123 Business Ave, New York, USA",
  "weight": 5.00,
  "price": 15.00,
  "status": "REGISTERED",
  "registeredAt": "2024-01-19T10:00:00",
  "deliveredAt": null,
  "updatedAt": "2024-01-19T10:00:00"
}
```

### Get Shipment by ID
```
GET /api/shipments/{id}
Authorization: Bearer <token>
```
*Note: Customers can only view shipments where they are sender or recipient.*

### Get All Shipments
```
GET /api/shipments
Authorization: Bearer <token>
```
*Note: Employees see all shipments. Customers see only their own.*

### Update Shipment Status (Employee Only)
```
PATCH /api/shipments/{id}/status
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "status": "IN_TRANSIT"
}
```

**Valid Status Values:** `REGISTERED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`

**Valid Transitions:**
- REGISTERED → IN_TRANSIT, CANCELLED
- IN_TRANSIT → DELIVERED, CANCELLED
- DELIVERED, CANCELLED → (no transitions)

### Update Shipment (Employee Only)
```
PUT /api/shipments/{id}
Authorization: Bearer <token>
```

### Delete Shipment (Employee Only)
```
DELETE /api/shipments/{id}
Authorization: Bearer <token>
```

---

## Reports

### All Employees Report (Employee Only)
```
GET /api/reports/employees
Authorization: Bearer <token>
```

### All Customers Report (Employee Only)
```
GET /api/reports/customers
Authorization: Bearer <token>
```

### All Shipments Report
```
GET /api/reports/shipments
Authorization: Bearer <token>
```
*Note: Employees see all. Customers see only their own.*

### Shipments by Employee (Employee Only)
```
GET /api/reports/shipments/employee/{employeeId}
Authorization: Bearer <token>
```

### Pending Shipments (Employee Only)
```
GET /api/reports/shipments/pending
Authorization: Bearer <token>
```

### Shipments Sent by Customer
```
GET /api/reports/shipments/customer/{customerId}/sent
Authorization: Bearer <token>
```
*Note: Customers can only view their own sent shipments.*

### Shipments Received by Customer
```
GET /api/reports/shipments/customer/{customerId}/received
Authorization: Bearer <token>
```
*Note: Customers can only view their own received shipments.*

### Revenue Report (Employee Only)
```
GET /api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer <token>
```

**Response:**
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "totalRevenue": 88.00,
  "deliveredShipmentsCount": 5
}
```

*Note: Only DELIVERED shipments are counted in revenue.*

---

## Error Responses

### Validation Error (400)
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have validation errors",
  "path": "/api/shipments",
  "validationErrors": {
    "weight": "Weight must be positive",
    "senderId": "Sender ID is required"
  }
}
```

### Unauthorized (401)
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid JWT token.",
  "path": "/api/shipments"
}
```

### Forbidden (403)
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/companies"
}
```

### Not Found (404)
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment not found with id: 999",
  "path": "/api/shipments/999"
}
```

### Conflict (409)
```json
{
  "timestamp": "2024-01-19T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "User already exists with username: 'admin'",
  "path": "/api/auth/register"
}
```
