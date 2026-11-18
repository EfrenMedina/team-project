package view.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Formats money and percentages with explicit signs. */
public final class Formatters {
  /** The true minus sign, used for negative values. */
  public static final String MINUS = "−"; // −

  private Formatters() {
  }

  /**
   * $1,234.56 (negatives: −$1,234.56). Input is integer cents.
   */
  public static String money(long cents) {
    String s = NumberFormat.getCurrencyInstance(Locale.US)
        .format(BigDecimal.valueOf(Math.abs(cents), 2));
    return cents < 0 ? MINUS + s : s;
  }

  /**
   * Always signed: +$200.00, −$50.00, $0.00.
   */
  public static String signedMoney(long cents) {
    return cents > 0 ? "+" + money(cents) : money(cents);
  }

  /**
   * Always signed: +1.25%, −0.50%, 0.00%. Input is a fraction (0.0125 = 1.25%).
   */
  public static String percent(double fraction) {
    BigDecimal pct = BigDecimal.valueOf(fraction * 100).setScale(2, RoundingMode.HALF_UP);
    String body = pct.abs().toPlainString() + "%";
    return pct.signum() > 0 ? "+" + body : pct.signum() < 0 ? MINUS + body : body;
  }
}