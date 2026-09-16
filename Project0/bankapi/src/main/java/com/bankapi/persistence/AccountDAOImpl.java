package com.bankapi.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.bankapi.domain.Account;
import com.bankapi.exception.DataAccessException;

public class AccountDAOImpl implements AccountDAO {

    private static final String INSERT_SQL =
            "INSERT INTO accounts (account_id, pin_hash, owner_name, balance, created_at) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL =
            "SELECT account_id, pin_hash, owner_name, balance, created_at " +
            "FROM accounts WHERE account_id = ?";

    @Override
    public void createAccount(Account account) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, account.getAccountId());
            statement.setString(2, account.getPinHash());
            statement.setString(3, account.getOwnerName());
            statement.setBigDecimal(4, account.getBalance());
            statement.setObject(5, account.getCreatedAt());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create account " + account.getAccountId(), e);
        }
    }

    @Override
    public Account findById(String accountId) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setString(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not look up account " + accountId, e);
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getString("account_id"),
                resultSet.getString("pin_hash"),
                resultSet.getString("owner_name"),
                resultSet.getBigDecimal("balance"),
                resultSet.getObject("created_at", LocalDateTime.class));
    }
}