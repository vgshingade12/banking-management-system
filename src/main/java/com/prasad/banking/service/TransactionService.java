package com.prasad.banking.service;

import com.prasad.banking.dto.*;
import com.prasad.banking.entity.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionService interface — defines deposit, withdrawal, transfer,
 * and transaction history operations.
 */
public interface TransactionService {

    TransactionOperationResponse deposit(String accountNumber, DepositRequest request);

    TransactionOperationResponse withdraw(String accountNumber, WithdrawalRequest request);

    TransferResponse transfer(TransferRequest request);

    List<TransactionResponse> getTransactionHistory(
            String accountNumber,
            TransactionType type,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
}
