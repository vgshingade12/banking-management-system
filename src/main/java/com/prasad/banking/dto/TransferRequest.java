package com.prasad.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccount;

    @NotBlank(message = "Destination account number is required")
    private String toAccount;

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    private String description;

    public TransferRequest() {
    }

    public TransferRequest(String fromAccount, String toAccount, BigDecimal amount, String description) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.description = description;
    }

    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }

    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static TransferRequestBuilder builder() {
        return new TransferRequestBuilder();
    }

    public static class TransferRequestBuilder {
        private String fromAccount;
        private String toAccount;
        private BigDecimal amount;
        private String description;

        public TransferRequestBuilder fromAccount(String fromAccount) { this.fromAccount = fromAccount; return this; }
        public TransferRequestBuilder toAccount(String toAccount) { this.toAccount = toAccount; return this; }
        public TransferRequestBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransferRequestBuilder description(String description) { this.description = description; return this; }

        public TransferRequest build() {
            return new TransferRequest(fromAccount, toAccount, amount, description);
        }
    }
}
