package com.prasad.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested customer is not found in the database.
 *
 * @ResponseStatus(NOT_FOUND) — Spring automatically returns HTTP 404
 * when this exception is not caught, but we handle it in GlobalExceptionHandler
 * for a consistent response format.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
    }
}
