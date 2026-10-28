package entity;

import java.util.Objects;

/**
 * One line of a journal entry: an account, a side and a positive amount.
 * The side gives the direction, so the amount is never zero or negative.
 *
 * @param account the account this line affects
 * @param side whether the line is a debit or a credit
 * @param amount how much; must be greater than zero
 */
public record LedgerLine(LedgerAccount account, EntrySide side, Money amount) {

  public LedgerLine {
    Objects.requireNonNull(account, "account");
    Objects.requireNonNull(side, "side");
    Objects.requireNonNull(amount, "amount");

    if (!amount.isPositive()) {
      throw new IllegalArgumentException("Line amount must be positive: " + amount);
    }
  }
}
