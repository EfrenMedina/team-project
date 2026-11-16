package entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One trading day of a stock's price history, used to draw its chart.
 *
 * @param date the trading day
 * @param open the first price that day; greater than zero
 * @param high the highest price that day; greater than zero and at least {@code low}
 * @param low the lowest price that day; greater than zero
 * @param close the last price that day; greater than zero
 * @param volume how many shares changed hands that day; never negative
 */
public record PricePoint(
    LocalDate date, Money open, Money high, Money low, Money close, long volume) {

  /**
   * Creates a price point.
   *
   * @throws NullPointerException if any component is null
   * @throws IllegalArgumentException if any price is not greater than zero, {@code high} is
   *     below {@code low}, or {@code volume} is negative
   */
  public PricePoint {
    Objects.requireNonNull(date, "date");
    requirePositive(open, "open");
    requirePositive(high, "high");
    requirePositive(low, "low");
    requirePositive(close, "close");

    if (high.compareTo(low) < 0) {
      throw new IllegalArgumentException("High " + high + " is below low " + low);
    }
    if (volume < 0) {
      throw new IllegalArgumentException("Volume must not be negative: " + volume);
    }
  }

  private static void requirePositive(Money amount, String name) {
    Objects.requireNonNull(amount, name);
    if (!amount.isPositive()) {
      throw new IllegalArgumentException(name + " must be greater than zero: " + amount);
    }
  }
}
