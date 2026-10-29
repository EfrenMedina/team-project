package entity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * One event in the ledger (a deposit, buy or sell), made of two or more lines whose debits
 * must equal their credits. Entries can't be changed after creation. To correct one, a new
 * entry must be made.
 *
 * @param id unique id of this entry
 * @param accountId the trading account this entry belongs to
 * @param description what happened, for example "Buy 10 AAPL"
 * @param postedAt when the entry was recorded
 * @param lines the lines; copied, so later changes to the caller's list have no effect
 */
public record JournalEntry(String id, String accountId, String description,
    Instant postedAt, List<LedgerLine> lines) {

  /**
   * Creates an entry, copying the lines.
   *
   * @throws NullPointerException if any component or line is null
   * @throws UnbalancedEntryException if there are fewer than 2 lines or debits don't equal
   *     credits
   */
  public JournalEntry {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(accountId, "accountId");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(postedAt, "postedAt");
    Objects.requireNonNull(lines, "lines");

    // Copy so the caller can't change our lines later.
    lines = List.copyOf(lines);

    if (lines.size() < 2) {
      throw new UnbalancedEntryException("An entry needs at least 2 lines, got " + lines.size());
    }

    Money debits = total(lines, EntrySide.DEBIT);
    Money credits = total(lines, EntrySide.CREDIT);
    if (!debits.equals(credits)) {
      throw new UnbalancedEntryException(
          "Debits " + debits + " don't equal credits " + credits);
    }
  }

  private static Money total(List<LedgerLine> lines, EntrySide side) {
    Money sum = Money.ZERO;
    for (LedgerLine line : lines) {
      if (line.side() == side) {
        sum = sum.add(line.amount());
      }
    }
    return sum;
  }
}
