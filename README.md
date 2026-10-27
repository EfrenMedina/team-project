# Team Project: Stock Trading Simulator

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

## Screenshots

_Coming once the first screens are built._
