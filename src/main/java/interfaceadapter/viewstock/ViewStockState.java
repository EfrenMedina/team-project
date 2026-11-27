package interfaceadapter.viewstock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import usecase.viewstock.ChartRange;

/**
 * What the Stock detail screen shows. Every text is already formatted by the presenter, so the
 * screen only puts it on labels. Text that isn't known yet, such as the price before the first
 * load or after a failure, is an empty string, never null.
 *
 * @param ticker the stock's ticker, for example {@code "AAPL"}; used by the trade buttons
 * @param range the range the chart shows, and the range button that is selected
 * @param companyName the company's name, for example {@code "Apple Inc."}
 * @param exchangeLine the ticker and exchange, for example {@code "AAPL · NASDAQ"}
 * @param price the latest price, for example {@code "$344.63"}
 * @param change today's change in dollars and percent, for example {@code "+$2.57 (+1.12%)"}
 * @param changeUp whether today's change is zero or more, so the screen can colour it green
 *     rather than red
 * @param keyStats the labelled numbers under the chart, in the order they are shown
 * @param chart the chart's points, oldest first
 * @param error the message to show instead of the stock, or {@code null} for none
 */
public record ViewStockState(
    String ticker,
    ChartRange range,
    String companyName,
    String exchangeLine,
    String price,
    String change,
    boolean changeUp,
    List<KeyStat> keyStats,
    List<ChartPoint> chart,
    String error) {

  /** Nothing loaded yet and the 1M range selected, as shown before the first stock opens. */
  public static final ViewStockState EMPTY =
      new ViewStockState(
          "", ChartRange.ONE_MONTH, "", "", "", "", true, List.of(), List.of(), null);

  /**
   * Creates the state and keeps its own copies of the lists.
   *
   * @throws NullPointerException if any value except {@code error} is null
   */
  public ViewStockState {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(range, "range");
    Objects.requireNonNull(companyName, "companyName");
    Objects.requireNonNull(exchangeLine, "exchangeLine");
    Objects.requireNonNull(price, "price");
    Objects.requireNonNull(change, "change");
    keyStats = List.copyOf(keyStats);
    chart = List.copyOf(chart);
  }

  /**
   * One labelled number under the chart.
   *
   * @param label what the number is, for example {@code "Open"}
   * @param value the formatted number, for example {@code "$346.72"}
   */
  public record KeyStat(String label, String value) {

    /**
     * Creates the key stat.
     *
     * @throws NullPointerException if either value is null
     */
    public KeyStat {
      Objects.requireNonNull(label, "label");
      Objects.requireNonNull(value, "value");
    }
  }

  /**
   * One day on the chart.
   *
   * @param date the trading day
   * @param close the day's closing price in dollars
   */
  public record ChartPoint(LocalDate date, BigDecimal close) {

    /**
     * Creates the chart point.
     *
     * @throws NullPointerException if either value is null
     */
    public ChartPoint {
      Objects.requireNonNull(date, "date");
      Objects.requireNonNull(close, "close");
    }
  }
}
