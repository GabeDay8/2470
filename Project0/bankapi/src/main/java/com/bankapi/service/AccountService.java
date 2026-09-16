package com.bankapi.service;

import java.math.BigDecimal;
import java.util.List;

import com.bankapi.domain.Account;
import com.bankapi.domain.TransactionRecord;

public interface AccountService {

    Account register(String accountId, String pin, String ownerName);

    Account login(String accountId, String pin);

    BigDecimal getBalance(String accountId);

    BigDecimal deposit(String accountId, BigDecimal amount);

    BigDecimal withdraw(String accountId, BigDecimal amount);

    BigDecimal transfer(String fromAccountId, String toAccountId, BigDecimal amount);

    List<TransactionRecord> getRecentTransactions(String accountId, int limit);
}