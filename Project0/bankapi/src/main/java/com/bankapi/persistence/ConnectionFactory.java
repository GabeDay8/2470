package com.bankapi.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hands out JDBC connections to the Postgres database described in
 * db.properties. This mirrors the singleton pattern from the course
 * reference, with one deliberate change: it loads db.properties from the
 * CLASSPATH (getResourceAsStream) instead of a relative file path
 * ("src/main/resources/db.properties"). A relative path only works if you
 * happen to launch the JVM from the exact project directory; loading from
 * the classpath works no matter where the app is run from, including when
 * it's packaged into a jar.
 */
public class ConnectionFactory {

    private static final ConnectionFactory INSTANCE = new ConnectionFactory();

    private final Properties props = new Properties();

    private ConnectionFactory() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "db.properties not found on the classpath. Make sure it exists at " +
                        "src/main/resources/db.properties.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
    }

    public static ConnectionFactory getConnectionFactory() {
        return INSTANCE;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    props.getProperty("DB_URL"),
                    props.getProperty("DB_USER"),
                    props.getProperty("DB_PASSWORD"));
        } catch (SQLException e) {
            throw new IllegalStateException("Could not connect to the database", e);
        }
    }
}