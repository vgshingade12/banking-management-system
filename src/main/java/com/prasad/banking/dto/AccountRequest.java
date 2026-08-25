package com.prasad.banking.dto;

import com.prasad.banking.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public class AccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required (SAVINGS or CURRENT)")
    private AccountType accountType;

    public AccountRequest() {
    }

    public AccountRequest(Long customerId, AccountType accountType) {
        this.customerId = customerId;
        this.accountType = accountType;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public static AccountRequestBuilder builder() {
        return new AccountRequestBuilder();
    }

    public static class AccountRequestBuilder {
        private Long customerId;
        private AccountType accountType;

        public AccountRequestBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public AccountRequestBuilder accountType(AccountType accountType) { this.accountType = accountType; return this; }

        public AccountRequest build() {
            return new AccountRequest(customerId, accountType);
        }
    }
}
