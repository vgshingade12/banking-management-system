package com.prasad.banking.repository;

import com.prasad.banking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity.
 *
 * Extends JpaRepository<Customer, Long>:
 *   - Customer → the entity type
 *   - Long     → the primary key type
 *
 * Spring Data JPA auto-generates the implementation at runtime.
 * We only define the method signatures — Spring figures out the SQL.
 *
 * Inherited methods (no code needed):
 *   save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by their email address.
     * Spring generates: SELECT * FROM customers WHERE email = ?
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find a customer by their unique customer code.
     * Spring generates: SELECT * FROM customers WHERE customer_code = ?
     */
    Optional<Customer> findByCustomerCode(String customerCode);

    /**
     * Check if any customer already has this email.
     * Spring generates: SELECT COUNT(*) > 0 FROM customers WHERE email = ?
     * Used for duplicate email validation before saving.
     */
    boolean existsByEmail(String email);

    /**
     * Check if any customer already has this customer code.
     */
    boolean existsByCustomerCode(String customerCode);
}
