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
| [Twelve Data](https://twelvedata.com/docs) | Daily price history for charts, and fallback quotes |

### Provider decision (SCRUM-18)

Tested on 2026-10-29 with free-plan keys, using [`spike/MarketDataSpike.java`](spike/MarketDataSpike.java).
Every endpoint we need returned data for AAPL and MSFT. One real response per endpoint is saved in
[`src/test/resources/api/`](src/test/resources/api/).

| Endpoint | Provider | Result |
|---|---|---|
| Quote (`/quote`) | Finnhub | Works |
| Symbol search (`/search`) | Finnhub | Works |
| Company profile (`/stock/profile2`) | Finnhub | Works |
| Daily candles (`/stock/candle`) | Finnhub | **HTTP 403**, not on the free plan |
| US symbol list (`/stock/symbol?exchange=US`) | Finnhub | Works: 31,112 symbols with exchange codes, about 7 MB (checked 2026-10-31) |
| Daily time series (`/time_series`) | Twelve Data | Works |
| Quote (`/quote`) | Twelve Data | Works (used as the fallback) |

**Measured rate limits** (`java spike/MarketDataSpike.java limits`):

- **Finnhub: 60 requests per minute.** 60 back-to-back quote calls succeeded in 5.2 s, and the 61st
  got HTTP 429 `Too many requests`. The `X-Ratelimit-Limit` and `X-Ratelimit-Remaining` response
  headers show the budget, so the gateway can read them.
- **Twelve Data: 8 credits per minute and 800 per day.** Each call costs 1 credit, including
  `/api_usage`. The 9th credit in a minute got HTTP 429 `run out of API credits for the current minute`.

**Decision.** Finnhub serves quotes, symbol search and company profiles. Twelve Data serves daily
price history only, because Finnhub's candles need a paid plan. Twelve Data's 8 calls a minute is the
tight limit, so we cache each symbol's daily history and fetch it at most once a day. The 60
Finnhub calls a minute are shared by everything that polls quotes (watchlist, portfolio, limit
order fills), so polling must batch symbols and stay well under that budget.

**Fallback.**

- If Finnhub fails or returns 429, quotes come from **Twelve Data `/quote`**, which works on the
  free plan.
- If Twelve Data is out of credits, charts show the **last cached history**.
- If both providers are down, offline demo mode (SCRUM-43) uses `FakeStockDataGateway` and its
  recorded data (below).

### Stock data in the code (SCRUM-21)

Use cases get all stock data through
[`usecase.shared.StockDataGateway`](src/main/java/usecase/shared/StockDataGateway.java):
`search`, `getQuote`, `getProfile` and `getDailyHistory`. Each one throws the checked
`StockDataUnavailableException` when data can't be fetched, so every interactor has to handle it.

**Search results need an exchange, which Finnhub's `/search` doesn't return.** The Finnhub gateway
(SCRUM-31) gets it from Finnhub's US symbol list, `/stock/symbol?exchange=US`: one free call that
returned all 31,112 US symbols with their exchange code (`XNAS` = NASDAQ, `XNYS` = NYSE) on
2026-10-31. It's about 7 MB, so fetch it at most once a day and cache it. Results not in the list
are foreign listings (such as `603020.SS`) and should be dropped. Never call `/stock/profile2` once
per search result: one search for "apple" returned 11 results, which would use up the quote budget.

[`dataaccess.FakeStockDataGateway`](src/main/java/dataaccess/FakeStockDataGateway.java) serves real
recorded responses for **AAPL, MSFT, GOOGL, AMZN and NVDA**: quote, profile and one year of daily
history (2025-10-20 to 2026-10-30). Call `setPrice(ticker, price)` in a test to move a price; it
also sets the day's high and low to that price, so limit order fills depend only on the price you
chose.

**Offline mode (SCRUM-43) must fix the clock to the last recorded day.** The recorded history stops
at `FakeStockDataGateway.LAST_RECORDED_DAY` (2026-10-30). Anything that works out dates from
today, like the chart's 1M, 6M and 1Y ranges, must use a `Clock` fixed to that day when running on
the fake. With the real date, the 1M chart is already partly empty and goes fully empty after
2026-11-30, the 6M chart after 2027-04-30 and the 1Y chart after 2027-10-30.

The fake's data is in [`src/main/resources/fake-stock-data/`](src/main/resources/fake-stock-data/),
not in `src/test/resources/api/` as the Technical Design (p. 15) says. Main resources are
available to the running app as well as to tests, which offline mode needs. To re-record or add
tickers (one Twelve Data credit per ticker, at most 8 a minute):

```bash
java spike/MarketDataSpike.java fake AAPL MSFT GOOGL AMZN NVDA
```

- **Adding a ticker:** also add it to `FakeStockDataGateway.TICKERS`, or the fake won't load it.
- **Re-recording:** the new data has new prices and dates, so the build fails until you update the
  values `FakeStockDataGatewayTest` checks (for example AAPL's price, quote time and the number of
  trading days), `FIRST_RECORDED_DAY` and `LAST_RECORDED_DAY` in the fake, and the dates in this
  section.

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
- Packages follow the Technical Design's layers without underscores, because Google Checkstyle
  rejects them: `entity`, `usecase`, `interfaceadapter`, `dataaccess`, `view`. Interfaces shared by
  several use cases (such as `StockDataGateway`) go in `usecase.shared`.
- Money is stored as integer cents; never use `double` or `REAL` for money. Every movement of cash is a
  journal entry; [`docs/ledger.md`](docs/ledger.md) explains debits, credits and the ledger classes.

## How we test

- Run everything with `mvn clean verify`. This runs the JUnit 5 tests, writes the coverage report to `target/site/jacoco/index.html` and the style report to `target/checkstyle-result.xml`. CI uploads both on every PR.
- Copy the pattern in [`ExampleInteractorTest`](src/test/java/app/ExampleInteractorTest.java) when testing a use case:
  - an **in-memory DAO** instead of SQLite, so tests are fast and need no files;
  - a **fake presenter** that records what the interactor sent, so we assert on output data, not on the UI;
  - a **fixed `Clock`** passed into the interactor, so time-based logic gives the same result every run.
- Model actions as types (records and enums, e.g. `Side.BUY`, `OrderType.LIMIT`), never as strings that need parsing.
- Name tests after behaviour: `action_condition_expectedResult`, e.g. `placeOrder_zeroQuantity_presentsErrorAndSavesNothing`.
- Test both the success path and each failure path of every interactor.
- Tests must not call the real stock APIs; use `FakeStockDataGateway`. Provider parsing tests (SCRUM-31) can use the recorded JSON in `src/main/resources/fake-stock-data/` and `src/test/resources/api/`.

## UI conventions

All screens live in `view/` and use the shared kit in `view/ui/`.

- Call `Theme.install()` once at startup, before creating any Swing component.
- Use only `Theme` colours, fonts and `SPACE_*` constants. No hard-coded values.
- Use `Formatters` for all money and percentages. Gains and losses always show `+` or `−`, never colour alone.
- Use `LabeledField`, `PrimaryButton` and `AppTable` instead of raw Swing components.
- For P&L columns call `table.useSignedColumns(...)` so they are right-aligned and coloured green or red.
- Run anything slow (network or database) in a `SwingWorker`, never on the UI thread.
- Screens only call controllers and read view models. No business logic in `view/`.
- Run `view.ui.DemoScreen` from IntelliJ to see every component.

### Prompt for generating a screen

> Write a Swing `JPanel` for <screen name> in package `view`.
> Use `view.ui.Theme`, `Formatters`, `LabeledField`, `PrimaryButton` and `AppTable`.
> Do not hard-code colours, fonts or sizes; use `Theme` constants.
> Show money with `Formatters.money`, and signed values with `Formatters.signedMoney` and `Formatters.percent`.
> Call `useSignedColumns` on profit and loss columns.
> Read data from the view model and send user actions to the controller. Do not put business logic in the view.

## Wireframes

Every screen is sketched in [`docs/wireframes/`](docs/wireframes/), with a table of screens, user stories and owners. Start with the navigation map:

![Navigation map](docs/wireframes/00-navigation-map.svg)

## Screenshots

_Coming once the first screens are built._
