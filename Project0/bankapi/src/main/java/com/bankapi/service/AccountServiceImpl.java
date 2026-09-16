package com.bankapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.bankapi.domain.Account;
import com.bankapi.domain.TransactionRecord;
import com.bankapi.exception.BankingException;
import com.bankapi.persistence.AccountDAO;
import com.bankapi.persistence.TransactionDAO;
import com.bankapi.security.PinHasher;

public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = Logger.getLogger(AccountServiceImpl.class.getName());
    private static final Pattern PIN_PATTERN = Pattern.compile("\\d{4,6}");

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Account register(String accountId, String pin, String ownerName) {
        if (accountId == null || accountId.isBlank()) {
            throw new BankingException("Account ID cannot be blank");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new BankingException("Name cannot be blank");
        }
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new BankingException("PIN must be 4 to 6 digits");
        }
        if (accountDAO.findById(accountId) != null) {
            throw new BankingException("Account ID " + accountId + " is already taken");
        }

        Account account = new Account(accountId, PinHasher.hash(pin), ownerName, BigDecimal.ZERO, LocalDateTime.now());
        accountDAO.createAccount(account);
        LOGGER.info(() -> "New account registered: " + accountId);
        return account;
    }

    @Override
    public Account login(String accountId, String pin) {
        Account account = accountDAO.findById(accountId);

        // Deliberately the SAME error message whether the account doesn't exist or
        // the PIN is wrong - this stops someone from being able to tell which one
        // it was, which would otherwise let them fish for valid account IDs.
        if (account == null || !PinHasher.matches(pin, account.getPinHash())) {
            LOGGER.severe(() -> "Failed login attempt for account " + accountId);
            throw new BankingException("Invalid Account ID or PIN");
        }

        LOGGER.info(() -> "Account " + accountId + " logged in successfully");
        return account;
    }

    @Override
    public BigDecimal getBalance(String accountId) {
        return requireAccount(accountId).getBalance();
    }

    @Override
    public BigDecimal deposit(String accountId, BigDecimal amount) {
        requireAccount(accountId);
        requirePositiveAmount(amount);
        BigDecimal newBalance = transactionDAO.recordDeposit(accountId, amount);
        LOGGER.info(() -> "Deposit of " + amount + " recorded for account " + accountId);
        return newBalance;
    }

    @Override
    public BigDecimal withdraw(String accountId, BigDecimal amount) {
        Account account = requireAccount(accountId);
        requirePositiveAmount(amount);

        // This is the Business layer's "can this user afford it" check - it gives a
        // fast, friendly rejection for the common case. TransactionDAO.recordWithdrawal
        // re-checks the SAME rule atomically at the database level (see the "AND
        // balance >= ?" guard there), which is what actually prevents an overdraft
        // if two withdrawals happened at the exact same moment. Belt and suspenders.
        if (account.getBalance().compareTo(amount) < 0) {
            LOGGER.severe(() -> "Rejected withdrawal of " + amount + " for account " + accountId
                    + " - insufficient funds");
            throw new BankingException("Insufficient funds: balance is $" + account.getBalance());
        }

        BigDecimal newBalance = transactionDAO.recordWithdrawal(accountId, amount);
        LOGGER.info(() -> "Withdrawal of " + amount + " recorded for account " + accountId);
        return newBalance;
    }

    @Override
    public BigDecimal transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new BankingException("Cannot transfer to the same account");
        }

        Account fromAccount = requireAccount(fromAccountId);
        requireAccount(toAccountId);
        requirePositiveAmount(amount);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            LOGGER.severe(() -> "Rejected transfer of " + amount + " from " + fromAccountId
                    + " to " + toAccountId + " - insufficient funds");
            throw new BankingException("Insufficient funds: balance is $" + fromAccount.getBalance());
        }

        BigDecimal senderBalanceAfter = transactionDAO.recordTransfer(fromAccountId, toAccountId, amount);
        LOGGER.info(() -> "Transfer of " + amount + " recorded from " + fromAccountId + " to " + toAccountId);
        return senderBalanceAfter;
    }

    @Override
    public List<TransactionRecord> getRecentTransactions(String accountId, int limit) {
        requireAccount(accountId);
        return transactionDAO.findByAccountId(accountId, limit);
    }

    private Account requireAccount(String accountId) {
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new BankingException("No account found with ID " + accountId);
        }
        return account;
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be greater than zero");
        }
    }
}