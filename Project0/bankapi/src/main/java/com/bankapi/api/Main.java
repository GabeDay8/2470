package com.bankapi.api;

import com.bankapi.persistence.AccountDAO;
import com.bankapi.persistence.AccountDAOImpl;
import com.bankapi.persistence.SchemaInitializer;
import com.bankapi.persistence.TransactionDAO;
import com.bankapi.persistence.TransactionDAOImpl;
import com.bankapi.service.AccountService;
import com.bankapi.service.AccountServiceImpl;
import com.bankapi.util.AppLogging;

public class Main {
    public static void main(String[] args) {
        AppLogging.configure();
        SchemaInitializer.initialize();

        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();
        AccountService accountService = new AccountServiceImpl(accountDAO, transactionDAO);

        new BankRepl(accountService).run();
    }
}