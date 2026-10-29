package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Uses the worked example from docs/ledger.md: deposit $10,000, buy 10 AAPL, sell 5 AAPL. */
class LedgerBalanceTest {

  private static final Instant POSTED_AT = Instant.parse("2026-10-28T12:00:00Z");

  @Test
  void balanceOf_noEntries_isZero() {
    // Inputs
    List<JournalEntry> entries = List.of();

    // Action
    Money balance = LedgerBalance.balanceOf(LedgerAccount.CASH, entries);

    // Assertion
    assertEquals(Money.ZERO, balance);
  }

  @Test
  void balanceOf_cashAfterDepositAndBuy_is8499() {
    // Inputs
    List<JournalEntry> entries = List.of(deposit(), buy());

    // Action
    Money balance = LedgerBalance.balanceOf(LedgerAccount.CASH, entries);

    // Assertion
    assertEquals(Money.of("8499.00"), balance);
  }

  @Test
  void balanceOf_cashAfterSell_is9348() {
    // Inputs
    List<JournalEntry> entries = List.of(deposit(), buy(), sellAtGain());

    // Action
    Money balance = LedgerBalance.balanceOf(LedgerAccount.CASH, entries);

    // Assertion
    assertEquals(Money.of("9348.00"), balance);
  }

  @Test
  void balanceOf_creditNormalAccount_isPositive() {
    // Inputs
    List<JournalEntry> entries = List.of(deposit());

    // Action
    Money balance = LedgerBalance.balanceOf(LedgerAccount.CONTRIBUTED_CAPITAL, entries);

    // Assertion
    assertEquals(Money.of("10000.00"), balance);
  }

  @Test
  void balanceOf_allAccountsAfterThreeEntries_matchDoc() {
    // Inputs
    List<JournalEntry> entries = List.of(deposit(), buy(), sellAtGain());

    // Action / Assertion
    assertEquals(Money.of("9348.00"), balance(LedgerAccount.CASH, entries));
    assertEquals(Money.of("750.00"), balance(LedgerAccount.INVESTMENTS_AT_COST, entries));
    assertEquals(Money.of("2.00"), balance(LedgerAccount.COMMISSION_EXPENSE, entries));
    assertEquals(Money.of("10000.00"), balance(LedgerAccount.CONTRIBUTED_CAPITAL, entries));
    assertEquals(Money.of("100.00"), balance(LedgerAccount.REALIZED_GAINS, entries));
  }

  @Test
  void balanceOf_saleAtLoss_realizedGainsIsNegative() {
    // Inputs
    List<JournalEntry> entries = List.of(deposit(), buy(), sellAtLoss());

    // Action
    Money gains = LedgerBalance.balanceOf(LedgerAccount.REALIZED_GAINS, entries);

    // Assertion
    assertEquals(Money.of("-50.00"), gains);
  }

  /** Deposit $10,000. */
  private static JournalEntry deposit() {
    return entry(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 1_000_000),
        line(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, 1_000_000));
  }

  /** Buy 10 AAPL at $150 with a $1 fee. */
  private static JournalEntry buy() {
    return entry(
        line(LedgerAccount.INVESTMENTS_AT_COST, EntrySide.DEBIT, 150_000),
        line(LedgerAccount.COMMISSION_EXPENSE, EntrySide.DEBIT, 100),
        line(LedgerAccount.CASH, EntrySide.CREDIT, 150_100));
  }

  /** Sell 5 AAPL at $170 with a $1 fee: $100 gain. */
  private static JournalEntry sellAtGain() {
    return entry(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 84_900),
        line(LedgerAccount.COMMISSION_EXPENSE, EntrySide.DEBIT, 100),
        line(LedgerAccount.INVESTMENTS_AT_COST, EntrySide.CREDIT, 75_000),
        line(LedgerAccount.REALIZED_GAINS, EntrySide.CREDIT, 10_000));
  }

  /** Sell 5 AAPL at $140 with a $1 fee: $50 loss. */
  private static JournalEntry sellAtLoss() {
    return entry(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 69_900),
        line(LedgerAccount.COMMISSION_EXPENSE, EntrySide.DEBIT, 100),
        line(LedgerAccount.REALIZED_GAINS, EntrySide.DEBIT, 5_000),
        line(LedgerAccount.INVESTMENTS_AT_COST, EntrySide.CREDIT, 75_000));
  }

  private static Money balance(LedgerAccount account, List<JournalEntry> entries) {
    return LedgerBalance.balanceOf(account, entries);
  }

  private static LedgerLine line(LedgerAccount account, EntrySide side, long cents) {
    return new LedgerLine(account, side, Money.ofCents(cents));
  }

  private static JournalEntry entry(LedgerLine... lines) {
    return new JournalEntry("entry-1", "account-1", "Test entry", POSTED_AT, List.of(lines));
  }
}
