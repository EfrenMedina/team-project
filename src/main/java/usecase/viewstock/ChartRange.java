package usecase.viewstock;

import java.time.LocalDate;
import java.util.Objects;

/** How far back the Stock detail chart goes. */
public enum ChartRange {
  ONE_MONTH(1),
  SIX_MONTHS(6),
  ONE_YEAR(12);

  /** The number of calendar months the range covers. */
  private final int months;

  ChartRange(int months) {
    this.months = months;
  }

  /**
   * Returns the first day of the range that ends on {@code today}. The chart shows every trading
   * day from this date to {@code today}, both included. At the end of a month the start moves
   * back to the last day of the shorter month, so one month before March 31 is February 28.
   *
   * @param today the last day of the range
   * @return the same day of the month, this many months before {@code today}
   * @throws NullPointerException if {@code today} is null
   */
  public LocalDate startDate(LocalDate today) {
    return Objects.requireNonNull(today, "today").minusMonths(months);
  }
}
