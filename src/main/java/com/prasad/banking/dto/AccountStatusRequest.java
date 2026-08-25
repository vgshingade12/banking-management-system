package com.prasad.banking.dto;

import com.prasad.banking.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public class AccountStatusRequest {

    @NotNull(message = "Status is required (ACTIVE, BLOCKED, or CLOSED)")
    private AccountStatus status;

    public AccountStatusRequest() {
    }

    public AccountStatusRequest(AccountStatus status) {
        this.status = status;
    }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public static AccountStatusRequestBuilder builder() {
        return new AccountStatusRequestBuilder();
    }

    public static class AccountStatusRequestBuilder {
        private AccountStatus status;

        public AccountStatusRequestBuilder status(AccountStatus status) { this.status = status; return this; }

        public AccountStatusRequest build() {
            return new AccountStatusRequest(status);
        }
    }
}
