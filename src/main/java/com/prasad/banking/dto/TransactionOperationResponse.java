package com.prasad.banking.dto;

import java.math.BigDecimal;

public class TransactionOperationResponse {

    private String message;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String transactionReference;

    public TransactionOperationResponse() {
    }

    public TransactionOperationResponse(String message, String accountNumber, BigDecimal amount, BigDecimal newBalance, String transactionReference) {
        this.message = message;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.newBalance = newBalance;
        this.transactionReference = transactionReference;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public static TransactionOperationResponseBuilder builder() {
        return new TransactionOperationResponseBuilder();
    }

    public static class TransactionOperationResponseBuilder {
        private String message;
        private String accountNumber;
        private BigDecimal amount;
        private BigDecimal newBalance;
        private String transactionReference;

        public TransactionOperationResponseBuilder message(String message) { this.message = message; return this; }
        public TransactionOperationResponseBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public TransactionOperationResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionOperationResponseBuilder newBalance(BigDecimal newBalance) { this.newBalance = newBalance; return this; }
        public TransactionOperationResponseBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }

        public TransactionOperationResponse build() {
            return new TransactionOperationResponse(message, accountNumber, amount, newBalance, transactionReference);
        }
    }
}
