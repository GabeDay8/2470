package com.bankapi.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bankapi.domain.TransactionRecord;
import com.bankapi.domain.TransactionType;
import com.bankapi.exception.BankingException;
import com.bankapi.exception.DataAccessException;

/*
 This is the class that keeps the ledger ACID. Every public method opens
 exactly one connection, turns autocommit off, does every statement the
 operation needs on that same connection, and either commits everything
 or rolls back everything. As long as every statement in one
 "operation" shares one connection, Postgres guarantees
 they succeed or fail together.
 */
public class TransactionDAOImpl implements TransactionDAO {

    private static final String CREDIT_SQL =
            "UPDATE accounts SET balance = balance + ? WHERE account_id = ? RETURNING balance";
    /*
     The "AND balance >= ?" guard is what makes an overdraft impossible even under
     concurrent access: the database itself refuses the row update if funds are
     insufficient at the moment the statement runs, not just when Java checked earlier.
    */
    private static final String DEBIT_SQL =
            "UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND balance >= ? RETURNING balance";

    private static final String INSERT_TRANSACTION_SQL =
            "INSERT INTO transactions " +
            "(account_id, related_account_id, transfer_group_id, type, amount, balance_after, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ACCOUNT_SQL =
            "SELECT transaction_id, account_id, related_account_id, transfer_group_id, type, " +
            "amount, balance_after, created_at " +
            "FROM transactions WHERE account_id = ? ORDER BY created_at DESC LIMIT ?";

    @Override
    public BigDecimal recordDeposit(String accountId, BigDecimal amount) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal newBalance = credit(connection, accountId, amount);
                insertTransactionRow(connection, accountId, null, null,
                        TransactionType.DEPOSIT, amount, newBalance);
                connection.commit();
                return newBalance;
            } catch (BankingException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw new DataAccessException("Could not record deposit for account " + accountId, e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database to record deposit", e);
        }
    }

    @Override
    public BigDecimal recordWithdrawal(String accountId, BigDecimal amount) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal newBalance = debit(connection, accountId, amount);
                insertTransactionRow(connection, accountId, null, null,
                        TransactionType.WITHDRAWAL, amount, newBalance);
                connection.commit();
                return newBalance;
            } catch (BankingException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw new DataAccessException("Could not record withdrawal for account " + accountId, e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database to record withdrawal", e);
        }
    }

    @Override
    public BigDecimal recordTransfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        UUID transferGroupId = UUID.randomUUID();
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            connection.setAutoCommit(false);
            try {
                /*
                 If this debit succeeds but the credit below fails for any reason
                 (bad account ID, connection drop, etc.), the catch blocks roll
                 back the entire connection - this debit included. Money can't
                 vanish from the sender without landing on the receiver.
                */
                BigDecimal senderBalanceAfter = debit(connection, fromAccountId, amount);
                BigDecimal receiverBalanceAfter = credit(connection, toAccountId, amount);

                insertTransactionRow(connection, fromAccountId, toAccountId, transferGroupId,
                        TransactionType.TRANSFER_OUT, amount, senderBalanceAfter);
                insertTransactionRow(connection, toAccountId, fromAccountId, transferGroupId,
                        TransactionType.TRANSFER_IN, amount, receiverBalanceAfter);

                connection.commit();
                return senderBalanceAfter;
            } catch (BankingException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw new DataAccessException(
                        "Could not record transfer from " + fromAccountId + " to " + toAccountId, e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database to record transfer", e);
        }
    }

    @Override
    public List<TransactionRecord> findByAccountId(String accountId, int limit) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ACCOUNT_SQL)) {
            statement.setString(1, accountId);
            statement.setInt(2, limit);
            List<TransactionRecord> records = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
            }
            return records;
        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch transaction history for " + accountId, e);
        }
    }

    //     shared helpers, all operating on a caller-supplied connection so they
    //     participate in whatever transaction the caller already started

    private BigDecimal credit(Connection connection, String accountId, BigDecimal amount) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(CREDIT_SQL)) {
        statement.setBigDecimal(1, amount);
        statement.setString(2, accountId);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new BankingException("No account found with ID " + accountId);
            }
            return resultSet.getBigDecimal("balance");
            }
        }
    }

    /*
     This assumes the caller already confirmed the account exists (e.g. the
     user is logged in). Under that assumption, zero rows updated can only mean
     one thing: balance < amount, i.e. insufficient funds.
     */
    private BigDecimal debit(Connection connection, String accountId, BigDecimal amount) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DEBIT_SQL)) {
        statement.setBigDecimal(1, amount);
        statement.setString(2, accountId);
        statement.setBigDecimal(3, amount);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new BankingException(
                        "Account " + accountId + " does not have sufficient funds for this amount");
            }
            return resultSet.getBigDecimal("balance");
            }
        }
    }

    private void insertTransactionRow(Connection connection, String accountId, String relatedAccountId,
            UUID transferGroupId, TransactionType type, BigDecimal amount, BigDecimal balanceAfter)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TRANSACTION_SQL)) {
            statement.setString(1, accountId);
            statement.setString(2, relatedAccountId);
            statement.setObject(3, transferGroupId);
            statement.setString(4, type.name());
            statement.setBigDecimal(5, amount);
            statement.setBigDecimal(6, balanceAfter);
            statement.setObject(7, LocalDateTime.now());
            statement.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            // Best effort - the exception that triggered this rollback is what
            // actually matters and is already being thrown by the caller.
        }
    }

    private TransactionRecord mapRecord(ResultSet resultSet) throws SQLException {
        Object transferGroupId = resultSet.getObject("transfer_group_id");
        return new TransactionRecord(
                resultSet.getLong("transaction_id"),
                resultSet.getString("account_id"),
                resultSet.getString("related_account_id"),
                transferGroupId == null ? null : transferGroupId.toString(),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getBigDecimal("amount"),
                resultSet.getBigDecimal("balance_after"),
                resultSet.getObject("created_at", LocalDateTime.class));
    }
}