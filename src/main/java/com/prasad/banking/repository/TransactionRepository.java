package com.prasad.banking.repository;

import com.prasad.banking.entity.Account;
import com.prasad.banking.entity.Transaction;
import com.prasad.banking.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 *
 * Includes custom JPQL queries for advanced filtering
 * (e.g., filter by type and date range).
 *
 * JPQL (Java Persistence Query Language) is similar to SQL but
 * operates on entity class names and field names — not table/column names.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Get all transactions for a specific account, sorted newest first.
     * Spring generates: SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC
     */
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);

    /**
     * Get all transactions of a specific type for an account, sorted newest first.
     * Used when the user filters by transactionType.
     */
    List<Transaction> findByAccountAndTransactionTypeOrderByCreatedAtDesc(
            Account account, TransactionType transactionType);

    /**
     * Custom JPQL query to filter by account AND optional type AND date range.
     *
     * :account        → the Account object (JPA matches on account_id FK)
     * :type           → TransactionType enum (or null to skip type filter)
     * :fromDate       → start of date range (or null to skip)
     * :toDate         → end of date range (or null to skip)
     *
     * The (:type IS NULL OR ...) pattern allows optional parameters.
     * If :type is null, that condition is always true (ignored).
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account = :account
              AND (:type IS NULL OR t.transactionType = :type)
              AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
              AND (:toDate IS NULL OR t.createdAt <= :toDate)
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findTransactionsWithFilters(
            @Param("account") Account account,
            @Param("type") TransactionType type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Find a transaction by its unique reference number.
     */
    Optional<Transaction> findByTransactionReference(String transactionReference);

    /**
     * Count the total number of transactions for auto-generating reference numbers.
     * We use count() + offset to generate TXN100001, TXN100002, etc.
     */
    long countByAccount(Account account);
}
