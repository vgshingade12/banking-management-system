package com.prasad.banking.service.impl;

import com.prasad.banking.dto.*;
import com.prasad.banking.entity.*;
import com.prasad.banking.exception.*;
import com.prasad.banking.repository.AccountRepository;
import com.prasad.banking.repository.TransactionRepository;
import com.prasad.banking.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Value("${banking.transaction.reference-prefix:TXN}")
    private String transactionReferencePrefix;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionOperationResponse deposit(String accountNumber, DepositRequest request) {
        log.info("Processing deposit of {} to account {}", request.getAmount(), accountNumber);

        Account account = getActiveAccount(accountNumber);

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        accountRepository.save(account);

        String reference = generateTransactionReference();
        String description = request.getDescription() != null
                ? request.getDescription() : "Cash deposit";
        Transaction transaction = buildTransaction(reference, TransactionType.DEPOSIT,
                request.getAmount(), newBalance, description, account);
        transactionRepository.save(transaction);

        log.info("Deposit successful. Account: {}, New Balance: {}", accountNumber, newBalance);

        return TransactionOperationResponse.builder()
                .message("Deposit successful")
                .accountNumber(accountNumber)
                .amount(request.getAmount())
                .newBalance(newBalance)
                .transactionReference(reference)
                .build();
    }

    @Override
    @Transactional
    public TransactionOperationResponse withdraw(String accountNumber, WithdrawalRequest request) {
        log.info("Processing withdrawal of {} from account {}", request.getAmount(), accountNumber);

        Account account = getActiveAccount(accountNumber);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(accountNumber,
                    account.getBalance(), request.getAmount());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);

        accountRepository.save(account);

        String reference = generateTransactionReference();
        String description = request.getDescription() != null
                ? request.getDescription() : "Cash withdrawal";
        Transaction transaction = buildTransaction(reference, TransactionType.WITHDRAWAL,
                request.getAmount(), newBalance, description, account);
        transactionRepository.save(transaction);

        log.info("Withdrawal successful. Account: {}, New Balance: {}", accountNumber, newBalance);

        return TransactionOperationResponse.builder()
                .message("Withdrawal successful")
                .accountNumber(accountNumber)
                .amount(request.getAmount())
                .newBalance(newBalance)
                .transactionReference(reference)
                .build();
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        log.info("Processing transfer of {} from {} to {}",
                request.getAmount(), request.getFromAccount(), request.getToAccount());

        if (request.getFromAccount().equalsIgnoreCase(request.getToAccount())) {
            throw new InvalidTransactionException(
                    "Source and destination account cannot be the same.");
        }

        Account sourceAccount = getActiveAccount(request.getFromAccount());

        Account destinationAccount = getActiveAccount(request.getToAccount());

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getFromAccount(),
                    sourceAccount.getBalance(), request.getAmount());
        }

        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(sourceNewBalance);

        BigDecimal destinationNewBalance = destinationAccount.getBalance().add(request.getAmount());
        destinationAccount.setBalance(destinationNewBalance);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        String transferReference = generateTransactionReference();
        String description = request.getDescription() != null
                ? request.getDescription() : "Fund transfer";

        Transaction debitTxn = buildTransaction(
                transferReference,
                TransactionType.TRANSFER,
                request.getAmount(),
                sourceNewBalance,
                description + " → " + request.getToAccount(),
                sourceAccount
        );

        Transaction creditTxn = buildTransaction(
                generateTransactionReference(),
                TransactionType.TRANSFER,
                request.getAmount(),
                destinationNewBalance,
                description + " ← " + request.getFromAccount(),
                destinationAccount
        );

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);

        log.info("Transfer successful. {} → {}, Amount: {}, Ref: {}",
                request.getFromAccount(), request.getToAccount(),
                request.getAmount(), transferReference);

        return TransferResponse.builder()
                .message("Transfer successful")
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .fromAccountNewBalance(sourceNewBalance)
                .transactionReference(transferReference)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(
            String accountNumber,
            TransactionType type,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        log.info("Fetching transaction history for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with number: " + accountNumber));

        List<Transaction> transactions = transactionRepository.findTransactionsWithFilters(
                account, type, fromDate, toDate);

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private Account getActiveAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with number: " + accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(accountNumber, account.getStatus().name());
        }

        return account;
    }

    private Transaction buildTransaction(String reference, TransactionType type,
                                         BigDecimal amount, BigDecimal balanceAfter,
                                         String description, Account account) {
        return Transaction.builder()
                .transactionReference(reference)
                .transactionType(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfter)
                .description(description)
                .account(account)
                .build();
    }

    private String generateTransactionReference() {
        long count = transactionRepository.count() + 1;
        return transactionReferencePrefix + (100000 + count) + System.currentTimeMillis() % 1000;
    }

    private TransactionResponse mapToTransactionResponse(Transaction txn) {
        return TransactionResponse.builder()
                .transactionReference(txn.getTransactionReference())
                .transactionType(txn.getTransactionType())
                .amount(txn.getAmount())
                .balanceAfterTransaction(txn.getBalanceAfterTransaction())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
