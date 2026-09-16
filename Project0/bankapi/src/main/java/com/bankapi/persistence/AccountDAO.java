package com.bankapi.persistence;

import com.bankapi.domain.Account;

public interface AccountDAO {

    /**
     * Inserts a brand new account. Callers are responsible for checking
     * uniqueness first (that's a Service-layer business rule).
     */
    void createAccount(Account account);

    /**
     * Returns the account, or null if no account with that ID exists.
     * (Same "return null, let the caller decide" style as the course
     * reference's getStudentById.)
     */
    Account findById(String accountId);
}