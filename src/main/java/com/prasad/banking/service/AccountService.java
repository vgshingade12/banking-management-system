package com.prasad.banking.service;

import com.prasad.banking.dto.AccountRequest;
import com.prasad.banking.dto.AccountResponse;
import com.prasad.banking.dto.AccountStatusRequest;
import com.prasad.banking.entity.AccountStatus;

import java.util.List;

/**
 * AccountService interface — defines all account management operations.
 */
public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    List<AccountResponse> getAllAccounts();

    AccountResponse getAccountById(Long id);

    AccountResponse getAccountByNumber(String accountNumber);

    AccountResponse updateAccountStatus(Long id, AccountStatusRequest request);
}
