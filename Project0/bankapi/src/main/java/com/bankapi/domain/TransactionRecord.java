package com.bankapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRecord {

    private final long transactionId;
    private final String accountId;
    private final String relatedAccountId;
    private final String transferGroupId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime createdAt;

    public TransactionRecord(long transactionId, String accountId, String relatedAccountId,
                              String transferGroupId, TransactionType type, BigDecimal amount,
                              BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.transferGroupId = transferGroupId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getRelatedAccountId() {
        return relatedAccountId;
    }

    public String getTransferGroupId() {
        return transferGroupId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        String counterparty = relatedAccountId == null ? "" : " (with " + relatedAccountId + ")";
        return String.format("[%s] %-12s $%10.2f%s | balance after: $%.2f",
                createdAt, type, amount, counterparty, balanceAfter);
    }
}