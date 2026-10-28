# The ledger: how cash is recorded

Every movement of cash in the app is a **journal entry** in a double-entry ledger. We never
store a cash balance; we calculate it by adding up the entries. That way every dollar can be
traced back to the deposit, trade or fee that moved it.

## Debits and credits in plain words

Think of the ledger accounts as buckets. A journal entry moves value from one bucket to
another, so nothing appears or disappears.

- Each line of an entry is either a **debit** (left column) or a **credit** (right column).
- Debit and credit are just the names of the two columns. They do **not** mean plus and minus,
  or good and bad.
- **In every entry, total debits = total credits.** If they don't match, value came from
  nowhere, which is a bug. `JournalEntry` refuses to be created in that case.

Whether a debit makes a bucket bigger or smaller depends on the kind of bucket. That is the
account's **normal side**:

| Account | What it means | Kind | Grows with a |
|---|---|---|---|
| `CASH` | Money available to spend | Asset | Debit |
| `INVESTMENTS_AT_COST` | What we paid for the shares we still hold | Asset | Debit |
| `COMMISSION_EXPENSE` | Trading fees paid | Expense | Debit |
| `CONTRIBUTED_CAPITAL` | Money put into the account (the opening deposit) | Equity | Credit |
| `REALIZED_GAINS` | Profit locked in by selling (a loss is a debit) | Income | Credit |

Memory trick: **DEA-LER**. Dividends, Expenses and Assets grow with a debit; Liabilities,
Equity and Revenue grow with a credit.

## Worked example

### 1. Deposit $10,000

| Account | Debit | Credit |
|---|---:|---:|
| CASH | 10,000.00 | |
| CONTRIBUTED_CAPITAL | | 10,000.00 |

Cash: **$10,000.00**

### 2. Buy 10 AAPL at $150 with a $1 fee

The shares cost 10 × $150 = $1,500. Cash pays for the shares and the fee.

| Account | Debit | Credit |
|---|---:|---:|
| INVESTMENTS_AT_COST | 1,500.00 | |
| COMMISSION_EXPENSE | 1.00 | |
| CASH | | 1,501.00 |

Cash: 10,000 − 1,501 = **$8,499.00**

### 3. Sell 5 AAPL at $170 with a $1 fee

- Proceeds: 5 × $170 = $850
- Cost removed: 5 of the 10 shares, so 5/10 × $1,500 = $750
- Realized gain: 850 − 750 = $100 (the fee is recorded separately, not taken off the gain)
- Cash received: 850 − 1 = $849

| Account | Debit | Credit |
|---|---:|---:|
| CASH | 849.00 | |
| COMMISSION_EXPENSE | 1.00 | |
| INVESTMENTS_AT_COST | | 750.00 |
| REALIZED_GAINS | | 100.00 |

Cash: 8,499 + 849 = **$9,348.00**

A sale at a loss debits `REALIZED_GAINS` instead. For example, selling the 5 shares at $140
gives proceeds of $700 against a cost of $750, so `REALIZED_GAINS` is debited $50.

### Balances after all three entries

| Account | Balance |
|---|---:|
| CASH | 9,348.00 |
| INVESTMENTS_AT_COST | 750.00 |
| COMMISSION_EXPENSE | 2.00 |
| CONTRIBUTED_CAPITAL | 10,000.00 |
| REALIZED_GAINS | 100.00 |

Check: what we have (9,348 + 750 = 10,098) equals where it came from
(10,000 + 100 − 2 = 10,098).

## Using the classes

The ledger classes live in `entity/`.

- `Money`: an exact dollar amount (never a `double`). Create it with `Money.ofCents(15000)` or
  `Money.of("150.00")`.
- `LedgerAccount`: the five accounts above, each knowing its normal side.
- `EntrySide`: `DEBIT` or `CREDIT`.
- `LedgerLine`: one line, made of an account, a side and a positive amount.
- `JournalEntry`: id, accountId, description, postedAt and its lines.
- `UnbalancedEntryException`: thrown when an entry breaks the rules below.
- `LedgerBalance`: calculates an account's balance from a list of entries.

Rules the classes enforce:

- A line's amount must be greater than zero. The side gives the direction, so there are no
  negative amounts on a line.
- An entry needs at least 2 lines, and its debits must equal its credits. Otherwise the
  constructor throws `UnbalancedEntryException`.
- Entries can't be changed after they are created. To correct a mistake, post a new entry.
- Balances are calculated from a list of entries with `LedgerBalance.balanceOf(account, entries)`.
  A line on the account's normal side adds to the balance and a line on the other side subtracts,
  so balances come out positive (`CONTRIBUTED_CAPITAL` is 10,000.00 after the deposit) and a
  negative `REALIZED_GAINS` means a net loss. `Account` has no cash field.

The worked example above is tested in `LedgerBalanceTest`.

You will rarely build entries by hand: `JournalEntryFactory` (SCRUM-25) has one method per event
(`forOpeningDeposit`, `forBuy`, `forSell`) that produces the entries shown above.
