package com.bankapi.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.bankapi.domain.Account;
import com.bankapi.exception.DataAccessException;

/*
 Repository-layer tests for AccountDAOImpl. Unlike the Service-layer
 tests (which mock the DAOs), these run against the real Postgres
 database in Docker - there's no meaningful way to "mock" a SQL
 statement and still prove the SQL itself is correct.

 Every test creates its own account with a random ID, and the
 @AfterEach cleanup method deletes it afterward, so running this class
 never leaves test data behind and never collides with real accounts.

 Requires the bankapi-db Docker container to be running.
 */
class AccountDAOImplTest {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final Set<String> createdAccountIds = new HashSet<>();

    @BeforeAll
    static void setUpSchema() {
        SchemaInitializer.initialize();
    }

    @AfterEach
    void cleanUpTestData() throws SQLException {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement deleteAccounts = connection.prepareStatement(
                        "DELETE FROM accounts WHERE account_id = ?")) {
            for (String accountId : createdAccountIds) {
                deleteAccounts.setString(1, accountId);
                deleteAccounts.executeUpdate();
            }
        }
        createdAccountIds.clear();
    }

    private String freshAccountId() {
        String id = "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        createdAccountIds.add(id);
        return id;
    }

    @Test
    void createAccount_success_canBeFoundById() {
        String accountId = freshAccountId();
        Account account = new Account(accountId, "fake-hash", "Ada Lovelace",
                new BigDecimal("25.00"), LocalDateTime.now());

        accountDAO.createAccount(account);
        Account found = accountDAO.findById(accountId);

        assertNotNull(found);
        assertEquals(accountId, found.getAccountId());
        assertEquals("Ada Lovelace", found.getOwnerName());
        assertEquals(0, new BigDecimal("25.00").compareTo(found.getBalance()));
    }

    @Test
    void createAccount_duplicateAccountId_throwsDataAccessException() {
        String accountId = freshAccountId();
        Account account = new Account(accountId, "fake-hash", "Ada Lovelace",
                BigDecimal.ZERO, LocalDateTime.now());
        accountDAO.createAccount(account);

        // Creating a second account with the SAME id collides with the
        // PRIMARY KEY constraint in Postgres, and AccountDAOImpl.createAccount
        // wraps that SQLException as a DataAccessException.
        assertThrows(DataAccessException.class, () -> accountDAO.createAccount(account));
    }

    @Test
    void findById_unknownAccount_returnsNull() {
        Account found = accountDAO.findById("DOES-NOT-EXIST");
        assertNull(found);
    }

    @Test
    void findById_existingAccount_returnsMatchingFields() {
        String accountId = freshAccountId();
        Account account = new Account(accountId, "another-hash", "Grace Hopper",
                new BigDecimal("100.50"), LocalDateTime.now());
        accountDAO.createAccount(account);

        Account found = accountDAO.findById(accountId);

        assertNotNull(found);
        assertEquals("another-hash", found.getPinHash());
        assertEquals("Grace Hopper", found.getOwnerName());
        assertEquals(0, new BigDecimal("100.50").compareTo(found.getBalance()));
    }
}