package com.prasad.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a transaction is invalid due to business rule violation.
 * Examples:
 *   - Transfer to the same account
 *   - Zero or negative amount (caught by validation first, but also here as defense)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
