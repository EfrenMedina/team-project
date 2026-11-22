package usecase.viewstock;

import entity.CompanyProfile;
import entity.PricePoint;
import entity.Quote;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import usecase.shared.StockDataGateway;
import usecase.shared.StockDataUnavailableException;

/**
 * Loads everything the Stock detail screen shows for one stock: its latest quote, its company
 * profile and its daily prices for the chosen range. Choosing another range runs it again.
 */
public class ViewStockInteractor implements ViewStockInputBoundary {

  /** Shown when the ticker is missing. */
  static final String BLANK_TICKER_MESSAGE = "Enter a ticker.";

  /**
   * Shown when the stock data can't be fetched. The wording comes from the Technical Design
   * (p. 15). An unknown ticker gets it too, because the gateway can't tell the two apart.
   */
  static final String UNAVAILABLE_MESSAGE = "Prices are unavailable right now.";

  /** Where the quote, profile and history come from. */
  private final StockDataGateway stockDataGateway;

  /** Receives the result. */
  private final ViewStockOutputBoundary presenter;

  /** Decides which day "today" is, so the chart's ranges end on it. */
  private final Clock clock;

  /**
   * Creates the interactor.
   *
   * @param stockDataGateway where the quote, profile and history come from
   * @param presenter receives the result
   * @param clock decides which day "today" is. The app should pass a clock in the
   *     {@code America/New_York} zone, the zone of the US market's trading days, and in
   *     offline mode a clock fixed to the fake's last recorded day
   * @throws NullPointerException if any argument is null
   */
  public ViewStockInteractor(
      StockDataGateway stockDataGateway, ViewStockOutputBoundary presenter, Clock clock) {
    this.stockDataGateway = Objects.requireNonNull(stockDataGateway, "stockDataGateway");
    this.presenter = Objects.requireNonNull(presenter, "presenter");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Loads the stock and reports it to the presenter. Nothing is shown unless all three parts
   * load: a page with a price but no company name, or a chart but no price, would be
   * misleading.
   *
   * @param inputData which stock to show and how far back its chart goes
   */
  @Override
  public void execute(ViewStockInputData inputData) {
    ChartRange range = inputData.range();
    if (inputData.ticker() == null || inputData.ticker().isBlank()) {
      presenter.prepareFailView(inputData.ticker(), range, BLANK_TICKER_MESSAGE);
      return;
    }
    String ticker = inputData.ticker().strip().toUpperCase(Locale.ROOT);
    LocalDate today = LocalDate.now(clock);

    ViewStockOutputData outputData;
    try {
      Quote quote = stockDataGateway.getQuote(ticker);
      CompanyProfile profile = stockDataGateway.getProfile(ticker);
      List<PricePoint> history =
          stockDataGateway.getDailyHistory(ticker, range.startDate(today), today);
      outputData = new ViewStockOutputData(quote, profile, range, history);
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(ticker, range, UNAVAILABLE_MESSAGE);
      return;
    }
    presenter.prepareSuccessView(outputData);
  }
}
