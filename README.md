# 🏦 Banking Management System (BMS)

![Java](https://img.shields.io/badge/Java-17%20LTS-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=for-the-badge&logo=postgresql)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate)
![Build Status](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-20%20Passed-success?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

> **A production-grade, monolithic backend Banking Management System built from scratch as a major solo project for campus placements (Finzly & Fintech roles).**

---

## 👨‍💻 Project Metadata & Ownership

- **Author:** Vaibhavi Shingade
- **Role:** Sole Developer & Software Architect
- **Development Timeframe:** 4 Weeks (Solo Effort)
- **Primary Focus:** Financial Precision (`BigDecimal`), Atomic Money Transfers (`@Transactional`), Clean 3-Layer Architecture, Enterprise Validation & Exception Handling.

---

## 🗓️ 4-Week Solo Development Journey & Milestones

This project was planned, designed, coded, and tested independently over a structured **4-week timeline**:

```
 📅 WEEK 1                📅 WEEK 2                📅 WEEK 3                📅 WEEK 4
 ──────────────────────   ──────────────────────   ──────────────────────   ──────────────────────
 ▫ System Requirements    ▫ Spring Data Repos      ▫ Transaction Engine     ▫ REST Controllers
 ▫ Database Design        ▫ Custom JPQL Queries    ▫ Money Transfer Logic   ▫ Unit Tests (JUnit/Mockito)
 ▫ Entity Mapping (JPA)   ▫ DTO Design Pattern     ▫ @Transactional Engine  ▫ Postman Collection
 ▫ Maven & Properties     ▫ Customer/Account Service ▫ Custom Exception Handler ▫ Documentation & Refactoring
```

### 🔹 Week 1: Architecture, Database Design & JPA Entities
* Conducted domain research on core banking operations (accounts, ledger records, transaction audit trails).
* Designed relational database schemas in PostgreSQL (`customers`, `accounts`, `transactions`).
* Implemented JPA/Hibernate entities with explicit relationships (`@OneToMany`, `@ManyToOne`) and indexing strategies.
* Configured Maven dependencies, multi-environment property sources, and project structure.

### 🔹 Week 2: Repositories, DTO Layer & Core Customer/Account Services
* Created Spring Data JPA repository interfaces for all entities.
* Engineered custom JPQL queries with optional parameters (`:type IS NULL OR...`) for transaction history filtering.
* Implemented Data Transfer Objects (DTOs) for request/response encapsulation to avoid leaking internal entity states.
* Built `CustomerServiceImpl` and `AccountServiceImpl` with customer code and account number generation algorithms.

### 🔹 Week 3: Money Transfer Engine, Transaction Management & Validation
* Built the core banking engine for **Deposit, Withdrawal, and Inter-Account Money Transfer**.
* Applied `@Transactional` to enforce strict ACID compliance — preventing partial updates during network or DB failures.
* Replaced floating-point numbers with `BigDecimal` to ensure zero decimal loss in monetary calculations.
* Implemented Jakarta Bean Validation and `@RestControllerAdvice` (`GlobalExceptionHandler`) for structured 400/404/409 error handling.

### 🔹 Week 4: REST Controllers, Comprehensive Unit Testing & Postman Collection
* Built RESTful controllers adhering to proper HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`) and status codes (`201 Created`, `204 No Content`).
* Authored a 20-test suite using **JUnit 5 & Mockito** verifying success paths and business rule failures (insufficient funds, blocked accounts).
* Created a complete **Postman Collection** with realistic sample payloads and environment variables.
* Finalized developer documentation (`README.md`, `API_DOCUMENTATION.md`, `INTERVIEW_PREPARATION.md`).

---

## 🌟 Key Features & Engineering Highlights

| Feature | Description | Technical Implementation |
|---------|-------------|--------------------------|
| **Atomic Money Transfer** | Atomically moves funds between accounts with full rollback guarantee on failure. | `@Transactional`, `@Service`, `BigDecimal` |
| **Financial Precision** | Implements exact decimal calculations; zero rounding errors. | Java `BigDecimal` (`add()`, `subtract()`, `compareTo()`) |
| **Account Lifecycle** | Supports account creation, status transitions (`ACTIVE`, `BLOCKED`, `CLOSED`). | Enum mapping, validation rules |
| **Transaction History & Filter** | Queries detailed ledger records with optional date range and transaction type filters. | Custom JPQL with optional params |
| **Enterprise Exception Framework** | Converts all business rule violations into standard JSON responses. | `@RestControllerAdvice`, Custom Exceptions |
| **Input Validation** | Rejects invalid email formats, negative transfer amounts, and blank fields. | Jakarta Bean Validation (`@NotBlank`, `@Positive`, etc.) |
| **Unit Test Suite** | 20 unit tests validating business rules without database dependencies. | JUnit 5, Mockito |

---

## 🏗️ Architecture & Component Flow

The project follows a **strict layered architecture** ensuring separation of concerns:

```text
┌─────────────────────────────────────────────────────────────┐
│                 Client Layer (Postman / Web UI)             │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / JSON
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Controller Layer (@RestController)              │
│   • CustomerController  • AccountController                 │
│   • TransactionController                                   │
│   (Validates inputs via @Valid, routes requests, no logic)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Java DTOs
                               ▼
┌─────────────────────────────────────────────────────────────┐
│               Service Layer (@Service Interfaces)           │
│   • CustomerServiceImpl  • AccountServiceImpl               │
│   • TransactionServiceImpl                                  │
│   (ACID @Transactional transactions, monetary business rules)│
└──────────────────────────────┬──────────────────────────────┘
                               │ JPA Entities
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Repository Layer (@Repository JPA)              │
│   • CustomerRepository  • AccountRepository                 │
│   • TransactionRepository                                   │
│   (Spring Data JPA, Hibernate ORM, JPQL Queries)            │
└──────────────────────────────┬──────────────────────────────┘
                               │ SQL
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 PostgreSQL Database (banking_db)            │
│   Tables: customers, accounts, transactions                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema & Relationships

```text
  ┌────────────────────────┐         ┌────────────────────────┐         ┌────────────────────────┐
  │       customers        │         │        accounts        │         │      transactions      │
  ├────────────────────────┤         ├────────────────────────┤         ├────────────────────────┤
  │ id (PK)                │ 1     * │ id (PK)                │ 1     * │ id (PK)                │
  │ customer_code (UNIQUE) │─────────│ account_number (UNIQUE)│─────────│ transaction_reference  │
  │ first_name             │         │ account_type           │         │ transaction_type       │
  │ last_name              │         │ balance (DECIMAL)      │         │ amount (DECIMAL)       │
  │ email (UNIQUE)         │         │ status                 │         │ balance_after_txn      │
  │ phone                  │         │ customer_id (FK)       │         │ description            │
  │ address                │         │ created_at             │         │ account_id (FK)        │
  │ created_at             │         │ updated_at             │         │ created_at             │
  └────────────────────────┘         └────────────────────────┘         └────────────────────────┘
```

---

## 📡 API Endpoint Reference

### 1️⃣ Customer APIs
- `POST /api/customers` — Create customer (Returns 201 Created)
- `GET /api/customers` — Retrieve all customers
- `GET /api/customers/{id}` — Get customer by ID
- `PUT /api/customers/{id}` — Update customer details
- `DELETE /api/customers/{id}` — Delete customer (Returns 204 No Content)

### 2️⃣ Account APIs
- `POST /api/accounts` — Create new bank account (`SAVINGS` / `CURRENT`)
- `GET /api/accounts` — Retrieve all accounts
- `GET /api/accounts/{id}` — Get account by ID
- `GET /api/accounts/number/{accountNumber}` — Lookup account by string number (e.g., `ACC100001`)
- `PUT /api/accounts/{id}/status` — Update account status (`ACTIVE`, `BLOCKED`, `CLOSED`)

### 3️⃣ Transaction & Money Transfer APIs
- `POST /api/accounts/{accountNumber}/deposit` — Deposit money into account
- `POST /api/accounts/{accountNumber}/withdraw` — Withdraw money (verifies sufficient balance)
- `POST /api/transactions/transfer` — Transfer money between accounts (Atomic `@Transactional`)
- `GET /api/accounts/{accountNumber}/transactions` — Get account transaction history (Supports query params: `transactionType`, `fromDate`, `toDate`)

---

## 💡 Transaction Management & ACID Principles

The money transfer feature is engineered around the **ACID paradigm**:

```java
@Transactional
public TransferResponse transfer(TransferRequest request) {
    // 1. Validate source and destination are different
    // 2. Lock & fetch source account
    // 3. Lock & fetch destination account
    // 4. Verify source has sufficient balance (balance >= amount)
    // 5. Deduct from source: sourceBalance.subtract(amount)
    // 6. Add to destination: destBalance.add(amount)
    // 7. Persist both account states to PostgreSQL
    // 8. Generate dual audit transaction entries
    // --> If ANY exception occurs during steps 1-8, Spring triggers
    //     an immediate ROLLBACK. Neither account balance is modified.
}
```

---

## 🚦 Getting Started & Local Setup

### Prerequisites
- **Java 17 LTS** or newer
- **Maven 3.8+**
- **PostgreSQL 15+**
- IDE (IntelliJ IDEA / VS Code)

### 1. Database Creation
Create the PostgreSQL database named `banking_db`:
```sql
CREATE DATABASE banking_db;
```

### 2. Environment Configuration
Copy `.env.example` to `.env` or set your environment variable:
```powershell
# Windows PowerShell
$env:DB_PASSWORD="your_postgres_password"
```

### 3. Build & Run
```bash
# Clone the repository
git clone https://github.com/vgshingade12/banking-management-system.git
cd banking-management-system

# Run unit tests
mvn clean test

# Run application
mvn spring-boot:run
```
The application will launch on `http://localhost:8080`.

---

## 🧪 Testing with Postman

1. Open Postman -> **Import** -> Select `postman/Banking-Management-System.postman_collection.json`.
2. Set variable `baseUrl` = `http://localhost:8080`.
3. Follow the automated execution sequence:
   - Create Customers -> Create Accounts -> Deposit Funds -> Perform Transfer -> View Audit Trail.

---

## 📄 Developer & Placement Credentials

Project built by **Vaibhavi Shingade** as a major capstone project for Finzly placement preparation.
Includes detailed interview guide in [INTERVIEW_PREPARATION.md](file:///C:/Users/PRASAD%20MAHAJAN/Desktop/ba/banking-management-system/INTERVIEW_PREPARATION.md) and full API specification in [API_DOCUMENTATION.md](file:///C:/Users/PRASAD%20MAHAJAN/Desktop/ba/banking-management-system/docs/API_DOCUMENTATION.md).
