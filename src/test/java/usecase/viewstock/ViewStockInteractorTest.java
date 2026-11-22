package usecase.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dataaccess.FakeStockDataGateway;
import entity.CompanyProfile;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import usecase.shared.StockDataGateway;
import usecase.shared.StockDataUnavailableException;

class ViewStockInteractorTest {

  private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

  /** The fake's last recorded day, as offline mode will use it. */
  private static final LocalDate TODAY = FakeStockDataGateway.LAST_RECORDED_DAY;

  private FakeStockDataGateway fake;
  private SwitchableGateway gateway;
  private RecordingPresenter presenter;
  private ViewStockInteractor interactor;

  @BeforeEach
  void setUp() {
    fake = new FakeStockDataGateway();
    gateway = new SwitchableGateway(fake);
    presenter = new RecordingPresenter();
    interactor = new ViewStockInteractor(gateway, presenter, clockOn(TODAY));
  }

  @Test
  void execute_knownTicker_presentsQuoteProfileAndHistory()
      throws StockDataUnavailableException {
    // Action
    interactor.execute(new ViewStockInputData("AAPL", ChartRange.ONE_MONTH));

    // Assertion
    ViewStockOutputData output = presenter.success;
    assertEquals(fake.getQuote("AAPL"), output.quote());
    assertEquals(new Stock("AAPL", "Apple Inc.", "NASDAQ"), output.profile().stock());
    assertEquals(ChartRange.ONE_MONTH, output.range());
    assertEquals(LocalDate.of(2026, 9, 30), output.history().get(0).date());
    assertEquals(TODAY, output.history().get(output.history().size() - 1).date());
    assertNull(presenter.error);
  }

  @ParameterizedTest
  @EnumSource(ChartRange.class)
  void execute_eachRange_presentsHistoryFromRangeStartToToday(ChartRange range)
      throws StockDataUnavailableException {
    // Action
    interactor.execute(new ViewStockInputData("MSFT", range));

    // Assertion
    assertEquals(
        fake.getDailyHistory("MSFT", range.startDate(TODAY), TODAY), presenter.success.history());
    assertEquals(range, presenter.success.range());
  }

  @Test
  void execute_lowercaseTickerWithSpaces_trimsAndUpperCases() {
    // Action
    interactor.execute(new ViewStockInputData("  aapl ", ChartRange.ONE_MONTH));

    // Assertion
    assertEquals("AAPL", presenter.success.quote().ticker());
    assertEquals(Set.of("AAPL"), gateway.tickersAskedFor);
  }

  @Test
  void execute_clockOnEarlierDay_endsHistoryOnThatDay() {
    // Inputs: Friday 2026-10-23
    LocalDate friday = LocalDate.of(2026, 10, 23);
    interactor = new ViewStockInteractor(gateway, presenter, clockOn(friday));

    // Action
    interactor.execute(new ViewStockInputData("AAPL", ChartRange.ONE_MONTH));

    // Assertion
    List<PricePoint> history = presenter.success.history();
    assertEquals(LocalDate.of(2026, 9, 23), history.get(0).date());
    assertEquals(friday, history.get(history.size() - 1).date());
  }

  @Test
  void execute_blankTicker_presentsErrorWithoutAskingGateway() {
    // Action
    interactor.execute(new ViewStockInputData("   ", ChartRange.SIX_MONTHS));

    // Assertion
    assertFailedWith("   ", ChartRange.SIX_MONTHS, ViewStockInteractor.BLANK_TICKER_MESSAGE);
    assertEquals(0, gateway.calls);
  }

  @Test
  void execute_nullTicker_presentsErrorWithoutAskingGateway() {
    // Action
    interactor.execute(new ViewStockInputData(null, ChartRange.ONE_MONTH));

    // Assertion
    assertFailedWith(null, ChartRange.ONE_MONTH, ViewStockInteractor.BLANK_TICKER_MESSAGE);
    assertEquals(0, gateway.calls);
  }

  @Test
  void execute_quoteUnavailable_presentsUnavailableError() {
    // Inputs
    gateway.failing.add(Method.QUOTE);

    // Action
    interactor.execute(new ViewStockInputData("aapl", ChartRange.ONE_YEAR));

    // Assertion: the ticker comes back cleaned up
    assertFailedWith("AAPL", ChartRange.ONE_YEAR, ViewStockInteractor.UNAVAILABLE_MESSAGE);
  }

