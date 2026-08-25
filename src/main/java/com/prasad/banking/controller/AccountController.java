package com.prasad.banking.controller;

import com.prasad.banking.dto.AccountRequest;
import com.prasad.banking.dto.AccountResponse;
import com.prasad.banking.dto.AccountStatusRequest;
import com.prasad.banking.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        log.info("POST /api/accounts — creating account for customer ID: {}",
                request.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        log.info("GET /api/accounts — fetching all accounts");
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        log.info("GET /api/accounts/{} — fetching account by ID", id);
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber) {

        log.info("GET /api/accounts/number/{} — fetching account by number", accountNumber);
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody AccountStatusRequest request) {

        log.info("PUT /api/accounts/{}/status — updating status to {}", id, request.getStatus());
        return ResponseEntity.ok(accountService.updateAccountStatus(id, request));
    }
}
