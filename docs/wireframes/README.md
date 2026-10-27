# Wireframes

One wireframe per screen of the Paper Trading App, drawn at the app's 1280×800 window size in the dark theme. They fix layout and navigation, not final colours or fonts. Each `.svg` has a matching PNG in [`png/`](png/).

Style rules used everywhere: one sans-serif font, a big "hero" number at the top, one line chart with 1D–ALL range toggles, a single accent colour that follows performance (green up, red down), and gains/losses always written with a `+` or `−` sign, never colour alone. Everything is buildable with Swing, FlatLaf and JFreeChart (panels, tables, buttons, text fields, toggle buttons, line and bar charts).

| # | Screen | User stories | Owner | What it shows |
|---|---|---|---|---|
| 00 | [Navigation map](00-navigation-map.svg) | all | whole team | Every screen and the click that moves you between them. |
| 01 | [Login / Sign up](01-login.svg) | US-01, US-02 | Efren Medina Arias | Log-in card and sign-up card, with a "username taken" error. |
| 02 | [Dashboard](02-dashboard.svg) | US-09 | Lifeable | Total value, today's change, chart, buying power, and the holdings + watchlist panel. |
| 03 | [Search](03-search.svg) | US-03 | Lifeable | Search field, results with price and "+ Watchlist", plus loading, no-match and offline states. |
| 04 | [Stock detail](04-stock-detail.svg) | US-04 | Andrei Mazin | Price, chart with range toggles, key stats, your position, Buy/Sell buttons. |
| 05 | [Trade ticket](05-trade-ticket.svg) | US-05, US-06, US-10 | Efren Medina Arias (buy), Abdulhadi Alnowailaty (sell), Andrei Mazin (limit) | Buy market, sell market, buy limit, and the review/confirm step. |
| 06 | [Open orders](06-open-orders.svg) | US-10 | Andrei Mazin, Abdulhadi Alnowailaty | Open limit orders with Cancel, the cancel dialog, and the automatic-fill notice. |
| 07 | [Portfolio](07-portfolio.svg) | US-07 | Abdulhadi Alnowailaty | Total value, summary cards, holdings with signed P/L, allocation bar. |
| 08 | [Trade history](08-trade-history.svg) | US-08 | Abdulhadi Alnowailaty | Filterable, paged list of filled trades. |
| 09 | [Account statement](09-account-statement.svg) | US-11 | Andrei Mazin | Opening/closing balance and every ledger entry with a running balance. |
| 10 | [Realized gains](10-realized-gains.svg) | US-12 | Efren Medina Arias | Gains and losses from completed sales, with a per-symbol bar chart. |

Owners come from the Jira assignee of each user story. All numbers are placeholder data, but they add up across screens (the portfolio, statement, history and realized gains all describe the same account).

To review: open the SVGs here on GitHub (they render inline) and leave comments on the PR.
