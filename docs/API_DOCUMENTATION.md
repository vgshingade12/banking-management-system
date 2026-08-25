# Banking Management System API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Content-Type:** `application/json`

---

## Standard Error Response

All errors return this format:

```json
{
  "timestamp": "2026-08-25T12:30:00",
  "status": 400,
  "error": "Insufficient Balance",
  "message": "Account ACC100001 has insufficient balance. Available: 5000.00, Requested: 10000.00",
  "path": "/api/accounts/ACC100001/withdraw"
}
```

Validation errors also include:

```json
{
  "validationErrors": {
    "email": "Please provide a valid email address",
    "firstName": "First name is required"
  }
}
```

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK — Success |
| 201 | Created — Resource created |
| 204 | No Content — Deleted successfully |
| 400 | Bad Request — Validation or business rule failure |
| 404 | Not Found — Resource doesn't exist |
| 409 | Conflict — Duplicate resource |
| 500 | Internal Server Error — Unexpected error |

---

## Customer APIs

### POST /api/customers
Creates a new customer.

**Request:**
```json
{
  "firstName": "Prasad",
  "lastName": "Mahajan",
  "email": "prasad@email.com",
  "phone": "9876543210",
  "address": "Pune, Maharashtra"
}
```

**Response 201:**
```json
{
  "id": 1,
  "customerCode": "CUST-0001",
  "firstName": "Prasad",
  "lastName": "Mahajan",
  "email": "prasad@email.com",
  "phone": "9876543210",
  "address": "Pune, Maharashtra",
  "createdAt": "2026-08-25T12:30:00"
}
```

**Errors:**
- `400` — Validation failure (empty name, invalid email, invalid phone)
- `409` — Email already registered

---

### GET /api/customers
Returns all customers.

**Response 200:** Array of customer objects.

---

### GET /api/customers/{id}
Returns a single customer.

**Response 200:** Customer object.  
**Error:** `404` — Customer not found.

---

### PUT /api/customers/{id}
Updates customer details.

**Request:** Same as POST.  
**Response 200:** Updated customer.  
**Errors:** `404`, `409` (if email changes to an existing one).

---

### DELETE /api/customers/{id}
Deletes a customer.

**Response:** `204 No Content`  
**Error:** `404`

---

## Account APIs

### POST /api/accounts
Creates a new bank account.

**Request:**
```json
{
  "customerId": 1,
  "accountType": "SAVINGS"
}
```
accountType: `SAVINGS` or `CURRENT`

**Response 201:**
```json
{
  "id": 1,
  "accountNumber": "ACC100001",
  "accountType": "SAVINGS",
  "balance": 0.00,
  "status": "ACTIVE",
  "customerId": 1,
  "customerCode": "CUST-0001",
  "customerName": "Prasad Mahajan",
  "createdAt": "2026-08-25T12:30:00"
}
```

---

### GET /api/accounts
Returns all accounts.

---

### GET /api/accounts/{id}
Returns account by database ID.

---

### GET /api/accounts/number/{accountNumber}
Returns account by account number (e.g., `ACC100001`).

---

### PUT /api/accounts/{id}/status
Updates account status.

**Request:**
```json
{ "status": "BLOCKED" }
```
status: `ACTIVE`, `BLOCKED`, `CLOSED`

---

## Transaction APIs

### POST /api/accounts/{accountNumber}/deposit
Deposits money into an account.

**Request:**
```json
{
  "amount": 50000,
  "description": "Salary credit"
}
```

**Response 200:**
```json
{
  "message": "Deposit successful",
  "accountNumber": "ACC100001",
  "amount": 50000,
  "newBalance": 50000.00,
  "transactionReference": "TXN100001123"
}
```

**Errors:**
- `400` — Amount must be positive
- `400` — Account is BLOCKED or CLOSED
- `404` — Account not found

---

### POST /api/accounts/{accountNumber}/withdraw
Withdraws money from an account.

**Request:**
```json
{
  "amount": 10000,
  "description": "ATM withdrawal"
}
```

**Response 200:**
```json
{
  "message": "Withdrawal successful",
  "accountNumber": "ACC100001",
  "amount": 10000,
  "newBalance": 40000.00,
  "transactionReference": "TXN100002456"
}
```

**Errors:**
- `400` — Insufficient balance
- `400` — Account not ACTIVE
- `404` — Account not found

---

### POST /api/transactions/transfer
Transfers money between two accounts (atomic operation).

**Request:**
```json
{
  "fromAccount": "ACC100001",
  "toAccount": "ACC100002",
  "amount": 15000,
  "description": "Rent payment"
}
```

**Response 200:**
```json
{
  "message": "Transfer successful",
  "fromAccount": "ACC100001",
  "toAccount": "ACC100002",
  "amount": 15000,
  "fromAccountNewBalance": 25000.00,
  "transactionReference": "TXN100003789"
}
```

**Errors:**
- `400` — Source and destination are same account
- `400` — Source account insufficient balance
- `400` — Either account not ACTIVE
- `404` — Either account not found

---

### GET /api/accounts/{accountNumber}/transactions
Returns transaction history, newest first.

**Optional Query Parameters:**

| Parameter | Values | Example |
|-----------|--------|---------|
| `transactionType` | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER` | `?transactionType=DEPOSIT` |
| `fromDate` | ISO DateTime | `?fromDate=2026-01-01T00:00:00` |
| `toDate` | ISO DateTime | `?toDate=2026-12-31T23:59:59` |

**Response 200:**
```json
[
  {
    "transactionReference": "TXN100001123",
    "transactionType": "DEPOSIT",
    "amount": 50000.00,
    "balanceAfterTransaction": 50000.00,
    "description": "Salary credit",
    "createdAt": "2026-08-25T12:30:00"
  }
]
```
