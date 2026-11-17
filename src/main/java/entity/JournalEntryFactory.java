package entity;

import java.time.Instant;
import java.util.List;

/**
 * Standard recipes for journal entries, so nobody writes debits and credits by hand.
 *
 * <p>Placeholder written for SCRUM-26 with only the opening deposit, which sign-up needs.
 * SCRUM-25 owns this class and adds {@code forBuy} and {@code forSell}, and may change this
 * method as long as sign-up is updated with it.
 */
public final class JournalEntryFactory {

  private JournalEntryFactory() {
  }

  /**
   * Creates the entry for money put into an account: debit Cash, credit Contributed capital.
   *
   * @param entryId unique id of the new entry
   * @param accountId the trading account receiving the money
   * @param postedAt when the deposit happened
   * @param amount how much was deposited; must be greater than zero
   * @return a balanced two-line entry
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if {@code amount} is not greater than zero
   */
  public static JournalEntry forOpeningDeposit(
      String entryId, String accountId, Instant postedAt, Money amount) {
    return new JournalEntry(entryId, accountId, "Opening deposit", postedAt, List.of(
        new LedgerLine(LedgerAccount.CASH, EntrySide.DEBIT, amount),
        new LedgerLine(LedgerAccount.CONTRIBUTED_CAPITAL, EntrySide.CREDIT, amount)));
  }
}
