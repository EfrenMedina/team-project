# Team Project: Stock Trading Simulator

[![CI](https://github.com/EfrenMedina/team-project/actions/workflows/ci.yml/badge.svg)](https://github.com/EfrenMedina/team-project/actions/workflows/ci.yml)

## Summary

A desktop app for practising stock trading with simulated money. Users sign up, search real stocks, view prices and charts, buy and sell shares, place limit orders, and track their portfolio and profit/loss. Every movement of cash is recorded in a double-entry ledger, so balances can always be explained and audited.

Built in Java 17 with Swing (FlatLaf), SQLite and Maven, following Clean Architecture.

## Features and who built them

| User story | Feature | Owner | Status |
|---|---|---|---|
| US-01 | Sign up | Efren Medina Arias | To do |
| US-02 | Log in and log out | Efren Medina Arias | To do |
| US-03 | Search stocks | Lifeable | To do |
| US-04 | Stock details and chart | Andrei Mazin | To do |
| US-05 | Buy shares, with no double-buys | Efren Medina Arias | To do |
| US-06 | Sell shares | Abdulhadi Alnowailaty | To do |
| US-07 | Portfolio and profit/loss | Abdulhadi Alnowailaty | To do |
| US-08 | Trade history | Abdulhadi Alnowailaty | To do |
| US-09 | Watchlist | Lifeable | To do |
| US-10 | Limit orders (place, list, cancel, automatic fills) | Andrei Mazin, Abdulhadi Alnowailaty | To do |
| US-11 | Account statement | Andrei Mazin | To do |
| US-12 | Realized gains report | Efren Medina Arias | To do |
| US-13 | Stop-loss orders (stretch) | Efren Medina Arias | To do |
| US-14 | Performance chart (stretch) | Abdulhadi Alnowailaty | To do |

Planning and tickets live in the team's Jira board (project `SCRUM`).

## APIs used

| API | Used for |
|---|---|
| [Finnhub](https://finnhub.io/docs/api) | Live quotes, symbol search, company profile |
| [Twelve Data](https://twelvedata.com/docs) | Daily price history for charts |

Provider decision, measured rate limits and fallback: _to be filled in by SCRUM-18._

API keys are read from `config.properties` in the project root (git-ignored, never committed).

## How to run

Requirements: JDK 17 and Maven 3.9+.

```bash
# Build and run all tests
mvn clean verify
```

1. Create `config.properties` in the project root with your API keys:
   ```properties
   finnhub.api.key=YOUR_KEY
   twelvedata.api.key=YOUR_KEY
   ```
2. Run the app: _main class and command to be added with the app shell (SCRUM-23)._

Offline demo mode: _to be added (SCRUM-43)._

## How we work

- Every change goes through a pull request into `main`, with one approval and a passing build.
- Branch names start with the Jira key, e.g. `SCRUM-19-ledger-entities`, so they link to the ticket.
- Money is stored as integer cents; never use `double` or `REAL` for money.

## How we test

- Run everything with `mvn clean verify`. This runs the JUnit 5 tests, writes the coverage report to `target/site/jacoco/index.html` and the style report to `target/checkstyle-result.xml`. CI uploads both on every PR.
- Copy the pattern in [`ExampleInteractorTest`](src/test/java/app/ExampleInteractorTest.java) when testing a use case:
  - an **in-memory DAO** instead of SQLite, so tests are fast and need no files;
  - a **fake presenter** that records what the interactor sent, so we assert on output data, not on the UI;
  - a **fixed `Clock`** passed into the interactor, so time-based logic gives the same result every run.
- Name tests after behaviour: `action_condition_expectedResult`, e.g. `buyShares_insufficientCash_presentsError`.
- Test both the success path and each failure path of every interactor.
- Tests must not call the real stock APIs; use the fake gateway and the recorded JSON in `src/test/resources/api/`.

## Screenshots

_Coming once the first screens are built._
