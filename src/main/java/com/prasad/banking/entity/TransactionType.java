package com.prasad.banking.entity;

/**
 * Represents the type of financial transaction.
 *
 * DEPOSIT    — Money added to an account.
 * WITHDRAWAL — Money removed from an account.
 * TRANSFER   — Money moved from one account to another.
 *              Two transaction records are created per transfer:
 *              one DEBIT on the source, one CREDIT on the destination.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
}
