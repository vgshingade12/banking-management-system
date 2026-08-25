# Interview Preparation Guide
## Banking Management System — Vaibhavi Shingade

This guide prepares you to confidently discuss your project in interviews at Finzly and similar fintech companies.

---

## How to Introduce the Project (30-second pitch)

> "I built a Banking Management System using Java Spring Boot and PostgreSQL as my major project.
> It includes customer management, savings and current accounts, deposits, withdrawals, and money transfers.
> The most important feature is the atomic money transfer — using Spring's @Transactional annotation to ensure
> both accounts are updated together or not at all, which prevents money loss in case of failures.
> I also wrote unit tests using JUnit and Mockito to verify all business rules."

---

## Java Questions

### Q: What OOP concepts did you use in this project?

**A:**
- **Encapsulation** — Entity fields are private; accessed through getters/setters (Lombok). DTOs hide internal entity structure from API callers.
- **Abstraction** — Service interfaces (CustomerService, AccountService, TransactionService) define WHAT can be done, not HOW. Controllers only know the interface.
- **Inheritance** — All custom exceptions extend RuntimeException, inheriting exception behavior.
- **Polymorphism** — Spring injects CustomerServiceImpl wherever CustomerService is expected. The controller doesn't know or care which implementation it's using.

---

### Q: Why did you use interfaces for the service layer?

**A:**
Three reasons:
1. **Testability** — In unit tests, I use Mockito to mock the interface. I don't need a real database.
2. **Loose coupling** — The controller depends on the abstraction (interface), not the implementation. This follows the SOLID Dependency Inversion Principle.
3. **Flexibility** — If I later want a different implementation (e.g., add caching), I just create a new class implementing the same interface without changing the controller.

---

### Q: Why did you use BigDecimal instead of double for money?

**A:**
Because double and float use binary floating-point representation, which cannot precisely represent many decimal fractions.

For example:
```java
double result = 0.1 + 0.2;
System.out.println(result); // Prints 0.30000000000000004 ← WRONG!
```

In banking, even a fraction of a rupee error multiplied across millions of transactions causes serious problems.

BigDecimal stores numbers as exact decimal values:
```java
BigDecimal result = new BigDecimal("0.1").add(new BigDecimal("0.2"));
System.out.println(result); // Prints 0.3 ← CORRECT
```

Also, when comparing BigDecimal values, I use `compareTo()` not `equals()`:
- `equals()` considers 5000 and 5000.00 as different (because of scale)
- `compareTo()` correctly treats them as equal

---

## Spring Boot Questions

### Q: Why did you choose Spring Boot?

**A:**
Spring Boot removes the need to manually configure everything from scratch. It:
- Auto-configures the database connection, JPA, and web server
- Embeds Tomcat — no need to deploy a WAR file
- Uses convention over configuration — sensible defaults, you only override what you need
- Has a huge ecosystem — validation, security, JPA all work together seamlessly

---

### Q: What is Dependency Injection? How did you use it?

**A:**
Dependency Injection (DI) means an object receives its dependencies from outside rather than creating them itself.

In my project, I use constructor injection via Lombok's `@RequiredArgsConstructor`:

```java
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository; // injected by Spring
```

Spring's IoC container creates the `CustomerRepository` bean and injects it into `CustomerServiceImpl`. I never write `new CustomerRepository()`.

Benefits: testable, loosely coupled, easy to swap implementations.

---

### Q: What is @Service, @Repository, @RestController?

**A:**
All three are specializations of `@Component`, which marks a class as a Spring-managed bean.

| Annotation | Layer | Extra behavior |
|-----------|-------|---------------|
| `@RestController` | Controller | Adds `@ResponseBody` — auto-serializes return values to JSON |
| `@Service` | Service | Marks business logic layer; used for transaction management |
| `@Repository` | Repository | Marks DB layer; translates SQL exceptions to Spring's DataAccessException |

Using the right annotation makes the code self-documenting and enables layer-specific features.

---

### Q: What is @Transactional? Why is it critical?

**A:**
`@Transactional` wraps a method in a database transaction. If the method completes successfully, the transaction is **committed** (changes are permanent). If any exception is thrown, the transaction is **rolled back** (all changes are undone).

In my transfer method:
```java
@Transactional
public TransferResponse transfer(TransferRequest request) {
    // Deduct from source account
    // Add to destination account
    // Save both accounts
    // Create transaction records
    // If DB crashes anywhere above → ALL changes roll back
}
```

