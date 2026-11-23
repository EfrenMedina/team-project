package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.FakeStockDataGateway;
import interfaceadapter.viewstock.ViewStockController;
import interfaceadapter.viewstock.ViewStockPresenter;
import interfaceadapter.viewstock.ViewStockState;
import interfaceadapter.viewstock.ViewStockState.ChartPoint;
import interfaceadapter.viewstock.ViewStockViewModel;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.viewstock.ChartRange;
import usecase.viewstock.ViewStockInputData;
import usecase.viewstock.ViewStockInteractor;

class StockDetailViewTest {

  private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
  private static final LocalDate TODAY = FakeStockDataGateway.LAST_RECORDED_DAY;

  private FakeStockDataGateway fake;
  private ViewStockViewModel viewModel;
  private StockDetailView view;
  private List<ViewStockInputData> requests;
  private List<String> opened;

  @BeforeEach
  void setUp() {
    fake = new FakeStockDataGateway();
    viewModel = new ViewStockViewModel();
    requests = new ArrayList<>();
    opened = new ArrayList<>();
    view = new StockDetailView(viewModel, Runnable::run);
    view.setViewStockController(controllerFor(fake));
    view.setTradeTicketOpener((ticker, action) -> opened.add(ticker + " " + action));
  }

  @Test
  void open_recordedStock_showsPriceChangeNameAndExchange() throws Exception {
    // Action
    view.open("AAPL");
    flushEventQueue();

    // Assertion: values from the recorded AAPL data
    assertEquals("AAPL · NASDAQ", view.exchangeLabel.getText());
    assertEquals("Apple Inc.", view.nameLabel.getText());
    assertEquals("$344.63", view.priceLabel.getText());
    assertEquals("−$4.28 (−1.23%)", view.changeLabel.getText());
    assertEquals(" ", view.errorLabel.getText());
    assertEquals(5, view.keyStatsPanel.getComponentCount());
    assertTrue(view.rangeButtons.get(ChartRange.ONE_MONTH).isSelected());
  }

  @Test
  void open_ticker_asksForOneMonth() {
    // Inputs
    view.setViewStockController(new ViewStockController(requests::add));

    // Action
    view.open("AAPL");

    // Assertion
    assertEquals(List.of(new ViewStockInputData("AAPL", ChartRange.ONE_MONTH)), requests);
  }

  @Test
  void rangeButton_clicked_redrawsChartForThatRange() throws Exception {
    // Inputs
    view.open("AAPL");
    flushEventQueue();
    final int oneMonthPoints = view.chartSeries.getItemCount();

    // Action
    view.rangeButtons.get(ChartRange.ONE_YEAR).doClick();
    flushEventQueue();

    // Assertion
    int expected = fake.getDailyHistory("AAPL", ChartRange.ONE_YEAR.startDate(TODAY), TODAY).size();
    assertEquals(expected, view.chartSeries.getItemCount());
    assertTrue(view.chartSeries.getItemCount() > oneMonthPoints);
    assertTrue(view.rangeButtons.get(ChartRange.ONE_YEAR).isSelected());
  }

  @Test
  void rangeButton_clicked_asksForSameTickerWithThatRange() throws Exception {
    // Inputs
    view.open("MSFT");
    flushEventQueue();
    view.setViewStockController(new ViewStockController(requests::add));

    // Action
    view.rangeButtons.get(ChartRange.SIX_MONTHS).doClick();

    // Assertion
    assertEquals(List.of(new ViewStockInputData("MSFT", ChartRange.SIX_MONTHS)), requests);
  }

  @Test
  void tradeButtons_clicked_openTicketWithShownTicker() throws Exception {
    // Inputs
    view.open("NVDA");
    flushEventQueue();

    // Action
    view.buyButton.doClick();
    view.sellButton.doClick();
    view.limitButton.doClick();

    // Assertion
    assertEquals(List.of("NVDA BUY", "NVDA SELL", "NVDA LIMIT"), opened);
    assertEquals("Buy NVDA", view.buyButton.getText());
    assertEquals("Sell NVDA", view.sellButton.getText());
  }

