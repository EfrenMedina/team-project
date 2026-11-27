package interfaceadapter.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.FakeStockDataGateway;
import entity.CompanyProfile;
import entity.Money;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import interfaceadapter.viewstock.ViewStockState.ChartPoint;
import interfaceadapter.viewstock.ViewStockState.KeyStat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.shared.StockDataUnavailableException;
import usecase.viewstock.ChartRange;
import usecase.viewstock.ViewStockOutputData;

class ViewStockPresenterTest {

  private static final LocalDate DAY = LocalDate.of(2026, 10, 29);

  private ViewStockViewModel viewModel;
  private ViewStockPresenter presenter;

  @BeforeEach
  void setUp() {
    viewModel = new ViewStockViewModel();
    presenter = new ViewStockPresenter(viewModel);
  }

  @Test
  void prepareSuccessView_recordedAapl_showsFormattedStock()
      throws StockDataUnavailableException {
    // Inputs: AAPL as recorded on 2026-10-31
    FakeStockDataGateway fake = new FakeStockDataGateway();
    ViewStockOutputData output = new ViewStockOutputData(fake.getQuote("AAPL"),
        fake.getProfile("AAPL"), ChartRange.ONE_MONTH, fake.getDailyHistory("AAPL", DAY, DAY));

    // Action
    presenter.prepareSuccessView(output);

    // Assertion
    ViewStockState state = viewModel.getState();
    assertEquals("AAPL", state.ticker());
    assertEquals(ChartRange.ONE_MONTH, state.range());
    assertEquals("Apple Inc.", state.companyName());
    assertEquals("AAPL · NASDAQ", state.exchangeLine());
    assertEquals("$344.63", state.price());
    assertEquals("−$4.28 (−1.23%)", state.change());
    assertFalse(state.changeUp());
    assertEquals(
        List.of(
            new KeyStat("Open", "$346.72"),
            new KeyStat("High", "$349.19"),
            new KeyStat("Low", "$334.35"),
            new KeyStat("Prev close", "$348.91"),
            new KeyStat("Market cap", "$5.03T")),
        state.keyStats());
    assertEquals(List.of(new ChartPoint(DAY, new BigDecimal("348.91"))), state.chart());
    assertNull(state.error());
  }

  @Test
  void prepareSuccessView_priceUp_showsPlusSignsAndChangeUp() {
    // Action: up $2.57 from $229.57
    presenter.prepareSuccessView(output("232.14", "229.57", "1000.00", List.of()));

    // Assertion
    assertEquals("+$2.57 (+1.12%)", viewModel.getState().change());
    assertTrue(viewModel.getState().changeUp());
  }

  @Test
  void prepareSuccessView_noChange_showsNoSignAndChangeUp() {
    // Action
    presenter.prepareSuccessView(output("100.00", "100.00", "1000.00", List.of()));

    // Assertion
    assertEquals("$0.00 (0.00%)", viewModel.getState().change());
    assertTrue(viewModel.getState().changeUp());
  }

  @Test
  void prepareSuccessView_priceOverOneThousand_usesThousandsSeparator() {
    // Action
    presenter.prepareSuccessView(output("1234.56", "1234.56", "1000.00", List.of()));

    // Assertion
    assertEquals("$1,234.56", viewModel.getState().price());
  }

  @Test
  void prepareSuccessView_history_givesOneChartPointPerDayOldestFirst() {
    // Inputs
    LocalDate nextDay = DAY.plusDays(1);
    List<PricePoint> history = List.of(point(DAY, "10.00"), point(nextDay, "11.50"));

    // Action
    presenter.prepareSuccessView(output("11.50", "10.00", "1000.00", history));

    // Assertion
    assertEquals(
        List.of(new ChartPoint(DAY, new BigDecimal("10.00")),
            new ChartPoint(nextDay, new BigDecimal("11.50"))),
        viewModel.getState().chart());
  }

  @Test
  void prepareSuccessView_afterFailure_clearsError() {
    // Inputs
    presenter.prepareFailView("AAPL", ChartRange.ONE_MONTH, "Prices are unavailable right now.");

    // Action
    presenter.prepareSuccessView(output("100.00", "100.00", "1000.00", List.of()));

    // Assertion
    assertNull(viewModel.getState().error());
  }

  @Test
  void prepareFailView_afterSuccess_showsErrorAndClearsOldStock() {
    // Inputs
    presenter.prepareSuccessView(output("100.00", "99.00", "1000.00", List.of(point(DAY, "1"))));

    // Action
    presenter.prepareFailView("MSFT", ChartRange.SIX_MONTHS, "Prices are unavailable right now.");

    // Assertion
    ViewStockState state = viewModel.getState();
    assertEquals("Prices are unavailable right now.", state.error());
    assertEquals("MSFT", state.ticker());
    assertEquals(ChartRange.SIX_MONTHS, state.range());
    assertEquals("", state.companyName());
    assertEquals("", state.price());
    assertTrue(state.keyStats().isEmpty());
    assertTrue(state.chart().isEmpty());
  }

  @Test
  void prepareFailView_nullTicker_showsEmptyTicker() {
    // Action
    presenter.prepareFailView(null, ChartRange.ONE_MONTH, "Enter a ticker.");

    // Assertion
    assertEquals("", viewModel.getState().ticker());
    assertEquals("Enter a ticker.", viewModel.getState().error());
  }

  @Test
  void money_negativeAmount_usesTrueMinusSign() {
    // Action / Assertion
    assertEquals("−$1,234.56", ViewStockPresenter.money(Money.of("-1234.56")));
  }

  @Test
  void signedPercent_moreThanTwoDecimals_roundsHalfEven() {
    // Action / Assertion
    assertEquals("+1.12%", ViewStockPresenter.signedPercent(new BigDecimal("1.125")));
    assertEquals("−0.50%", ViewStockPresenter.signedPercent(new BigDecimal("-0.5")));
  }

  @Test
  void marketCap_eachSize_usesShortForm() {
    // Action / Assertion
    assertEquals("$5.03T", ViewStockPresenter.marketCap(Money.of("5029592198492.90")));
    assertEquals("$812.40B", ViewStockPresenter.marketCap(Money.of("812400000000.00")));
    assertEquals("$95.10M", ViewStockPresenter.marketCap(Money.of("95100000.00")));
    assertEquals("$950,000.00", ViewStockPresenter.marketCap(Money.of("950000.00")));
  }

  @Test
  void marketCap_zero_showsUnknown() {
    // Action / Assertion
    assertEquals(ViewStockPresenter.UNKNOWN, ViewStockPresenter.marketCap(Money.ZERO));
  }

  @Test
  void constructor_nullViewModel_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ViewStockPresenter(null));
  }

  /** Output for a made-up stock with the given price, previous close and market cap. */
  private static ViewStockOutputData output(
      String price, String previousClose, String marketCap, List<PricePoint> history) {
    Money last = Money.of(price);
    Money before = Money.of(previousClose);
    Money high = last.compareTo(before) > 0 ? last : before;
    Money low = last.compareTo(before) > 0 ? before : last;
    Quote quote = new Quote("TEST", last, before, high, low, before, Instant.EPOCH);
    CompanyProfile profile =
        new CompanyProfile(new Stock("TEST", "Test Corp.", "NYSE"), "", Money.of(marketCap));
    return new ViewStockOutputData(quote, profile, ChartRange.ONE_MONTH, history);
  }

  private static PricePoint point(LocalDate date, String close) {
    Money price = Money.of(close);
    return new PricePoint(date, price, price, price, price, 1_000L);
  }
}
