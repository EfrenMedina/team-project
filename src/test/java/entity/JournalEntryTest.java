package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class JournalEntryTest {

  private static final Instant POSTED_AT = Instant.parse("2026-10-28T12:00:00Z");

  @Test
  void constructor_balancedDeposit_isCreated() {
    // Inputs
    List<LedgerLine> lines = List.of(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 1_000_000),
        line(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, 1_000_000));

    // Action
    JournalEntry entry = entry(lines);

    // Assertion
    assertEquals(2, entry.lines().size());
  }

  @Test
  void constructor_threeLineBuy_isCreated() {
    // Inputs
    List<LedgerLine> lines = List.of(
        line(LedgerAccount.INVESTMENTS_AT_COST, EntrySide.DEBIT, 150_000),
        line(LedgerAccount.COMMISSION_EXPENSE, EntrySide.DEBIT, 100),
        line(LedgerAccount.CASH, EntrySide.CREDIT, 150_100));

    // Action
    JournalEntry entry = entry(lines);

    // Assertion
    assertEquals(3, entry.lines().size());
  }

  @Test
  void constructor_unbalancedLines_throws() {
    // Inputs
    List<LedgerLine> lines = List.of(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 100),
        line(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, 99));

    // Action / Assertion
    assertThrows(UnbalancedEntryException.class, () -> entry(lines));
  }

  @Test
  void constructor_oneLine_throws() {
    // Inputs
    List<LedgerLine> lines = List.of(line(LedgerAccount.CASH, EntrySide.DEBIT, 100));

    // Action / Assertion
    assertThrows(UnbalancedEntryException.class, () -> entry(lines));
  }

  @Test
  void constructor_noLines_throws() {
    // Inputs
    List<LedgerLine> lines = List.of();

    // Action / Assertion
    assertThrows(UnbalancedEntryException.class, () -> entry(lines));
  }

  @Test
  void lines_afterCreation_cannotBeModified() {
    // Inputs
    JournalEntry entry = entry(List.of(
        line(LedgerAccount.CASH, EntrySide.DEBIT, 100),
        line(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, 100)));
    LedgerLine extra = line(LedgerAccount.CASH, EntrySide.DEBIT, 50);

    // Action / Assertion
    assertThrows(UnsupportedOperationException.class, () -> entry.lines().add(extra));
  }

  @Test
  void lines_callerChangesOriginalList_entryUnchanged() {
    // Inputs
    List<LedgerLine> lines = new ArrayList<>();
    lines.add(line(LedgerAccount.CASH, EntrySide.DEBIT, 100));
    lines.add(line(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, 100));
    JournalEntry entry = entry(lines);

    // Action
    lines.add(line(LedgerAccount.CASH, EntrySide.DEBIT, 50));

    // Assertion
    assertEquals(2, entry.lines().size());
  }

  private static LedgerLine line(LedgerAccount account, EntrySide side, long cents) {
    return new LedgerLine(account, side, Money.ofCents(cents));
  }

  private static JournalEntry entry(List<LedgerLine> lines) {
    return new JournalEntry("entry-1", "account-1", "Test entry", POSTED_AT, lines);
  }
}