Without `@Transactional`: if the app crashes after deducting from source but before adding to destination, money disappears. This is called a **partial update** — catastrophic in banking.

With `@Transactional`: either both accounts update, or neither does. This is **Atomicity** — the 'A' in ACID.

---

## JPA / Hibernate Questions

### Q: What is JPA? What is Hibernate?

**A:**
- **JPA (Java Persistence API)** is a Java standard (specification) for mapping Java objects to database tables. It defines annotations like `@Entity`, `@Table`, `@Column`, `@OneToMany`.
- **Hibernate** is the most popular JPA implementation. It takes your Java objects, generates SQL, and executes it against the database.

Think of it like: JPA is the interface, Hibernate is the implementation — same relationship as CustomerService and CustomerServiceImpl.

In my project: I write `@Entity` and `@OneToMany` (JPA annotations), and Hibernate generates `CREATE TABLE`, `INSERT INTO`, `SELECT` SQL automatically.

---

### Q: Explain @OneToMany and @ManyToOne

**A:**
In my project:
- One Customer can have Many Accounts → `@OneToMany` on Customer
- Many Accounts belong to One Customer → `@ManyToOne` on Account

```java
// In Customer entity:
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Account> accounts;

// In Account entity:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;
```

The `@JoinColumn` tells Hibernate that the `accounts` table has a `customer_id` foreign key column pointing to `customers.id`.

---

### Q: What is lazy loading?

**A:**
By default, JPA uses lazy loading for relationships (`fetch = FetchType.LAZY`). This means related entities are NOT loaded from the database until you actually access them.

Example: When I load a Customer, their list of Accounts is NOT fetched from the DB immediately. Only when I call `customer.getAccounts()` does Hibernate run a SELECT query for accounts.

Why? Performance. If I only need the customer's email, I don't want to load all their accounts unnecessarily.

In my project, I use lazy loading everywhere, and I map to DTOs inside a `@Transactional` method (where the Hibernate session is still open) to avoid `LazyInitializationException`.

---

### Q: What is the difference between @Column(name = "account_type") with EnumType.ORDINAL vs STRING?

**A:**
- **ORDINAL** stores the enum as an integer (0, 1, 2...). If you add a new enum value in the middle, all existing data becomes wrong.
- **STRING** stores the enum as text ("SAVINGS", "CURRENT"). This is readable in the database and safe to change.

I always use `EnumType.STRING` for this reason.

---

## REST API Questions

### Q: What is REST?

**A:**
REST (Representational State Transfer) is an architectural style for designing web APIs. Key principles:

1. **Stateless** — Each request contains all information needed; server stores no client session state.
2. **Resource-based URLs** — URLs identify resources (`/api/customers/1`, not `/getCustomerById?id=1`).
3. **HTTP verbs** — Use GET for read, POST for create, PUT for update, DELETE for delete.
4. **Uniform interface** — Consistent request/response format (JSON in my case).

---

### Q: Difference between GET, POST, PUT, DELETE?

**A:**

| Verb | Purpose | Body? | Idempotent? |
|------|---------|-------|-------------|
| GET | Read resource | No | Yes |
| POST | Create resource | Yes | No |
| PUT | Update resource | Yes | Yes |
| DELETE | Delete resource | No | Yes |

**Idempotent** means calling the same request multiple times gives the same result. GET /api/customers/1 always returns the same customer. DELETE /api/customers/1 deletes once; subsequent calls return 404.

POST is NOT idempotent — calling POST /api/customers twice creates two customers.

---

### Q: Why did you use HTTP status codes?

**A:**
Status codes tell the client what happened without them having to parse the response body. Standard codes make APIs predictable and easy to integrate.

- `201 Created` after creating a customer — client knows to update its UI
- `404 Not Found` when account doesn't exist — client knows to show "Account not found" message
- `400 Bad Request` on insufficient balance — client knows it's a user error, not a server issue
- `409 Conflict` on duplicate email — client knows to ask user for a different email

---

## Database Questions

### Q: Why PostgreSQL?

**A:**
- **ACID compliant** — Critical for financial data. Transactions are reliable.
- **Open source and production-ready** — Used by major fintech companies.
- **Excellent JPA support** — Works seamlessly with Hibernate.
- **DECIMAL type** — Stores exact decimal numbers, unlike SQLite's limited numeric support.
- **Strong community and documentation**

