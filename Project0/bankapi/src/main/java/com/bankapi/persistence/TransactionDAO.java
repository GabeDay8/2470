package com.bankapi.persistence;

import java.math.BigDecimal;
import java.util.List;

import com.bankapi.domain.TransactionRecord;

public interface TransactionDAO {

    /* Credits the account and writes one DEPOSIT row, atomically. Returns the new balance. */
    BigDecimal recordDeposit(String accountId, BigDecimal amount);

    /*
     Debits the account and writes one WITHDRAWAL row, atomically. Returns the new balance.
     Throws InsufficientFundsException if the account can't cover the amount.
     */
    BigDecimal recordWithdrawal(String accountId, BigDecimal amount);

    /*
     Debits fromAccountId, credits toAccountId, and writes a TRANSFER_OUT + TRANSFER_IN
     pair - all four writes on ONE connection/transaction, so either all of it happens or
     none of it does. Returns the sender's new balance. Throws InsufficientFundsException
     if the sender can't cover the amount.
     */
    BigDecimal recordTransfer(String fromAccountId, String toAccountId, BigDecimal amount);

    /* Most recent transactions for this account, newest first. */
    List<TransactionRecord> findByAccountId(String accountId, int limit);
}