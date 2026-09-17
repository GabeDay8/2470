package com.bankapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bankapi.domain.Account;
import com.bankapi.domain.TransactionRecord;
import com.bankapi.domain.TransactionType;
import com.bankapi.exception.BankingException;
import com.bankapi.persistence.AccountDAO;
import com.bankapi.persistence.TransactionDAO;
import com.bankapi.security.PinHasher;

/*
 These tests never touch a real database - AccountDAO and TransactionDAO
 are Mockito fakes, so every test here is purely checking AccountServiceImpl's
 own logic (the business rules), in isolation from Postgres.
 */
class AccountServiceImplTest {

    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        transactionDAO = mock(TransactionDAO.class);
        service = new AccountServiceImpl(accountDAO, transactionDAO);
    }

    // --- register ---

    @Test
    void register_createsAccount_whenAccountIdIsUnique() {
        when(accountDAO.findById("acc1")).thenReturn(null);

        Account created = service.register("acc1", "1234", "Gabe Day");

        assertEquals("acc1", created.getAccountId());
        assertEquals("Gabe Day", created.getOwnerName());
        assertEquals(BigDecimal.ZERO, created.getBalance());
        verify(accountDAO).createAccount(created);
    }

    @Test
    void register_throws_whenAccountIdAlreadyExists() {
        Account existing = new Account("acc1", "hash", "Someone Else", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        assertThrows(BankingException.class, () -> service.register("acc1", "1234", "Gabe Day"));
        verify(accountDAO, never()).createAccount(any());
    }

    // --- login ---

    @Test
    void login_returnsAccount_whenPinIsCorrect() {
        String correctHash = PinHasher.hash("1234");
        Account existing = new Account("acc1", correctHash, "Gabe Day", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        Account result = service.login("acc1", "1234");

        assertEquals("acc1", result.getAccountId());
    }

    @Test
    void login_throws_whenPinIsWrong() {
        String correctHash = PinHasher.hash("1234");
        Account existing = new Account("acc1", correctHash, "Gabe Day", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        assertThrows(BankingException.class, () -> service.login("acc1", "9999"));
    }

    @Test
    void login_throws_whenAccountDoesNotExist() {
        when(accountDAO.findById("ghost")).thenReturn(null);

        assertThrows(BankingException.class, () -> service.login("ghost", "1234"));
    }

    // --- getBalance ---

    @Test
    void getBalance_returnsBalance_whenAccountExists() {
        Account existing = new Account("acc1", "hash", "Gabe Day", new BigDecimal("150.00"), LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        BigDecimal balance = service.getBalance("acc1");

        assertEquals(new BigDecimal("150.00"), balance);
    }

    @Test
    void getBalance_throws_whenAccountDoesNotExist() {
        when(accountDAO.findById("ghost")).thenReturn(null);

        assertThrows(BankingException.class, () -> service.getBalance("ghost"));
    }

    // --- deposit ---

    @Test
    void deposit_returnsNewBalance_whenAmountIsPositive() {
        Account existing = new Account("acc1", "hash", "Gabe Day", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);
        when(transactionDAO.recordDeposit("acc1", new BigDecimal("25.00"))).thenReturn(new BigDecimal("35.00"));

        BigDecimal newBalance = service.deposit("acc1", new BigDecimal("25.00"));

        assertEquals(new BigDecimal("35.00"), newBalance);
    }

    @Test
    void deposit_throws_whenAmountIsZeroOrNegative() {
        Account existing = new Account("acc1", "hash", "Gabe Day", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        assertThrows(BankingException.class, () -> service.deposit("acc1", BigDecimal.ZERO));
        verify(transactionDAO, never()).recordDeposit(any(), any());
    }

    // --- withdraw ---

    @Test
    void withdraw_returnsNewBalance_whenSufficientFunds() {
        Account existing = new Account("acc1", "hash", "Gabe Day", new BigDecimal("100.00"), LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);
        when(transactionDAO.recordWithdrawal("acc1", new BigDecimal("40.00"))).thenReturn(new BigDecimal("60.00"));

        BigDecimal newBalance = service.withdraw("acc1", new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), newBalance);
    }

    @Test
    void withdraw_throws_whenAmountExceedsBalance() {
        Account existing = new Account("acc1", "hash", "Gabe Day", new BigDecimal("20.00"), LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);

        assertThrows(BankingException.class, () -> service.withdraw("acc1", new BigDecimal("50.00")));
        verify(transactionDAO, never()).recordWithdrawal(any(), any());
    }

    // --- transfer ---

    @Test
    void transfer_returnsSenderNewBalance_whenValidAndSufficientFunds() {
        Account sender = new Account("acc1", "hash", "Gabe Day", new BigDecimal("100.00"), LocalDateTime.now());
        Account receiver = new Account("acc2", "hash", "Someone Else", new BigDecimal("10.00"), LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(sender);
        when(accountDAO.findById("acc2")).thenReturn(receiver);
        when(transactionDAO.recordTransfer("acc1", "acc2", new BigDecimal("30.00"))).thenReturn(new BigDecimal("70.00"));

        BigDecimal senderBalanceAfter = service.transfer("acc1", "acc2", new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), senderBalanceAfter);
    }

    @Test
    void transfer_throws_whenSenderHasInsufficientFunds() {
        Account sender = new Account("acc1", "hash", "Gabe Day", new BigDecimal("10.00"), LocalDateTime.now());
        Account receiver = new Account("acc2", "hash", "Someone Else", BigDecimal.ZERO, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(sender);
        when(accountDAO.findById("acc2")).thenReturn(receiver);

        assertThrows(BankingException.class, () -> service.transfer("acc1", "acc2", new BigDecimal("50.00")));
        verify(transactionDAO, never()).recordTransfer(any(), any(), any());
    }

    @Test
    void transfer_throws_whenTransferringToSameAccount() {
        assertThrows(BankingException.class, () -> service.transfer("acc1", "acc1", new BigDecimal("10.00")));
    }

    // --- getRecentTransactions ---

    @Test
    void getRecentTransactions_returnsHistory_whenAccountExists() {
        Account existing = new Account("acc1", "hash", "Gabe Day", BigDecimal.TEN, LocalDateTime.now());
        when(accountDAO.findById("acc1")).thenReturn(existing);
        TransactionRecord record = new TransactionRecord(1L, "acc1", null, null,
                TransactionType.DEPOSIT, new BigDecimal("10.00"), new BigDecimal("20.00"), LocalDateTime.now());
        when(transactionDAO.findByAccountId("acc1", 10)).thenReturn(List.of(record));

        List<TransactionRecord> history = service.getRecentTransactions("acc1", 10);

        assertEquals(1, history.size());
        assertEquals(TransactionType.DEPOSIT, history.get(0).getType());
    }

    @Test
    void getRecentTransactions_throws_whenAccountDoesNotExist() {
        when(accountDAO.findById("ghost")).thenReturn(null);

        assertThrows(BankingException.class, () -> service.getRecentTransactions("ghost", 10));
    }
}