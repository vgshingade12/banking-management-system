package com.prasad.banking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_account_id", columnList = "account_id"),
        @Index(name = "idx_transaction_reference", columnList = "transaction_reference"),
        @Index(name = "idx_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_transaction_created_at", columnList = "created_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", unique = true, nullable = false, length = 20)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after_transaction", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(Long id, String transactionReference, TransactionType transactionType, BigDecimal amount, BigDecimal balanceAfterTransaction, String description, Account account, LocalDateTime createdAt) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.description = description;
        this.account = account;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfterTransaction() { return balanceAfterTransaction; }
    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) { this.balanceAfterTransaction = balanceAfterTransaction; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public static class TransactionBuilder {
        private Long id;
        private String transactionReference;
        private TransactionType transactionType;
        private BigDecimal amount;
        private BigDecimal balanceAfterTransaction;
        private String description;
        private Account account;
        private LocalDateTime createdAt;

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }
        public TransactionBuilder transactionType(TransactionType transactionType) { this.transactionType = transactionType; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder balanceAfterTransaction(BigDecimal balanceAfterTransaction) { this.balanceAfterTransaction = balanceAfterTransaction; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder account(Account account) { this.account = account; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transaction build() {
            return new Transaction(id, transactionReference, transactionType, amount, balanceAfterTransaction, description, account, createdAt);
        }
    }
}
