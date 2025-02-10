# KASIKORN-LINE-BACKEND-ASSIGNMENT

---

# Mobile Banking Core Project

This Project is a backend system built with Spring Boot that provides essential banking functionalities such as user
management, account creation and management, debit card issuance, banner management, and authentication. The project
leverages Spring Data JPA for persistence, Liquibase for database versioning, and Spring Security for authentication.

---

# Features

- **User Management**  
  Create and manage users with secure password hashing and automatic greeting generation.

- **Account Management**  
  Create and manage different types of accounts including:
    - **SAVING**: Basic saving accounts.
    - **SAVING_GOAL**: Saving accounts with a savings goal and automatic progress calculation.
    - **LOAN**: Loan accounts with loan-specific fields such as loan amount, interest rate, term, and dynamic due date
      calculation.

- **Debit Card Management**  
  Issue debit cards for SAVING accounts with a uniquely generated card number in the format `xxxx xxxx xxxx xxxx`.

- **Banner Fetch and Initialization**
  Provides a fetch API to retrieve the banner and an initialization function, invoked by the User Service during user
  creation, to assign the initial banner to the user.

- **Authentication & Security**  
  Secure API endpoints using Spring Security. The authenticated user's ID is obtained from the security context for
  various operations.

- **Database Migrations & Seeding**  
  Manage schema changes and seed initial data using Liquibase.

---

# Technology Stack

- **Java 23** (or later)
- **Spring Boot 3.4.2** (Spring Data JPA, Spring Security, Spring Web)
- **Liquibase** for database versioning
- **MySQL** as the database
- **Lombok** for reducing boilerplate code
- **JWT** for authentication token

---

# Setup & Running the Project

## Prerequisites

- *This project was built with following:*
- **Java 23:** Make sure you have the latest JDK 23 installed.
- **Maven 3.9.9:** Confirm you have Maven 3.9.9 or later.
- **MySQL 8:** Ensure you have a running MySQL 8 instance with proper credentials.
- **Docker Desktop:** (Optional) for running the project in containers.
- **Internet Connection:** (Optional) required for Docker to pull images.

## Configuration

Update the `src/main/resources/application.properties` file with your database and JWT settings. For example:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/mobile_banking?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# JPA & Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
# Liquibase
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.liquibase.enabled=true
# JWT properties
jwt.secret=e419ufsdhf8wqedaoijdasdk...
jwt.expirationMs=600000
#Spring security
spring.security.user.name=none
spring.security.user.password=none
```

- *If you prefer using separate environment-specific properties (e.g., `application-local.properties`), remember to
  set `spring.profiles.active=local` accordingly.*

---

# Build & Run

## Local Run

If all requirements are met, build and run the project with:

```properties
mvn clean install
mvn spring-boot:run
```

## Using Docker

This project includes a `docker-compose.yml` and a `Dockerfile` in the root directory. The preconfigured environment
variables are:

```properties
port:8080
SPRING_DATASOURCE_URL:jdbc:mysql://db:3306/mobile_banking
SPRING_DATASOURCE_USERNAME:root
SPRING_DATASOURCE_PASSWORD:P@ssw0rd
SPRING_LIQUIBASE_CHANGELOG:classpath:/db/changelog/db.changelog-master.xml
SPRING_LIQUIBASE_DEFAULT_SCHEMA:mobile_banking
```

To build and run using Docker:

```properties
mvn clean package -DskipTests
docker-compose up --build
```

---

# API Specification

This document describes the API endpoints for the Mobile Banking application. *Protected* Api require token for
authentication which can get from *Login* api.

## 1. User Endpoint

### 1.1 Create User

This endpoint is used to register a new user in the system.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/user/create`

### Request Headers

- `Content-Type: application/json`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "user_id": "john_doe",
  "name": "John Doe",
  "password": "password1234"
}
```

- **user_id:** (String, Required) Unique identifier for the user.
- **name:** (String, Required) The name of the user.
- **password:** (String, Required) Must be between 8 and 16 characters.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "response_status": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "user_id": "john_doe",
    "name": "John Doe",
    "banner_id": "banner123",
    "greeting": "Have a nice day John Doe"
  }
}
```

- **response_status:** Contains the status (000 for success or 999 for fail) and a message.
- **data:** Contains response class of respective api.

### 1.2 Create PIN (Protected)

This endpoint is used to set up a PIN for an existing user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/user/pin/create`

### Request Headers

- `Content-Type: application/json`
- `Authorization: {{token}}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "pin": "123456"
}
```

- **pin:** (String, Required) String value of numeric, must be exactly 6 digits.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "response_status": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "user_id": "john_doe"
  }
}
```

## 2. Auth Endpoint

### 2.1 Login

This endpoint is used to authorize user with login credentials.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/login`

### Request Headers

- `Content-Type: application/json`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "user_id": "john_doe",
  "password": "password1234",
  "pin": "123456"
}
```