---

### Q: What is a primary key? What is a foreign key?

**A:**
- **Primary Key**: A column (or combination) that uniquely identifies each row. In my tables, `id` is the PK — auto-generated by PostgreSQL. No two rows can have the same id.
- **Foreign Key**: A column that references the primary key of another table. In `accounts`, the `customer_id` column is a FK that references `customers.id`. This enforces referential integrity — you cannot create an account for a non-existent customer.

---

### Q: What indexes did you create?

**A:**
I created database indexes on:
- `accounts.account_number` — Most frequent lookup in the entire app (every deposit/withdrawal/transfer queries by account number)
- `transactions.account_id` — Transaction history is always queried by account
- `transactions.transaction_reference` — Customer support queries by reference
- `transactions.created_at` — Date range filtering on transaction history

Indexes work like a book's index — instead of scanning every row, the DB jumps directly to matching rows. Without the index on `account_number`, every deposit query would scan every row in the accounts table.

---

## Banking Logic Questions

### Q: How does deposit work?

**A:**
1. Client sends `POST /api/accounts/ACC100001/deposit` with amount
2. Controller receives request, validates via `@Valid`
3. Service finds the account by account number
4. Verifies account status is ACTIVE (throws exception if BLOCKED/CLOSED)
5. Adds amount to balance using `BigDecimal.add()` (exact arithmetic)
6. Saves updated account to database
7. Creates a Transaction record for audit trail
8. Returns success response with new balance

---

### Q: How did you prevent negative balance?

**A:**
In the withdrawal method, before subtracting:

```java
// Compare current balance to requested amount
if (account.getBalance().compareTo(request.getAmount()) < 0) {
    // compareTo returns negative if balance < amount
    throw new InsufficientBalanceException(accountNumber,
            account.getBalance(), request.getAmount());
}
// Only if we reach here, balance is sufficient
BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
```

`compareTo()` returns:
- Negative (-1) if balance < amount → throw exception
- Zero (0) if balance == amount → proceed (balance becomes 0)
- Positive (+1) if balance > amount → proceed

---

### Q: How does money transfer work? What happens if it fails halfway?

**A:**
The transfer method is annotated with `@Transactional`. Here's the sequence:

```
1. Validate source ≠ destination
2. Find source account       ─┐
3. Find destination account   │
4. Check sufficient balance   │  All inside ONE database transaction
5. Deduct from source         │
6. Add to destination         │
7. Save source account        │
8. Save destination account   │
9. Create audit records      ─┘
```

**If it fails halfway** — say the server crashes after step 7 (source saved) but before step 8 (destination NOT saved):

Spring sees the exception and triggers a **rollback**. PostgreSQL undoes ALL changes made since the transaction started. Both accounts return to their original state. No money is lost, no partial update occurs.

This is guaranteed by Spring's transaction management + PostgreSQL's ACID properties.

---

## Project Architecture Questions

### Q: Explain your project architecture

**A:**
I followed a **three-layer architecture**:

1. **Controller Layer** — Entry point for HTTP requests. Receives JSON, validates input with `@Valid`, calls the service, and returns HTTP responses. Contains NO business logic.

2. **Service Layer** — Contains ALL business logic. Validates business rules (sufficient balance, account status, etc.), manages `@Transactional` boundaries, and coordinates between repositories.

3. **Repository Layer** — Interfaces extending JpaRepository. Spring Data JPA auto-generates SQL from method names. Custom `@Query` for complex filtering.

**Why this separation?**
- Easy to test each layer independently
- Business rules don't spread into controllers
- Controllers don't need to know about SQL

---

### Q: Explain the complete request flow

**A:**
Example: `POST /api/accounts/ACC100001/deposit` with amount 5000

```
1. HTTP request arrives at port 8080
2. Spring DispatcherServlet routes to TransactionController.deposit()
3. @Valid runs Jakarta Bean Validation on DepositRequest
   → If amount <= 0, throws MethodArgumentNotValidException → 400 response
4. Controller calls transactionService.deposit("ACC100001", request)
5. TransactionServiceImpl.deposit():
   a. Calls accountRepository.findByAccountNumber("ACC100001")
      → Hibernate runs: SELECT * FROM accounts WHERE account_number = 'ACC100001'
   b. Checks account status == ACTIVE
   c. Calculates: newBalance = 10000.00 + 5000.00 = 15000.00
   d. Sets account.setBalance(15000.00)
   e. Calls accountRepository.save(account)
      → Hibernate runs: UPDATE accounts SET balance = 15000.00 WHERE id = 1
   f. Creates Transaction entity, saves it
      → Hibernate runs: INSERT INTO transactions (...)
6. Returns TransactionOperationResponse to controller
7. Controller returns ResponseEntity with 200 OK
8. Spring serializes response to JSON and sends back to client
```

