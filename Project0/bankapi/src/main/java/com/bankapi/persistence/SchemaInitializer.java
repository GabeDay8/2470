package com.bankapi.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
 Creates the two tables the whole app is built on, if they don't already
 exist. Call SchemaInitializer.initialize() once, at startup, from Main.
 */
public class SchemaInitializer {

    private static final String CREATE_ACCOUNTS_TABLE = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_id  VARCHAR(20)     PRIMARY KEY,
                pin_hash    VARCHAR(100)     NOT NULL,
                owner_name  VARCHAR(80)     NOT NULL,
                balance     DECIMAL(19,4)   NOT NULL DEFAULT 0 CHECK (balance >= 0),
                created_at  TIMESTAMP       NOT NULL DEFAULT now()
            )
            """;

    private static final String CREATE_TRANSACTIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id      BIGSERIAL       PRIMARY KEY,
                account_id          VARCHAR(20)     NOT NULL REFERENCES accounts(account_id),
                related_account_id  VARCHAR(20)     REFERENCES accounts(account_id),
                transfer_group_id   UUID,
                type                VARCHAR(20)     NOT NULL CHECK (type IN
                                        ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN')),
                amount              DECIMAL(19,4)   NOT NULL CHECK (amount > 0),
                balance_after       DECIMAL(19,4)   NOT NULL,
                created_at          TIMESTAMP       NOT NULL DEFAULT now()
            )
            """;

    private static final String CREATE_TRANSACTIONS_INDEX =
            "CREATE INDEX IF NOT EXISTS idx_transactions_account " +
            "ON transactions (account_id, created_at DESC)";

    private SchemaInitializer() {
    }

    public static void initialize() {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(CREATE_ACCOUNTS_TABLE);
            statement.execute(CREATE_TRANSACTIONS_TABLE);
            statement.execute(CREATE_TRANSACTIONS_INDEX);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database schema", e);
        }
    }
}