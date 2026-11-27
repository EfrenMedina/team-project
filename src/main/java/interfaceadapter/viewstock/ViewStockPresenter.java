package interfaceadapter.viewstock;

import entity.Money;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import interfaceadapter.viewstock.ViewStockState.ChartPoint;
import interfaceadapter.viewstock.ViewStockState.KeyStat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.viewstock.ChartRange;
import usecase.viewstock.ViewStockOutputBoundary;
import usecase.viewstock.ViewStockOutputData;

/**
 * Turns the view-stock use case's result into what the Stock detail screen shows: formatted
 * prices, today's signed change, the key stats and the chart's points, or an error message.
 *
 * <p>Signs follow the shared UI kit's rules (SCRUM-22): a gain starts with {@code +}, a loss
 * with the true minus sign {@code −}, and zero has no sign. The kit lives in the view layer,
 * which a presenter may not use, so the rules are repeated here.
 */
public class ViewStockPresenter implements ViewStockOutputBoundary {

  /** The true minus sign, used for losses. */
  static final String MINUS = "−";

  /** Shown for a market cap the provider doesn't know, which it reports as zero. */
  static final String UNKNOWN = "—";

  /** One trillion, one billion and one million dollars, for short market caps. */
  private static final BigDecimal TRILLION = new BigDecimal("1e12");
  private static final BigDecimal BILLION = new BigDecimal("1e9");
  private static final BigDecimal MILLION = new BigDecimal("1e6");

  /** The Stock detail screen's state, which this presenter updates. */
  private final ViewStockViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel the Stock detail screen's view model
   * @throws NullPointerException if {@code viewModel} is null
   */
  public ViewStockPresenter(ViewStockViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
  }

  /**
   * Shows the stock: its name, exchange, price, today's change, key stats and chart.
   *
   * @param outputData everything the screen shows for the stock
   */
  @Override
  public void prepareSuccessView(ViewStockOutputData outputData) {
    Quote quote = outputData.quote();
    Stock stock = outputData.profile().stock();
    Money change = quote.change();
    viewModel.setState(
        new ViewStockState(
            stock.ticker(),
            outputData.range(),
            stock.name(),
            stock.ticker() + " · " + stock.exchange(),
            money(quote.price()),
            signedMoney(change) + " (" + signedPercent(quote.changePercent()) + ")",
            !change.isNegative(),
            List.of(
                new KeyStat("Open", money(quote.open())),
                new KeyStat("High", money(quote.high())),
                new KeyStat("Low", money(quote.low())),
                new KeyStat("Prev close", money(quote.previousClose())),
                new KeyStat("Market cap", marketCap(outputData.profile().marketCap()))),
            outputData.history().stream().map(ViewStockPresenter::chartPoint).toList(),
            null));
  }

  /**
   * Shows the error with the ticker and range that were asked for, and clears the old stock,
   * so another stock's numbers are never shown under this ticker.
   *
   * @param ticker the ticker that was asked for; blank or null if none was given
   * @param range the range that was asked for
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String ticker, ChartRange range, String error) {
    viewModel.setState(
        new ViewStockState(
            ticker == null ? "" : ticker, range, "", "", "", "", true, List.of(), List.of(),
            error));
  }

  /**
   * Formats an amount as US dollars, for example {@code "$1,234.56"} or {@code "−$1,234.56"}.
   *
   * @param amount the amount
   * @return the amount with a sign for losses only, a dollar sign, separators and two decimals
   */
  static String money(Money amount) {
    String digits =
        NumberFormat.getCurrencyInstance(Locale.US).format(dollars(amount).abs());
    return amount.isNegative() ? MINUS + digits : digits;
  }

  /**
   * Formats a change in dollars, for example {@code "+$2.57"}, {@code "−$4.28"} or
   * {@code "$0.00"}.
   *
   * @param amount the change
   * @return the change with {@code +} for a gain, {@code −} for a loss and no sign for zero
   */
  static String signedMoney(Money amount) {
    return amount.isPositive() ? "+" + money(amount) : money(amount);
  }

  /**
   * Formats a change in percent, for example {@code "+1.12%"}, {@code "−1.23%"} or
   * {@code "0.00%"}.
   *
   * @param percent the change in percent, so {@code 1.12} means 1.12%
   * @return the change with two decimals, {@code +} for a gain, {@code −} for a loss and no
   *     sign for zero
   */
  static String signedPercent(BigDecimal percent) {
    BigDecimal rounded = percent.setScale(2, RoundingMode.HALF_EVEN);
    String digits = rounded.abs().toPlainString() + "%";
    if (rounded.signum() > 0) {
      return "+" + digits;
    }
    return rounded.signum() < 0 ? MINUS + digits : digits;
  }

  /**
   * Formats a market cap in short form, as on the wireframe: {@code "$5.03T"}, {@code "$812.40B"}
   * or {@code "$95.10M"}. Smaller amounts are written in full.
   *
   * @param marketCap the market cap; zero means the provider doesn't know it
   * @return the short form, or {@value #UNKNOWN} for zero
   */
  static String marketCap(Money marketCap) {
    if (marketCap.isZero()) {
      return UNKNOWN;
    }
    BigDecimal value = dollars(marketCap);
    if (value.compareTo(TRILLION) >= 0) {
      return shortForm(value, TRILLION, "T");
    }
    if (value.compareTo(BILLION) >= 0) {
      return shortForm(value, BILLION, "B");
    }
    if (value.compareTo(MILLION) >= 0) {
      return shortForm(value, MILLION, "M");
    }
    return money(marketCap);
  }

  private static String shortForm(BigDecimal value, BigDecimal unit, String suffix) {
    return "$" + value.divide(unit, 2, RoundingMode.HALF_EVEN).toPlainString() + suffix;
  }

  private static ChartPoint chartPoint(PricePoint point) {
    return new ChartPoint(point.date(), dollars(point.close()));
  }

  private static BigDecimal dollars(Money amount) {
    return BigDecimal.valueOf(amount.toCents(), 2);
  }
}