---

### Q: What was the most difficult part?

**A:**
The most challenging part was the **money transfer with @Transactional**. I had to understand:

1. How Spring's transaction proxy works (AOP-based — Spring wraps the service in a proxy that manages the transaction boundary)
2. Why `@Transactional` must be on a public method called from outside the class (self-invocation bypasses the proxy)
3. How to ensure both Transaction records are created with meaningful descriptions to differentiate the debit side from the credit side
4. Using `compareTo()` correctly for BigDecimal comparison — a subtle but critical bug if you use `equals()` instead

---

### Q: How would you secure this application?

**A:**
I'd add Spring Security with JWT (JSON Web Tokens):

1. Add Spring Security + JJWT dependency
2. Create `AuthController` with `/auth/register` and `/auth/login` endpoints
3. Add a `User` entity with username, password (BCrypt hashed), and role
4. Create `JwtAuthFilter` — intercepts every request, extracts JWT, validates it
5. Configure `SecurityFilterChain` to require authentication on all `/api/**` endpoints
6. Add `@PreAuthorize("hasRole('ADMIN')")` on sensitive endpoints (block account, delete customer)

I've already added `// TODO` comments in the controllers marking exactly where these would be added.

---

### Q: How would you scale this application?

**A:**
For a production system at scale:

1. **Database connection pooling** — Use HikariCP (Spring Boot includes it) with tuned pool size
2. **Caching** — Redis cache for frequently-read account data to reduce DB load
3. **Pagination** — Return paginated transaction history instead of all records
4. **Read replicas** — PostgreSQL read replica for GET endpoints to offload the primary DB
5. **Microservices** — Split into Customer Service, Account Service, Transaction Service with message queues (Kafka) for async communication
6. **Horizontal scaling** — Multiple app instances behind a load balancer (Spring Session for shared state)
7. **Monitoring** — Actuator + Prometheus + Grafana for metrics

For the campus placement context, the current monolithic design is appropriate and demonstrates clean architecture that can be extended.

---

## Resume Bullet Points

Based on features actually implemented:

```
• Designed and developed a Banking Management System REST API using Java 17, Spring Boot 3.x,
  and PostgreSQL implementing customer management, multi-account support, and core banking
  operations (deposit, withdrawal, fund transfer).

• Implemented atomic money transfer using Spring @Transactional annotation ensuring ACID
  compliance — prevents partial updates and guarantees data consistency on transaction failure.

• Built a three-layer architecture (Controller → Service → Repository) using Spring Data JPA/
  Hibernate with custom JPQL queries for filtered transaction history retrieval.

• Applied Jakarta Bean Validation and a @RestControllerAdvice GlobalExceptionHandler to return
  consistent JSON error responses for 15+ custom exception scenarios.

• Used BigDecimal for all financial calculations to eliminate floating-point precision errors,
  a critical requirement in financial systems.

• Wrote unit tests using JUnit 5 and Mockito covering 20+ test cases including insufficient
  balance rejection, blocked account transactions, and transfer rollback verification.

• Created comprehensive API documentation and a Postman collection covering 18 endpoints
  for complete end-to-end testing.
```

---

## Quick Reference — Key Classes

| Class | Purpose | Interview talking point |
|-------|---------|------------------------|
| `TransactionServiceImpl` | Core banking logic | @Transactional transfer, BigDecimal arithmetic |
| `GlobalExceptionHandler` | Error handling | @RestControllerAdvice, consistent error format |
| `Account` | Account entity | @Enumerated(STRING), BigDecimal column, indexes |
| `TransactionRepository` | DB queries | JPQL @Query with optional parameters |
| `CustomerServiceImpl` | CRUD logic | DRY principle, entity-to-DTO mapping |
| `TransactionServiceTest` | Unit tests | Mockito, Arrange-Act-Assert pattern |

---

*Good luck at your Finzly interview, Vaibhavi! You built this — you can explain every line of it.*
