package entity;

import java.util.List;
import java.util.Objects;

/**
 * Calculates account balances by adding up journal entries. The ledger never stores a
 * balance; it is always calculated from the entries.
 */
public final class LedgerBalance {

  private LedgerBalance() {}

  /**
   * Returns the balance of {@code account} after all {@code entries}.
   *
   * <p>A line on the account's normal side increases the balance; a line on the other side
   * decreases it. So a balance is positive when the account has grown, for example
   * CONTRIBUTED_CAPITAL is 10000.00 after the opening deposit, not -10000.00. A negative
   * REALIZED_GAINS means a net loss.
   *
   * @param account the account to calculate
   * @param entries the entries to add up, in any order
   * @return the balance; {@link Money#ZERO} if no line touches the account
   */
  public static Money balanceOf(LedgerAccount account, List<JournalEntry> entries) {
    Objects.requireNonNull(account, "account");
    Objects.requireNonNull(entries, "entries");

    Money balance = Money.ZERO;
    for (JournalEntry entry : entries) {
      for (LedgerLine line : entry.lines()) {
        if (line.account() == account) {
          if (line.side() == account.getNormalSide()) {
            balance = balance.add(line.amount());
          } else {
            balance = balance.subtract(line.amount());
          }
        }
      }
    }
    return balance;
  }
}
