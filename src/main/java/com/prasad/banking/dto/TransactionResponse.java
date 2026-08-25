package com.prasad.banking.dto;

import com.prasad.banking.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String description;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(String transactionReference, TransactionType transactionType, BigDecimal amount, BigDecimal balanceAfterTransaction, String description, LocalDateTime createdAt) {
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.description = description;
        this.createdAt = createdAt;
    }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TransactionResponseBuilder builder() {
        return new TransactionResponseBuilder();
    }

    public static class TransactionResponseBuilder {
        private String transactionReference;
        private TransactionType transactionType;
        private BigDecimal amount;
        private BigDecimal balanceAfterTransaction;
        private String description;
        private LocalDateTime createdAt;

        public TransactionResponseBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }
        public TransactionResponseBuilder transactionType(TransactionType transactionType) { this.transactionType = transactionType; return this; }
        public TransactionResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionResponseBuilder balanceAfterTransaction(BigDecimal balanceAfterTransaction) { this.balanceAfterTransaction = balanceAfterTransaction; return this; }
        public TransactionResponseBuilder description(String description) { this.description = description; return this; }
        public TransactionResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TransactionResponse build() {
            return new TransactionResponse(transactionReference, transactionType, amount, balanceAfterTransaction, description, createdAt);
        }
    }
}
