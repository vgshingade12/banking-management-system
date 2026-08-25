package com.prasad.banking.dto;

import com.prasad.banking.entity.AccountStatus;
import com.prasad.banking.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;

    private Long customerId;
    private String customerCode;
    private String customerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String accountNumber, AccountType accountType, BigDecimal balance, AccountStatus status, Long customerId, String customerCode, String customerName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.customerId = customerId;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static AccountResponseBuilder builder() {
        return new AccountResponseBuilder();
    }

    public static class AccountResponseBuilder {
        private Long id;
        private String accountNumber;
        private AccountType accountType;
        private BigDecimal balance;
        private AccountStatus status;
        private Long customerId;
        private String customerCode;
        private String customerName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AccountResponseBuilder id(Long id) { this.id = id; return this; }
        public AccountResponseBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public AccountResponseBuilder accountType(AccountType accountType) { this.accountType = accountType; return this; }
        public AccountResponseBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public AccountResponseBuilder status(AccountStatus status) { this.status = status; return this; }
        public AccountResponseBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public AccountResponseBuilder customerCode(String customerCode) { this.customerCode = customerCode; return this; }
        public AccountResponseBuilder customerName(String customerName) { this.customerName = customerName; return this; }
        public AccountResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AccountResponse build() {
            return new AccountResponse(id, accountNumber, accountType, balance, status, customerId, customerCode, customerName, createdAt, updatedAt);
        }
    }
}
