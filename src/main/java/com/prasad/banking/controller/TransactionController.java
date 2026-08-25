package com.prasad.banking.controller;

import com.prasad.banking.dto.*;
import com.prasad.banking.entity.TransactionType;
import com.prasad.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<TransactionOperationResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request) {

        log.info("POST /api/accounts/{}/deposit — amount: {}", accountNumber, request.getAmount());
        return ResponseEntity.ok(transactionService.deposit(accountNumber, request));
    }

    @PostMapping("/api/accounts/{accountNumber}/withdraw")
    public ResponseEntity<TransactionOperationResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequest request) {

        log.info("POST /api/accounts/{}/withdraw — amount: {}", accountNumber, request.getAmount());
        return ResponseEntity.ok(transactionService.withdraw(accountNumber, request));
    }

    @PostMapping("/api/transactions/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        log.info("POST /api/transactions/transfer — {} → {}, amount: {}",
                request.getFromAccount(), request.getToAccount(), request.getAmount());
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @GetMapping("/api/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        log.info("GET /api/accounts/{}/transactions — type: {}, from: {}, to: {}",
                accountNumber, transactionType, fromDate, toDate);

        return ResponseEntity.ok(transactionService.getTransactionHistory(
                accountNumber, transactionType, fromDate, toDate));
    }
}
