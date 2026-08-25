package com.prasad.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a transaction is attempted on a BLOCKED or CLOSED account.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String accountNumber, String status) {
        super(String.format(
                "Account %s is %s. Only ACTIVE accounts can perform transactions.",
                accountNumber, status
        ));
    }
}