  @Test
  void open_dataUnavailable_showsErrorAndDisablesTradeButtons() throws Exception {
    // Action: the fake has no data for XYZQ
    view.open("XYZQ");
    flushEventQueue();

    // Assertion
    assertEquals("Prices are unavailable right now.", view.errorLabel.getText());
    assertEquals("XYZQ", view.exchangeLabel.getText());
    assertEquals(" ", view.priceLabel.getText());
    assertEquals(0, view.chartSeries.getItemCount());
    assertFalse(view.buyButton.isEnabled());
    assertFalse(view.sellButton.isEnabled());
    assertFalse(view.limitButton.isEnabled());
    assertTrue(view.rangeButtons.get(ChartRange.ONE_MONTH).isEnabled());
  }

  @Test
  void open_whileRequestRuns_showsLoadingAndDisablesButtons() throws Exception {
    // Inputs: an executor that holds the request until the test runs it
    List<Runnable> held = new ArrayList<>();
    Executor holding = held::add;
    view = new StockDetailView(viewModel, holding);
    view.setViewStockController(controllerFor(fake));

    // Action
    view.open("AAPL");

    // Assertion: loading until the request finishes
    assertEquals(StockDetailView.LOADING_TEXT, view.statusLabel.getText());
    assertFalse(view.rangeButtons.get(ChartRange.ONE_YEAR).isEnabled());
    assertFalse(view.buyButton.isEnabled());
    held.get(0).run();
    flushEventQueue();
    assertEquals(" ", view.statusLabel.getText());
    assertTrue(view.rangeButtons.get(ChartRange.ONE_YEAR).isEnabled());
    assertTrue(view.buyButton.isEnabled());
  }

  @Test
  void newView_beforeAnyStock_hasTradeButtonsDisabled() {
    // Action / Assertion
    assertFalse(view.buyButton.isEnabled());
    assertEquals("Buy", view.buyButton.getText());
  }

  @Test
  void open_noControllerYet_doesNothing() {
    // Inputs
    view.setViewStockController(null);

    // Action
    view.open("AAPL");

    // Assertion
    assertEquals(" ", view.statusLabel.getText());
  }

  @Test
  void chartRose_rangeEndsAboveStart_isTrueEvenIfTodayFell() {
    // Inputs: up over the range, down today
    ViewStockState state = withChart(false, "10.00", "12.00");

    // Action / Assertion
    assertTrue(StockDetailView.chartRose(state));
  }

  @Test
  void chartRose_rangeEndsBelowStart_isFalse() {
    // Inputs
    ViewStockState state = withChart(true, "12.00", "10.00");

    // Action / Assertion
    assertFalse(StockDetailView.chartRose(state));
  }

  @Test
  void chartRose_noPoints_isTrue() {
    // Action / Assertion
    assertTrue(StockDetailView.chartRose(ViewStockState.EMPTY));
  }

  @Test
  void getViewName_always_matchesViewModel() {
    // Action / Assertion
    assertEquals(ViewStockViewModel.VIEW_NAME, view.getViewName());
  }

  @Test
  void label_eachRange_matchesButtonText() {
    // Action / Assertion
    assertEquals("1M", StockDetailView.label(ChartRange.ONE_MONTH));
    assertEquals("6M", StockDetailView.label(ChartRange.SIX_MONTHS));
    assertEquals("1Y", StockDetailView.label(ChartRange.ONE_YEAR));
  }

  @Test
  void constructor_nullArgument_throws() {
    // Inputs
    Executor direct = Runnable::run;

    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new StockDetailView(null, direct));
    assertThrows(NullPointerException.class, () -> new StockDetailView(viewModel, null));
  }

  /** A loaded state with today's direction and one chart point per given close. */
  private static ViewStockState withChart(boolean changeUp, String... closes) {
    List<ChartPoint> chart = new ArrayList<>();
    for (int i = 0; i < closes.length; i++) {
      chart.add(new ChartPoint(TODAY.minusDays(closes.length - 1 - i), new BigDecimal(closes[i])));
    }
    return new ViewStockState("TEST", ChartRange.ONE_MONTH, "Test Corp.", "TEST · NYSE",
        "$1.00", "", changeUp, List.of(), chart, null);
  }

  /** The real use case and presenter over the fake gateway, on the fake's last day. */
  private ViewStockController controllerFor(FakeStockDataGateway gateway) {
    Clock clock = Clock.fixed(TODAY.atTime(12, 0).atZone(NEW_YORK).toInstant(), NEW_YORK);
    return new ViewStockController(
        new ViewStockInteractor(gateway, new ViewStockPresenter(viewModel), clock));
  }

  /** Waits for redraws the view handed to the Swing event thread. */
  private static void flushEventQueue()
      throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(() -> { });
  }
}
