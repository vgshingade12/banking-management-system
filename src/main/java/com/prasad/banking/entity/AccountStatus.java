package com.prasad.banking.entity;

/**
 * Represents the lifecycle status of a bank account.
 *
 * ACTIVE  — Account is fully operational. Deposits, withdrawals, and transfers allowed.
 * BLOCKED — Account is temporarily frozen. No transactions allowed. Can be reactivated.
 * CLOSED  — Account is permanently closed. No transactions allowed. Cannot be reopened.
 */
public enum AccountStatus {
    ACTIVE,
    BLOCKED,
    CLOSED
}
