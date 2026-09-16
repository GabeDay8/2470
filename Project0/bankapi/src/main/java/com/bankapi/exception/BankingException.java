package com.bankapi.exception;

/**
 * Base type for every exception that represents a USER-FACING business rule
 * violation (wrong PIN, insufficient funds, duplicate account, etc.).
 *
 * The message on any BankingException is always safe to print directly to
 * the person using the CLI - it never contains SQL, stack traces, or
 * internal details. Compare this to DataAccessException, which represents
 * a system failure and is handled completely differently by the API layer.
 */
public class BankingException extends RuntimeException {
    public BankingException(String message) {
        super(message);
    }
}