- **user_id:** (String, Required) Unique identifier for the user.
- **password:** (String, Conditional) Must be between 8 and 16 characters.
- **pin:** (String, Conditional) Must be 6 numeric characters only.

**either password or pin must be provided*

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "response_status": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "user_id": "john_doe",
    "name": "John Doe",
    "token": "xAWasdawe0CAs912SAcasldjn...",
    "banner_id": "banner123",
    "greeting": "Welcome John Doe!",
    "message": "Login successful"
  }
}
```

## 3. Account Endpoint

### 3.1 Account Create (Protected)

This endpoint is used to register new account to user id.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/account/create`

### Request Headers

- `Content-Type: application/json`
- `Authorization: {{token}}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "color": "#AAAAAA",
  "account_name": "new acc2",
  "is_main": true,
  "type": "saving_goal",
  "currency": "THB",
  "initial_balance": 100.00,
  "saving_goal": 500.00,
  "loan_amount": 50000.00,
  "loan_term": 4
}
```

- **color:** (String, Required) The color associated with the account.
- **is_main:** (Boolean, Optional) Indicates whether this account is the primary account. Defaults to `false` if not
  provided.
- **account_name:** (String, Required) The name of the account.
- **type:** (String, Required) Specifies the type of account. Allowed values: `SAVING`, `LOAN`, `SAVING_GOAL`.
- **currency:** (String, Required) The currency code for the account. Allowed values: `THB`, `USD`, `JPY`, `CNY`.
- **initial_balance:** (BigDecimal, Optional) The initial balance of the account.
- **saving_goal:** (BigDecimal, Optional) The target saving goal amount for the account.
- **loan_amount:** (BigDecimal, Optional) The amount of the loan (applicable if the account type is `LOAN`).
- **loan_term:** (Integer, Optional) The term or duration of the loan in months (applicable if the account type is
  `LOAN`).

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "account_id": "8c95c823-f4d9-43a2-a3b3-5f74ae8dc9ae",
    "account_name": "new acc2",
    "user_id": "user001",
    "color": "#AAAAAA",
    "is_main_account": false,
    "progress": 20,
    "type": "SAVING_GOAL",
    "currency": "THB",
    "account_balance": 100.00,
    "saving_goal": 500.00,
    "account_number": "303-9-49611-6",
    "issuer": "TestLab",
    "due_date": null,
    "loan_amount": null,
    "interest_rate": null,
    "loan_term": null,
    "loan_start_date": null,
    "account_status": "ACTIVE"
  }
}
```

### 3.2 Account Fetch (Protected)

This endpoint is used to fetch the list of accounts for the authenticated user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/account/fetch`

### Request Headers

- `Content-Type: application/json`
- `Authorization: {{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "limit": 5,
  "page": 0
}
```

- **limit:** (Integer, Optional) The number of account records to fetch. Must be at least 1. Defaults to 10 if not
  provided.
- **page:** (Integer, Optional) The page index for pagination. Must be at least 0. Defaults to 0 if not provided.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure. The `data` field contains an array of
account objects with the following properties:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "accounts": [
      {
        "accountId": "u001-l001",
        "accountName": "House Loan",
        "userId": "user001",
        "color": "#AAAAAA",
        "isMainAccount": true,
        "progress": null,
        "type": "LOAN",
        "currency": "USD",
        "accountBalance": 0.00,
        "savingGoal": null,
        "accountNumber": "568-2-21484-9",
        "issuer": "BankD",
        "dueDate": "2027-01-01T07:00:00",
        "paymentAccml": 5000.00,
        "interestAccml": 1520.34,
        "loanAmount": 50000.00,
        "remaining": 45000.00,
        "interestRate": 5.50,
        "loanTerm": 24,
        "loanStartDate": "2025-01-01T07:00:00",
        "accountStatus": "ACTIVE",
        "flags": [
          "Flag3",
          "Disbursement",
          "Overdue"
        ]
      },
      {
        "accountId": "fbfc2884-4382-46a3-8312-d816859e911b",
        "accountName": "new acc",
        "userId": "user001",
        "color": "#AAAAAA",
        "isMainAccount": false,
        "progress": null,
        "type": "LOAN",
        "currency": "THB",
        "accountBalance": null,
        "savingGoal": null,
        "accountNumber": "664-2-63436-4",
        "issuer": "TEST",
        "dueDate": "2025-06-07T11:44:40",
        "paymentAccml": 0.00,
        "interestAccml": 41.10,
        "loanAmount": 50000.00,
        "remaining": 50000.00,
        "interestRate": 10.00,
        "loanTerm": 4,
        "loanStartDate": "2025-02-07T11:44:40",
        "accountStatus": "ACTIVE",
        "flags": []
      }
    ]
  }
}
```

### 3.3 Account Flag Create (Protected)

This endpoint is used to create a flag for a specific account.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/account/flag/create`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "account_id": "8c95c823-f4d9-43a2-a3b3-5f74ae8dc9ae",
  "flag_value": "FLAG_EXAMPLE"
}
```

- **account_id:** (String, Required) Unique identifier for the account.
- **flag_value:** (String, Required) The flag value to be assigned to the account.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "account_id": "8c95c823-f4d9-43a2-a3b3-5f74ae8dc9ae",
    "flag_value": "FLAG_EXAMPLE"
  }
}
```