  @Test
  void execute_profileUnavailable_presentsUnavailableError() {
    // Inputs
    gateway.failing.add(Method.PROFILE);

    // Action
    interactor.execute(new ViewStockInputData("AAPL", ChartRange.ONE_MONTH));

    // Assertion
    assertFailedWith("AAPL", ChartRange.ONE_MONTH, ViewStockInteractor.UNAVAILABLE_MESSAGE);
  }

  @Test
  void execute_historyUnavailable_presentsUnavailableError() {
    // Inputs
    gateway.failing.add(Method.HISTORY);

    // Action
    interactor.execute(new ViewStockInputData("AAPL", ChartRange.ONE_MONTH));

    // Assertion: no price without a chart either
    assertFailedWith("AAPL", ChartRange.ONE_MONTH, ViewStockInteractor.UNAVAILABLE_MESSAGE);
  }

  @Test
  void execute_unknownTicker_presentsUnavailableError() {
    // Action: the fake has no data for XYZQ
    interactor.execute(new ViewStockInputData("XYZQ", ChartRange.ONE_MONTH));

    // Assertion
    assertFailedWith("XYZQ", ChartRange.ONE_MONTH, ViewStockInteractor.UNAVAILABLE_MESSAGE);
  }

  @Test
  void constructor_nullArgument_throws() {
    // Inputs
    Clock clock = clockOn(TODAY);

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new ViewStockInteractor(null, presenter, clock));
    assertThrows(NullPointerException.class,
        () -> new ViewStockInteractor(gateway, null, clock));
    assertThrows(NullPointerException.class,
        () -> new ViewStockInteractor(gateway, presenter, null));
  }

  private void assertFailedWith(String ticker, ChartRange range, String error) {
    assertEquals(ticker, presenter.failedTicker);
    assertEquals(range, presenter.failedRange);
    assertEquals(error, presenter.error);
    assertNull(presenter.success);
  }

  /** A clock fixed at noon on the given day in New York. */
  private static Clock clockOn(LocalDate day) {
    return Clock.fixed(day.atTime(12, 0).atZone(NEW_YORK).toInstant(), NEW_YORK);
  }

  // ---- Test doubles ----

  /** The gateway methods {@link SwitchableGateway} can be told to fail. */
  private enum Method {
    QUOTE,
    PROFILE,
    HISTORY
  }

  /**
   * Answers from the fake gateway, except that the chosen methods throw as if the API were
   * down. Also counts calls and records which tickers were asked for.
   */
  private static class SwitchableGateway implements StockDataGateway {
    private final StockDataGateway delegate;
    final Set<Method> failing = new HashSet<>();
    final Set<String> tickersAskedFor = new HashSet<>();
    int calls;

    SwitchableGateway(StockDataGateway delegate) {
      this.delegate = delegate;
    }

    @Override
    public List<Stock> search(String query) throws StockDataUnavailableException {
      calls++;
      return delegate.search(query);
    }

    @Override
    public Quote getQuote(String ticker) throws StockDataUnavailableException {
      record(Method.QUOTE, ticker);
      return delegate.getQuote(ticker);
    }

    @Override
    public CompanyProfile getProfile(String ticker) throws StockDataUnavailableException {
      record(Method.PROFILE, ticker);
      return delegate.getProfile(ticker);
    }

    @Override
    public List<PricePoint> getDailyHistory(String ticker, LocalDate from, LocalDate to)
        throws StockDataUnavailableException {
      record(Method.HISTORY, ticker);
      return delegate.getDailyHistory(ticker, from, to);
    }

    private void record(Method method, String ticker) throws StockDataUnavailableException {
      calls++;
      tickersAskedFor.add(ticker);
      if (failing.contains(method)) {
        throw new StockDataUnavailableException(method + " is down");
      }
    }
  }

  /** Records the last thing the interactor presented. */
  private static class RecordingPresenter implements ViewStockOutputBoundary {
    ViewStockOutputData success;
    String failedTicker;
    ChartRange failedRange;
    String error;

    @Override
    public void prepareSuccessView(ViewStockOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String ticker, ChartRange range, String error) {
      failedTicker = ticker;
      failedRange = range;
      this.error = error;
    }
  }
}
