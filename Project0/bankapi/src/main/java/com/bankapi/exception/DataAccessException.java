package com.bankapi.exception;

/**
 * Represents a SYSTEM failure - the database connection dropped, a query
 * was malformed, etc. This is deliberately a SEPARATE hierarchy from
 * BankingException: the message here may contain technical detail and
 * should be logged at ERROR, never shown to the end user directly. The API
 * layer catches this separately and shows a generic "Service unavailable"
 * message instead.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}