package com.prasad.banking.service.impl;

import com.prasad.banking.dto.AccountRequest;
import com.prasad.banking.dto.AccountResponse;
import com.prasad.banking.dto.AccountStatusRequest;
import com.prasad.banking.entity.Account;
import com.prasad.banking.entity.AccountStatus;
import com.prasad.banking.entity.Customer;
import com.prasad.banking.exception.AccountNotFoundException;
import com.prasad.banking.exception.CustomerNotFoundException;
import com.prasad.banking.repository.AccountRepository;
import com.prasad.banking.repository.CustomerRepository;
import com.prasad.banking.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Value("${banking.account.initial-balance:0.00}")
    private BigDecimal initialBalance;

    @Value("${banking.account.number-prefix:ACC}")
    private String accountNumberPrefix;

    public AccountServiceImpl(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating {} account for customer ID: {}",
                request.getAccountType(), request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account {} created for customer {}", accountNumber, customer.getCustomerCode());

        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.info("Fetching account with ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Fetching account with number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with number: " + accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(Long id, AccountStatusRequest request) {
        log.info("Updating account {} status to {}", id, request.getStatus());

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setStatus(request.getStatus());
        Account updated = accountRepository.save(account);
        log.info("Account {} status updated to {}", updated.getAccountNumber(), updated.getStatus());

        return mapToResponse(updated);
    }

    private String generateAccountNumber() {
        long count = accountRepository.count();
        String accountNumber;

        do {
            count++;
            accountNumber = accountNumberPrefix + (100000 + count);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public AccountResponse mapToResponse(Account account) {
        Customer customer = account.getCustomer();
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
