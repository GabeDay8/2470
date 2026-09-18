# Bank of CLI

Terminal-based banking application in Java. Supports account registration/login, balance inquiry, deposits, withdrawals, transfers, and transaction history, backed by PostgreSQL over JDBC.

## Architecture

3-layer architecture, strict downward dependency only:
API (BankRepl, Main)-> Service (AccountServiceImpl) — business rules-> Repository (AccountDAOImpl, TransactionDAOImpl) — JDBC/SQL

Each layer only calls the layer directly below it. No layer is skipped.

## Tech Stack

- Java 17
- Maven
- PostgreSQL 16 (Docker)
- JDBC (raw, no ORM)
- JUnit 5 + Mockito
- `java.util.logging`

## Features

- **Register / Login** — Account ID + PIN, PIN stored as salted SHA-256 hash
- **Balance inquiry**
- **Deposit / Withdrawal** — no overdraft, enforced at the database level
- **Transfer** — atomic across two accounts (ACID)
- **Transaction history**
- **Logging** — INFO / SEVERE (ERROR equivalent) to `bank-of-cli.log`

## Atomicity

Deposits, withdrawals, and transfers each run inside a single JDBC `Connection` with `setAutoCommit(false)`. On failure, the transaction rolls back completely — no partial writes. Overdraft protection is enforced with a `WHERE balance >= ?` guard clause on the debit `UPDATE`, preventing race conditions between concurrent withdrawals.

Money is represented as `BigDecimal`, not `double`, to avoid floating-point rounding error.

## Security

- PINs are never stored in plaintext — SHA-256 with a random per-account salt
- All SQL is parameterized (`PreparedStatement`) — no string-concatenated queries
- User-facing errors (`BankingException`) are separated from system errors (`DataAccessException`); raw SQL/stack traces are never shown to the user
- Login failure returns the same generic message whether the account doesn't exist or the PIN is wrong (prevents account enumeration)

## Project Structure

com.bankapi
├── api # Main, BankRepl (CLI/REPL)
├── service # AccountService, AccountServiceImpl (business rules)
├── persistence # DAOs, ConnectionFactory, SchemaInitializer (JDBC)
├── domain # Account, TransactionRecord, TransactionType
├── exception # BankingException, DataAccessException
├── security # PinHasher
└── util # AppLogging


## Running

```bash
docker compose up -d          # start Postgres
mvn compile exec:java -Dexec.mainClass=com.bankapi.api.Main
```

## Testing

```bash
mvn test
```

28 JUnit 5 tests across Service and Repository layers — every method has a positive and negative test case ("2-Test Rule"). Service tests mock the DAOs with Mockito; Repository tests run against a live PostgreSQL instance to verify SQL correctness and atomicity, including a rejected transfer leaving both account balances unchanged.
