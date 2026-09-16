package com.bankapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private final String accountId;
    private final String pinHash;
    private final String ownerName;
    private BigDecimal balance;
    private final LocalDateTime createdAt;

    public Account(String accountId, String pinHash, String ownerName, BigDecimal balance, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.pinHash = pinHash;
        this.ownerName = ownerName;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPinHash() {
        return pinHash;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Account ID: %s | Owner: %s | Balance: $%.2f", accountId, ownerName, balance);
    }
}