package com.prasad.banking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank customer.
 *
 * JPA maps this class to the 'customers' table in PostgreSQL.
 *
 * Relationships:
 *   One Customer → Many Accounts (OneToMany)
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", unique = true, nullable = false, length = 20)
    private String customerCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public Customer() {
    }

    public Customer(Long id, String customerCode, String firstName, String lastName, String email, String phone, String address, LocalDateTime createdAt, List<Account> accounts) {
        this.id = id;
        this.customerCode = customerCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        if (accounts != null) {
            this.accounts = accounts;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public static class CustomerBuilder {
        private Long id;
        private String customerCode;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private LocalDateTime createdAt;
        private List<Account> accounts = new ArrayList<>();

        public CustomerBuilder id(Long id) { this.id = id; return this; }
        public CustomerBuilder customerCode(String customerCode) { this.customerCode = customerCode; return this; }
        public CustomerBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerBuilder email(String email) { this.email = email; return this; }
        public CustomerBuilder phone(String phone) { this.phone = phone; return this; }
        public CustomerBuilder address(String address) { this.address = address; return this; }
        public CustomerBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CustomerBuilder accounts(List<Account> accounts) { this.accounts = accounts; return this; }

        public Customer build() {
            return new Customer(id, customerCode, firstName, lastName, email, phone, address, createdAt, accounts);
        }
    }
}
