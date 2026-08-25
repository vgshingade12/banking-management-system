package com.prasad.banking.service;

import com.prasad.banking.dto.*;
import com.prasad.banking.entity.*;
import com.prasad.banking.exception.*;
import com.prasad.banking.repository.AccountRepository;
import com.prasad.banking.repository.TransactionRepository;
import com.prasad.banking.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TransactionServiceImpl.
 *
 * These are the most important tests in the project — they verify the
 * core banking rules:
 *   ✓ Deposit works correctly
 *   ✓ Withdrawal rejects insufficient balance
 *   ✓ Transfer is rejected for same account
 *   ✓ Transfer is rejected if source has insufficient funds
 *   ✓ Blocked/Closed accounts reject transactions
 *   ✓ BigDecimal arithmetic is correct
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account activeAccount;
    private Account anotherActiveAccount;
    private Account blockedAccount;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Inject @Value fields (since we're not loading Spring context)
        ReflectionTestUtils.setField(transactionService,
                "transactionReferencePrefix", "TXN");

        customer = Customer.builder()
                .id(1L)
                .customerCode("CUST-0001")
                .firstName("Vaibhavi")
                .lastName("Shingade")
                .email("vaibhavi@email.com")
                .phone("9876543210")
                .build();

        activeAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC100001")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("10000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        anotherActiveAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC100002")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        blockedAccount = Account.builder()
                .id(3L)
                .accountNumber("ACC100003")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .status(AccountStatus.BLOCKED)
                .customer(customer)
                .build();
    }

    // -----------------------------------------------------------------------
    // DEPOSIT TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deposit — success, balance increases correctly")
    void deposit_Success_BalanceIncreases() {
        // ARRANGE
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DepositRequest request = new DepositRequest(new BigDecimal("3000.00"), "Salary credit");

        // ACT
        TransactionOperationResponse response = transactionService.deposit("ACC100001", request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Deposit successful");
        assertThat(response.getNewBalance()).isEqualByComparingTo(new BigDecimal("13000.00"));
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));

        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deposit — account not found throws AccountNotFoundException")
    void deposit_AccountNotFound_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC999999"))
                .thenReturn(Optional.empty());

        DepositRequest request = new DepositRequest(new BigDecimal("1000"), "test");

        assertThatThrownBy(() -> transactionService.deposit("ACC999999", request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deposit — blocked account throws AccountNotActiveException")
    void deposit_BlockedAccount_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC100003"))
                .thenReturn(Optional.of(blockedAccount));

        DepositRequest request = new DepositRequest(new BigDecimal("1000"), "test");

        assertThatThrownBy(() -> transactionService.deposit("ACC100003", request))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("BLOCKED");
    }

    // -----------------------------------------------------------------------
    // WITHDRAWAL TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Withdrawal — success, balance decreases correctly")
    void withdraw_Success_BalanceDecreases() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("4000.00"), "ATM");

        TransactionOperationResponse response = transactionService.withdraw("ACC100001", request);

        assertThat(response.getMessage()).isEqualTo("Withdrawal successful");
        // 10000 - 4000 = 6000
        assertThat(response.getNewBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
    }

    @Test
    @DisplayName("Withdrawal — insufficient balance throws InsufficientBalanceException")
    void withdraw_InsufficientBalance_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));

        // Account has 10000, trying to withdraw 15000 — should fail
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("15000.00"), "test");

        assertThatThrownBy(() -> transactionService.withdraw("ACC100001", request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("ACC100001")
                .hasMessageContaining("15000");

        // Balance must NOT change — no save call
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Withdrawal — exact balance withdrawal succeeds (balance becomes 0)")
    void withdraw_ExactBalance_Succeeds() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Withdraw exactly the full balance
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("10000.00"), "Full withdrawal");

        TransactionOperationResponse response = transactionService.withdraw("ACC100001", request);

        assertThat(response.getNewBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // -----------------------------------------------------------------------
    // TRANSFER TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Transfer — success, both balances update correctly")
    void transfer_Success_BothBalancesUpdate() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC100002"))
                .thenReturn(Optional.of(anotherActiveAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC100001")
                .toAccount("ACC100002")
                .amount(new BigDecimal("3000.00"))
                .description("Transfer to friend")
                .build();

        TransferResponse response = transactionService.transfer(request);

        assertThat(response.getMessage()).isEqualTo("Transfer successful");
        // Source: 10000 - 3000 = 7000
        assertThat(response.getFromAccountNewBalance()).isEqualByComparingTo(new BigDecimal("7000.00"));

        // Verify both accounts were saved
        verify(accountRepository, times(2)).save(any(Account.class));

        // Verify two transaction records were created (one per account)
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer — same account throws InvalidTransactionException")
    void transfer_SameAccount_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC100001")
                .toAccount("ACC100001")  // same account!
                .amount(new BigDecimal("1000"))
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("same");

        // No DB calls should happen
        verify(accountRepository, never()).findByAccountNumber(any());
    }

    @Test
    @DisplayName("Transfer — insufficient balance in source account")
    void transfer_InsufficientBalance_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount)); // balance 10000
        when(accountRepository.findByAccountNumber("ACC100002"))
                .thenReturn(Optional.of(anotherActiveAccount));

        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC100001")
                .toAccount("ACC100002")
                .amount(new BigDecimal("50000.00")) // more than balance
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class);

        // Neither account should be saved
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer — blocked source account throws AccountNotActiveException")
    void transfer_BlockedSourceAccount_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC100003"))
                .thenReturn(Optional.of(blockedAccount));

        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC100003")
                .toAccount("ACC100001")
                .amount(new BigDecimal("500"))
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("BLOCKED");
    }

    @Test
    @DisplayName("Transfer — destination account not found")
    void transfer_DestinationNotFound_ThrowsException() {
        when(accountRepository.findByAccountNumber("ACC100001"))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACCXXXXX"))
                .thenReturn(Optional.empty());

        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC100001")
                .toAccount("ACCXXXXX")
                .amount(new BigDecimal("1000"))
                .build();

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }
}
