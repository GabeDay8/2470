package com.bankapi.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.bankapi.domain.Account;
import com.bankapi.domain.TransactionRecord;
import com.bankapi.exception.BankingException;
import com.bankapi.exception.DataAccessException;
import com.bankapi.service.AccountService;

/**
 * The whole API layer. Its only job is: print menus, read input, call
 * AccountService, print the result. It never touches SQL and never decides
 * whether a withdrawal is allowed - that's all the Service layer's job.
 *
 * Two exception types get caught here, and ONLY here, and they're handled
 * completely differently:
 *   - BankingException: a user mistake (wrong PIN, insufficient funds...).
 *     Its message is safe to print as-is.
 *   - DataAccessException: a system failure. Its message may contain SQL
 *     detail, so it gets logged (not shown), and the user sees a generic,
 *     non-scary message instead. This is the rubric's "Security First" rule.
 */
public class BankRepl {

    private static final Logger LOGGER = Logger.getLogger(BankRepl.class.getName());

    private final AccountService accountService;
    private final Scanner scanner = new Scanner(System.in);
    private Account currentAccount;

    public BankRepl(AccountService accountService) {
        this.accountService = accountService;
    }

    public void run() {
        System.out.println("Welcome to Bank of CLI.");
        while (true) {
            try {
                boolean keepGoing = (currentAccount == null) ? handleLoggedOutCommand() : handleLoggedInCommand();
                if (!keepGoing) {
                    return;
                }
            } catch (BankingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DataAccessException e) {
                LOGGER.severe(() -> "Database failure: " + e.getMessage());
                System.out.println("Service unavailable right now. Please try again in a moment.");
            }
        }
    }

    private boolean handleLoggedOutCommand() {
        System.out.println();
        System.out.println("1) Register   2) Login   3) Exit");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> register();
            case "2" -> login();
            case "3" -> {
                System.out.println("Goodbye.");
                return false;
            }
            default -> System.out.println("Unknown option.");
        }
        return true;
    }

    private boolean handleLoggedInCommand() {
        System.out.println();
        System.out.println("Logged in as " + currentAccount.getAccountId());
        System.out.println("1) Balance   2) Deposit   3) Withdraw   4) Transfer   5) History   6) Log out   7) Exit");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> showBalance();
            case "2" -> deposit();
            case "3" -> withdraw();
            case "4" -> transfer();
            case "5" -> showHistory();
            case "6" -> {
                System.out.println("Logged out.");
                currentAccount = null;
            }
            case "7" -> {
                System.out.println("Goodbye.");
                return false;
            }
            default -> System.out.println("Unknown option.");
        }
        return true;
    }

    private void register() {
        System.out.print("Choose an Account ID: ");
        String accountId = scanner.nextLine().trim();
        System.out.print("Your name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Choose a 4-6 digit PIN: ");
        String pin = scanner.nextLine().trim();

        Account account = accountService.register(accountId, pin, name);
        System.out.println("Account created. Welcome, " + account.getOwnerName() + ".");
    }

    private void login() {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        System.out.print("PIN: ");
        String pin = scanner.nextLine().trim();

        currentAccount = accountService.login(accountId, pin);
        System.out.println("Welcome back, " + currentAccount.getOwnerName() + ".");
    }

    private void showBalance() {
        BigDecimal balance = accountService.getBalance(currentAccount.getAccountId());
        System.out.printf("Current balance: $%.2f%n", balance);
    }

    private void deposit() {
        BigDecimal amount = readAmount("Amount to deposit: ");
        BigDecimal newBalance = accountService.deposit(currentAccount.getAccountId(), amount);
        System.out.printf("Deposited $%.2f. New balance: $%.2f%n", amount, newBalance);
    }

    private void withdraw() {
        BigDecimal amount = readAmount("Amount to withdraw: ");
        BigDecimal newBalance = accountService.withdraw(currentAccount.getAccountId(), amount);
        System.out.printf("Withdrew $%.2f. New balance: $%.2f%n", amount, newBalance);
    }

    private void transfer() {
        System.out.print("Recipient Account ID: ");
        String toAccountId = scanner.nextLine().trim();
        BigDecimal amount = readAmount("Amount to transfer: ");
        BigDecimal newBalance = accountService.transfer(currentAccount.getAccountId(), toAccountId, amount);
        System.out.printf("Transferred $%.2f to %s. Your new balance: $%.2f%n", amount, toAccountId, newBalance);
    }

    private void showHistory() {
        List<TransactionRecord> records = accountService.getRecentTransactions(currentAccount.getAccountId(), 10);
        if (records.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("Recent transactions:");
        records.forEach(record -> System.out.println("  " + record));
    }

    private BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new BankingException("\"" + input + "\" is not a valid dollar amount");
        }
    }
}