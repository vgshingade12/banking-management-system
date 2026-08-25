package com.prasad.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a withdrawal or transfer is attempted but the account
 * does not have enough funds.
 *
 * This is a 400 Bad Request — the request itself is malformed
 * (business rule violation), not a server error.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String accountNumber, java.math.BigDecimal available, java.math.BigDecimal requested) {
        super(String.format(
                "Account %s has insufficient balance. Available: %.2f, Requested: %.2f",
                accountNumber, available, requested
        ));
    }
}