## 4. Transaction Endpoint

### 4.1 Transaction Fetch Recent (Protected)

This endpoint is used to fetch recent transactions for the authenticated user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/txn/fetch`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "limit": 5,
  "page": 0
}
```

- **limit:** (Integer, Optional) The number of transaction records to fetch. Must be at least 1. Defaults to 10 if not
  provided.
- **page:** (Integer, Optional) The page index for pagination. Must be at least 0. Defaults to 0 if not provided.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure. The `data` field contains an array of
transaction objects with the following properties:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "txnList": [
      {
        "transaction_id": "txn12345",
        "user_id": "user001",
        "name": "Payment Received",
        "image": "https://example.com/images/txn.png",
        "is_bank": true,
        "created_at": "2025-02-10T12:34:56"
      }
    ]
  }
}
```

### 4.2 Transaction Payment (Protected)

This endpoint is used to process a payment transaction for the authenticated user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/payment`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "transaction_type": "PAYMENT",
  "amount": 100.00,
  "name": "Payment Name",
  "account_id": "account_id_example"
}
```

- **transaction_type:** (String, Required) The type of transaction. Allowed values: `DEPOSIT`, `WITHDRAW`, `QR_PAYMENT`,
  `PAYMENT`.
- **amount:** (BigDecimal, Required) The amount for the transaction.
- **name:** (String, Required) A name or description for the transaction.
- **account_id:** (String, Required) The identifier of the account on which the transaction is to be performed.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "txnId": "93c18e02-e13d-4c5e-aef8-78280897bccf"
  }
}
```

## 5. Debit Card Endpoint

### 5.1 Debit Card Fetch (Protected)

This endpoint is used to fetch debit cards for the authenticated user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/debit/fetch`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "limit": 5,
  "page": 0
}
```

- **limit:** (Integer, Optional) The number of debit card records to fetch. Must be at least 1. Defaults to 10 if not
  provided.
- **page:** (Integer, Optional) The page index for pagination. Must be at least 0. Defaults to 0 if not provided.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure. The `data` field contains an array of
debit card objects with the following properties:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "debits": [
      {
        "cardId": "00189796e1a211ef95a30242ac180002",
        "status": "Active",
        "name": "My Debit Card",
        "issuer": "TestLab",
        "number": "7155 78xx xxxx 5723",
        "color": "#00a1e2",
        "borderColor": "#ffffff",
        "createdAt": "2025-02-07T09:56:46"
      },
      {
        "cardId": "0018e221e1a211ef95a30242ac180007",
        "status": "Active",
        "name": "My Debit Card",
        "issuer": "TestLab",
        "number": "6909 53xx xxxx 4765",
        "color": "#00a1e2",
        "borderColor": "#ffffff",
        "createdAt": "2025-02-07T09:56:46"
      }
    ]
  }
}
```

### 5.2 Debit Card Create (Protected)

This endpoint is used to create a debit card for the authenticated user.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/debit/create`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

The payload should be a JSON object with the following structure:

```json
{
  "account_id": "account_id_example",
  "name": "My Debit Card",
  "color": "#FFFFFF",
  "border_color": "#000000"
}
```

- **account_id:** (String, Required) Unique identifier for the account to link the debit card.
- **name:** (String, Required) The name assigned to the debit card.
- **color:** (String, Required) The primary color for the debit card.
- **border_color:** (String, Required) The border color for the debit card.

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "status": "ACTIVE",
    "user_id": "user001",
    "account_id": "account_id_example",
    "name": "My Debit Card",
    "issuer": "TEST_LAB",
    "number": "1234 5678 9012 3456",
    "color": "#FFFFFF",
    "border_color": "#000000"
  }
}
```

## 6. Banner Endpoint

### 6. Banner (Protected)

This endpoint is used to retrieve banners for the authenticated user. The user ID is extracted from the token, so no
request body is required.

**HTTP Method:** `POST`  
**Endpoint:** `/api/v1/mb/banner`

### Request Headers

- Content-Type: `application/json`
- Authorization: `{{token}}`

### Request Body

_No request body required._

### Response

A successful response returns a JSON object wrapped in an `ApiResponse` structure. The `data` field contains an array of
banner objects with the following properties:

```json
{
  "responseStatus": {
    "status": "000",
    "message": "Success"
  },
  "data": {
    "banners": [
      {
        "title": "Welcome Banner",
        "description": "Welcome to our service!"
      },
      {
        "title": "Promo Banner",
        "description": "Enjoy our exclusive offers."
      }
    ]
  }
}
```

