package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * A stock's latest price and today's trading range.
 *
 * <p>Limit orders use the day's high and low as well as the price: a buy limit fills when the
 * price or the day's low reaches the limit, so a dip between two price checks isn't missed.
 *
 * @param ticker the ticker symbol, for example {@code "AAPL"}; never blank
 * @param price the latest price; greater than zero
 * @param open today's opening price; greater than zero
 * @param high today's highest price; greater than zero and at least {@code low}
 * @param low today's lowest price; greater than zero
 * @param previousClose yesterday's closing price, which today's change is measured from;
 *     greater than zero
 * @param time when the price was last updated
 */
public record Quote(
    String ticker,
    Money price,
    Money open,
    Money high,
    Money low,
    Money previousClose,
    Instant time) {

  /**
   * Creates a quote.
   *
   * @throws NullPointerException if any component is null
   * @throws IllegalArgumentException if {@code ticker} is blank, any price is not greater than
   *     zero, or {@code high} is below {@code low}
   */
  public Quote {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(time, "time");
    if (ticker.isBlank()) {
      throw new IllegalArgumentException("Ticker must not be blank");
    }

    requirePositive(price, "price");
    requirePositive(open, "open");
    requirePositive(high, "high");
    requirePositive(low, "low");
    requirePositive(previousClose, "previousClose");
    if (high.compareTo(low) < 0) {
      throw new IllegalArgumentException("High " + high + " is below low " + low);
    }
  }

  /**
   * Returns how much the price has moved since yesterday's close.
   *
   * @return the price minus the previous close; negative if the price went down
   */
  public Money change() {
    return price.subtract(previousClose);
  }

  /**
   * Returns how much the price has moved since yesterday's close, as a percentage.
   *
   * @return the change divided by the previous close, times 100, rounded to two decimal places;
   *     for example {@code 1.48} for a 1.48% rise or {@code -0.16} for a 0.16% fall
   */
  public BigDecimal changePercent() {
    // Multiply as a BigDecimal: a long times 100 would overflow for very large prices
    return BigDecimal.valueOf(change().toCents())
        .movePointRight(2)
        .divide(BigDecimal.valueOf(previousClose.toCents()), 2, RoundingMode.HALF_EVEN);
  }

  private static void requirePositive(Money amount, String name) {
    Objects.requireNonNull(amount, name);
    if (!amount.isPositive()) {
      throw new IllegalArgumentException(name + " must be greater than zero: " + amount);
    }
  }
}
