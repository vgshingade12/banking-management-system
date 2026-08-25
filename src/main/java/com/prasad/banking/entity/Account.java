package com.prasad.banking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number"),
        @Index(name = "idx_account_customer_id", columnList = "customer_id")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    public Account() {
    }

    public Account(Long id, String accountNumber, AccountType accountType, BigDecimal balance, AccountStatus status, Customer customer, LocalDateTime createdAt, LocalDateTime updatedAt, List<Transaction> transactions) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.customer = customer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (transactions != null) {
            this.transactions = transactions;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private Long id;
        private String accountNumber;
        private AccountType accountType;
        private BigDecimal balance;
        private AccountStatus status;
        private Customer customer;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Transaction> transactions = new ArrayList<>();

        public AccountBuilder id(Long id) { this.id = id; return this; }
        public AccountBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public AccountBuilder accountType(AccountType accountType) { this.accountType = accountType; return this; }
        public AccountBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public AccountBuilder status(AccountStatus status) { this.status = status; return this; }
        public AccountBuilder customer(Customer customer) { this.customer = customer; return this; }
        public AccountBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public AccountBuilder transactions(List<Transaction> transactions) { this.transactions = transactions; return this; }

        public Account build() {
            return new Account(id, accountNumber, accountType, balance, status, customer, createdAt, updatedAt, transactions);
        }
    }
}
