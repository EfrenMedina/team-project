package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An exact amount of money in US dollars, always stored with two decimal places.
 *
 * <p>Money is immutable: every operation returns a new {@code Money} and never changes the
 * original. Amounts are held as a {@link java.math.BigDecimal}, never a {@code double}, so
 * arithmetic is exact (for example, $0.10 + $0.20 is exactly $0.30). Any rounding uses
 * {@link java.math.RoundingMode#HALF_EVEN}.
 *
 * <p>Values may be negative (for example, a balance after a net loss). Rules such as "a ledger
 * line amount must be positive" belong to the classes that use {@code Money}, not here.
 */
public final class Money implements Comparable<Money> {

  /** Zero dollars. Useful as the starting value when adding up amounts. */
  public static final Money ZERO = ofCents(0);

  private final BigDecimal amount;

  private Money(BigDecimal amount) {
    Objects.requireNonNull(amount, "amount");
    this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
  }

  /**
   * Creates an amount from a whole number of cents.
   *
   * @param cents the amount in cents, for example {@code 15000} for $150.00; may be negative
   * @return the amount
   */
  public static Money ofCents(long cents) {
    return new Money(BigDecimal.valueOf(cents, 2));
  }

  /**
   * Creates an amount from a decimal string such as {@code "150.00"}.
   *
   * @param amount the decimal amount, with at most two decimal places
   * @return the amount
   * @throws IllegalArgumentException if {@code amount} is null, not a number, or has more than
   *     two decimal places
   */
  public static Money of(String amount) {
    if (amount == null || amount.trim().isEmpty()) {
      throw new IllegalArgumentException("Not a valid amount: " + amount);
    }
    try {
      BigDecimal converted = new BigDecimal(amount);
      if (converted.stripTrailingZeros().scale() > 2) {
        throw new IllegalArgumentException("Not a valid amount: " + amount);
      }
      return new Money(converted);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Not a valid amount: " + amount, e);
    }
  }

  /**
   * Returns this amount as a whole number of cents, for storage.
   *
   * @return the amount in cents, for example {@code 15000} for $150.00
   */
  public long toCents() {
    return this.amount.movePointRight(2).longValueExact();
  }

  /**
   * Returns the sum of this amount and {@code other}.
   *
   * @param other the amount to add
   * @return a new amount; this one is unchanged
   */
  public Money add(Money other) {
    Objects.requireNonNull(other, "other");
    return new Money(this.amount.add(other.amount));
  }

  /**
   * Returns this amount minus {@code other}. The result may be negative.
   *
   * @param other the amount to subtract
   * @return a new amount; this one is unchanged
   */
  public Money subtract(Money other) {
    Objects.requireNonNull(other, "other");
    return new Money(this.amount.subtract(other.amount));
  }

  /**
   * Returns this amount multiplied by a whole number, for example a price times a quantity.
   *
   * @param factor the multiplier, for example a number of shares
   * @return a new amount; this one is unchanged
   */
  public Money multiply(int factor) {
    return new Money(this.amount.multiply(new BigDecimal(factor)));
  }

  /**
   * Returns whether this amount is greater than zero.
   *
   * @return true if this amount is more than $0.00
   */
  public boolean isPositive() {
    return this.amount.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Returns whether this amount is less than zero.
   *
   * @return true if this amount is less than $0.00
   */
  public boolean isNegative() {
    return this.amount.compareTo(BigDecimal.ZERO) < 0;
  }

  /**
   * Returns whether this amount is exactly zero.
   *
   * @return true if this amount is $0.00
   */
  public boolean isZero() {
    return this.amount.compareTo(BigDecimal.ZERO) == 0;
  }

  /**
   * Compares two amounts by value.
   *
   * @param other the amount to compare with
   * @return a negative number, zero or a positive number if this amount is less than, equal to
   *     or greater than {@code other}
   */
  @Override
  public int compareTo(Money other) {
    return this.amount.compareTo(other.amount);
  }

  /**
   * Two amounts are equal when they represent the same number of cents.
   *
   * @param o the object to compare with
   * @return true if {@code o} is a {@code Money} with the same amount
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money)) {
      return false;
    }
    return amount.equals(((Money) o).amount);
  }

  @Override
  public int hashCode() {
    return this.amount.hashCode();
  }

  /**
   * Returns the amount as a plain decimal, for example {@code "150.00"}. For debugging only.
   *
   * @return the amount as a string
   */
  @Override
  public String toString() {
    return this.amount.toPlainString();
  }
}
