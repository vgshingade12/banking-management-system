package com.prasad.banking.config;

import com.prasad.banking.dto.AccountRequest;
import com.prasad.banking.dto.CustomerRequest;
import com.prasad.banking.dto.DepositRequest;
import com.prasad.banking.dto.TransferRequest;
import com.prasad.banking.entity.AccountType;
import com.prasad.banking.service.AccountService;
import com.prasad.banking.service.CustomerService;
import com.prasad.banking.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public DataLoader(CustomerService customerService,
                      AccountService accountService,
                      TransactionService transactionService) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void run(String... args) {
        try {
            if (customerService.getAllCustomers().isEmpty()) {
                log.info("Seeding initial sample data for Banking Management System...");

                var c1 = customerService.createCustomer(
                        new CustomerRequest("Prasad", "Mahajan", "prasad@example.com", "9876543210", "123 Tech Park, Pune"));
                var c2 = customerService.createCustomer(
                        new CustomerRequest("Vaibhavi", "Shingade", "vaibhavi@example.com", "9123456789", "456 Developer Way, Mumbai"));

                var acc1 = accountService.createAccount(new AccountRequest(c1.getId(), AccountType.SAVINGS));
                var acc2 = accountService.createAccount(new AccountRequest(c2.getId(), AccountType.CURRENT));

                transactionService.deposit(acc1.getAccountNumber(), new DepositRequest(new BigDecimal("50000.00"), "Initial Account Opening Deposit"));
                transactionService.deposit(acc2.getAccountNumber(), new DepositRequest(new BigDecimal("125000.00"), "Business Operating Capital"));

                transactionService.transfer(new TransferRequest(acc2.getAccountNumber(), acc1.getAccountNumber(), new BigDecimal("15000.00"), "Vendor Invoice Settlement"));

                log.info("Sample data seeding completed successfully! Created 2 customers, 2 accounts, and initial transactions.");
            }
        } catch (Exception e) {
            log.warn("DataLoader execution skipped or already seeded: {}", e.getMessage());
        }
    }
}
