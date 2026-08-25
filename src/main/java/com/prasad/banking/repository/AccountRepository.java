package com.prasad.banking.repository;

import com.prasad.banking.entity.Account;
import com.prasad.banking.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity.
 *
 * Spring Data JPA auto-generates all SQL from method names.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find an account by its unique account number (e.g., "ACC100001").
     * This is the most frequently used query in the entire application.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if an account number already exists.
     * Used during account creation to ensure uniqueness.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Get all accounts belonging to a specific customer.
     * Spring generates: SELECT * FROM accounts WHERE customer_id = ?
     */
    List<Account> findByCustomerId(Long customerId);

    /**
     * Get all accounts with a specific status.
     * Useful for admin queries (e.g., list all BLOCKED accounts).
     */
    List<Account> findByStatus(AccountStatus status);
}
