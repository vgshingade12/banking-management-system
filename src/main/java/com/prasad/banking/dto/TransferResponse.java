package com.prasad.banking.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private String message;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private BigDecimal fromAccountNewBalance;
    private String transactionReference;

    public TransferResponse() {
    }

    public TransferResponse(String message, String fromAccount, String toAccount, BigDecimal amount, BigDecimal fromAccountNewBalance, String transactionReference) {
        this.message = message;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.fromAccountNewBalance = fromAccountNewBalance;
        this.transactionReference = transactionReference;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }

    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFromAccountNewBalance() { return fromAccountNewBalance; }
    public void setFromAccountNewBalance(BigDecimal fromAccountNewBalance) { this.fromAccountNewBalance = fromAccountNewBalance; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public static TransferResponseBuilder builder() {
        return new TransferResponseBuilder();
    }

    public static class TransferResponseBuilder {
        private String message;
        private String fromAccount;
        private String toAccount;
        private BigDecimal amount;
        private BigDecimal fromAccountNewBalance;
        private String transactionReference;

        public TransferResponseBuilder message(String message) { this.message = message; return this; }
        public TransferResponseBuilder fromAccount(String fromAccount) { this.fromAccount = fromAccount; return this; }
        public TransferResponseBuilder toAccount(String toAccount) { this.toAccount = toAccount; return this; }
        public TransferResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransferResponseBuilder fromAccountNewBalance(BigDecimal fromAccountNewBalance) { this.fromAccountNewBalance = fromAccountNewBalance; return this; }
        public TransferResponseBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }

        public TransferResponse build() {
            return new TransferResponse(message, fromAccount, toAccount, amount, fromAccountNewBalance, transactionReference);
        }
    }
}
