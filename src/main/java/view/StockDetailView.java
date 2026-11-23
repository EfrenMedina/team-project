package view;

import interfaceadapter.viewstock.TradeTicketOpener;
import interfaceadapter.viewstock.ViewStockController;
import interfaceadapter.viewstock.ViewStockState;
import interfaceadapter.viewstock.ViewStockState.ChartPoint;
import interfaceadapter.viewstock.ViewStockState.KeyStat;
import interfaceadapter.viewstock.ViewStockViewModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import usecase.viewstock.ChartRange;

/**
 * The Stock detail screen from wireframe 04: the company, its price and today's change, a
 * price chart with 1M, 6M and 1Y buttons, the key stats, and Buy, Sell and Limit buttons that
 * open the Trade ticket. It asks the {@link ViewStockController} for data and redraws whenever
 * the {@link ViewStockViewModel} changes.
 *
 * <p>Left out until their user stories exist: the "Your position" panel (US-07), "Add to
 * watchlist" (US-09) and the stock's open orders (US-10). The sidebar and "Back to Search"
 * link belong to the app shell (SCRUM-23).
 */
public class StockDetailView extends JPanel implements PropertyChangeListener {

  /** Gains, matching {@code view.ui.Theme.GAIN}; switch to the theme once it is on main. */
  private static final Color GAIN = new Color(0x3DD68C);

  /** Losses and errors, matching {@code view.ui.Theme.LOSS}. */
  private static final Color LOSS = new Color(0xFF6A4D);

  /** Secondary text, matching {@code view.ui.Theme.MUTED}. */
  private static final Color MUTED = new Color(0x9AA1AB);

  /** Shown while a request is running. */
  static final String LOADING_TEXT = "Loading…";

  final JLabel exchangeLabel = new JLabel(" ");
  final JLabel nameLabel = new JLabel(" ");
  final JLabel priceLabel = new JLabel(" ");
  final JLabel changeLabel = new JLabel(" ");
  final JLabel errorLabel = new JLabel(" ");
  final JLabel statusLabel = new JLabel(" ");
  final Map<ChartRange, JToggleButton> rangeButtons = new EnumMap<>(ChartRange.class);
  final JPanel keyStatsPanel = new JPanel(new GridLayout(0, 4, 16, 12));
  final JButton buyButton = new JButton("Buy");
  final JButton sellButton = new JButton("Sell");
  final JButton limitButton = new JButton("Limit order");

  /** The chart's data: one closing price per trading day. */
  final TimeSeries chartSeries = new TimeSeries("Close");

  /** The chart, kept so its line colour can follow the range's performance. */
  private final JFreeChart chart;

  /** The screen's state. */
  private final ViewStockViewModel viewModel;

  /** Runs use case calls off the Swing event thread. */
  private final Executor useCaseExecutor;

  /** Where data requests go; set by the app shell after the use case is built. */
  private ViewStockController viewStockController;

  /** Opens the Trade ticket; set by the app shell once the ticket exists. */
  private TradeTicketOpener tradeTicketOpener;

  /**
   * Creates the screen and starts listening to its view model.
   *
   * @param viewModel the Stock detail screen's view model
   * @param useCaseExecutor runs use case calls off the Swing event thread. The Technical
   *     Design (p. 16) has every screen share one single-threaded executor
   * @throws NullPointerException if either argument is null
   */
  public StockDetailView(ViewStockViewModel viewModel, Executor useCaseExecutor) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    this.useCaseExecutor = Objects.requireNonNull(useCaseExecutor, "useCaseExecutor");
    chart = createChart(new TimeSeriesCollection(chartSeries));

    setLayout(new BorderLayout(24, 0));
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    JPanel main = new JPanel(new BorderLayout(0, 16));
    main.add(createHeader(), BorderLayout.NORTH);
    main.add(createChartArea(), BorderLayout.CENTER);
    main.add(createKeyStats(), BorderLayout.SOUTH);
    add(main, BorderLayout.CENTER);
    add(createTradePanel(), BorderLayout.EAST);

    viewModel.addPropertyChangeListener(this);
    showState(viewModel.getState());
  }

  /**
   * Sets where data requests go. The app shell calls this once, after building the
   * view-stock use case.
   *
   * @param viewStockController the controller to call
   */
  public void setViewStockController(ViewStockController viewStockController) {
    this.viewStockController = viewStockController;
  }

  /**
   * Sets what the Buy, Sell and Limit buttons call. The app shell calls this once the Trade
   * ticket exists; until then the buttons do nothing.
   *
   * @param tradeTicketOpener opens the Trade ticket
   */
  public void setTradeTicketOpener(TradeTicketOpener tradeTicketOpener) {
    this.tradeTicketOpener = tradeTicketOpener;
  }

  /**
   * Returns the name the app shell uses to switch to this screen.
   *
   * @return {@value ViewStockViewModel#VIEW_NAME}
   */
  public String getViewName() {
    return ViewStockViewModel.VIEW_NAME;
  }

  /**
   * Loads a stock with its 1M chart, for example after the user picks it in Search. Call this
   * on the Swing event thread.
   *
   * @param ticker the stock's ticker
   */
  public void open(String ticker) {
    request(ticker, ChartRange.ONE_MONTH);
  }

  /**
   * Redraws the screen when the view model's state changes. The presenter runs on the
   * executor's thread, so the redraw is moved onto the Swing event thread.
   *
   * @param event the change; its new value is the new {@link ViewStockState}
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (!ViewStockViewModel.STATE_PROPERTY.equals(event.getPropertyName())) {
      return;
    }
    ViewStockState state = (ViewStockState) event.getNewValue();
    if (SwingUtilities.isEventDispatchThread()) {
      showState(state);
    } else {
      SwingUtilities.invokeLater(() -> showState(state));
    }
  }

  /**
   * Asks the controller for a stock on the executor's thread, and shows that it is loading.
   *
   * @param ticker the stock's ticker
   * @param range how far back the chart goes
   */
  private void request(String ticker, ChartRange range) {
    ViewStockController controller = viewStockController;
    if (controller == null) {
      return;
    }
    statusLabel.setText(LOADING_TEXT);
    setButtonsEnabled(false);
    useCaseExecutor.execute(() -> controller.execute(ticker, range));
  }

  /**
   * Shows a state: either the stock or the error, with the state's range button selected.
   *
   * @param state what to show
   */
  private void showState(ViewStockState state) {
    statusLabel.setText(" ");
    errorLabel.setText(state.error() == null ? " " : state.error());
    exchangeLabel.setText(orSpace(state.exchangeLine().isEmpty()
        ? state.ticker() : state.exchangeLine()));
    nameLabel.setText(orSpace(state.companyName()));
    priceLabel.setText(orSpace(state.price()));
    changeLabel.setText(orSpace(state.change()));
    changeLabel.setForeground(state.changeUp() ? GAIN : LOSS);
    chart.getXYPlot().getRenderer().setSeriesPaint(0, chartRose(state) ? GAIN : LOSS);

    rangeButtons.get(state.range()).setSelected(true);
    showKeyStats(state);
    showChart(state);

    boolean loaded = state.error() == null && !state.price().isEmpty();
    buyButton.setText(loaded ? "Buy " + state.ticker() : "Buy");
    sellButton.setText(loaded ? "Sell " + state.ticker() : "Sell");
    setButtonsEnabled(true);
    buyButton.setEnabled(loaded);
    sellButton.setEnabled(loaded);
    limitButton.setEnabled(loaded);
  }

  private void showKeyStats(ViewStockState state) {
    keyStatsPanel.removeAll();
    for (KeyStat stat : state.keyStats()) {
      JLabel label = new JLabel(stat.label());
      label.setForeground(MUTED);
      JLabel value = new JLabel(stat.value());
      value.setFont(value.getFont().deriveFont(Font.BOLD));
      JPanel cell = new JPanel(new GridLayout(2, 1));
      cell.add(label);
      cell.add(value);
      keyStatsPanel.add(cell);
    }
    keyStatsPanel.revalidate();
    keyStatsPanel.repaint();
  }

  private void showChart(ViewStockState state) {
    chartSeries.setNotify(false);
    chartSeries.clear();
    for (ChartPoint point : state.chart()) {
      chartSeries.add(new Day(point.date().getDayOfMonth(), point.date().getMonthValue(),
          point.date().getYear()), point.close());
    }
    chartSeries.setNotify(true);
  }

  /** Turns the range buttons on or off, and the trade buttons off when {@code false}. */
  private void setButtonsEnabled(boolean enabled) {
    rangeButtons.values().forEach(button -> button.setEnabled(enabled));
    if (!enabled) {
      buyButton.setEnabled(false);
      sellButton.setEnabled(false);
      limitButton.setEnabled(false);
    }
  }

  /** Sends a trade button click to the Trade ticket, with the shown stock's ticker. */
  private void openTicket(TradeTicketOpener.Action action) {
    if (tradeTicketOpener != null) {
      tradeTicketOpener.open(viewModel.getState().ticker(), action);
    }
  }

  private JPanel createHeader() {
    exchangeLabel.setForeground(MUTED);
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 26f));
    priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 40f));
    errorLabel.setForeground(LOSS);
    statusLabel.setForeground(MUTED);

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.add(errorLabel);
    header.add(exchangeLabel);
    header.add(nameLabel);
    header.add(priceLabel);
    header.add(changeLabel);
    header.add(statusLabel);
    return header;
  }

  private JPanel createChartArea() {
    JPanel rangeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    ButtonGroup group = new ButtonGroup();
    for (ChartRange range : ChartRange.values()) {
      JToggleButton button = new JToggleButton(label(range));
      button.addActionListener(event -> request(viewModel.getState().ticker(), range));
      group.add(button);
      rangeBar.add(button);
      rangeButtons.put(range, button);
    }

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(640, 320));
    chartPanel.setPopupMenu(null);
    chartPanel.setDomainZoomable(false);
    chartPanel.setRangeZoomable(false);

    JPanel area = new JPanel(new BorderLayout(0, 8));
    area.add(chartPanel, BorderLayout.CENTER);
    area.add(rangeBar, BorderLayout.SOUTH);
    return area;
  }

  private JPanel createKeyStats() {
    JLabel title = new JLabel("Key stats");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    JPanel section = new JPanel(new BorderLayout(0, 8));
    section.add(title, BorderLayout.NORTH);
    section.add(keyStatsPanel, BorderLayout.CENTER);
    return section;
  }

  private JPanel createTradePanel() {
    buyButton.addActionListener(event -> openTicket(TradeTicketOpener.Action.BUY));
    sellButton.addActionListener(event -> openTicket(TradeTicketOpener.Action.SELL));
    limitButton.addActionListener(event -> openTicket(TradeTicketOpener.Action.LIMIT));
    buyButton.setBackground(GAIN);
    buyButton.setForeground(Color.BLACK);
    buyButton.setFont(buyButton.getFont().deriveFont(Font.BOLD));

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    for (JButton button : new JButton[] {buyButton, sellButton, limitButton}) {
      button.setAlignmentX(LEFT_ALIGNMENT);
      button.setMaximumSize(new Dimension(240, 44));
      button.setPreferredSize(new Dimension(240, 44));
      // FlatLaf client property; other look and feels ignore it.
      button.putClientProperty("JButton.buttonType", "roundRect");
      panel.add(button);
      panel.add(Box.createVerticalStrut(12));
    }
    return panel;
  }

  /** A line chart with no title, legend or axis labels, as on the wireframe. */
  private static JFreeChart createChart(TimeSeriesCollection dataset) {
    JFreeChart chart =
        ChartFactory.createTimeSeriesChart(null, null, null, dataset, false, false, false);
    chart.setBackgroundPaint(null);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(null);
    plot.setOutlineVisible(false);
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(false);
    plot.getRangeAxis().setAutoRange(true);
    for (Axis axis : new Axis[] {plot.getDomainAxis(), plot.getRangeAxis()}) {
      axis.setTickLabelPaint(MUTED);
      axis.setAxisLinePaint(MUTED);
      axis.setTickMarkPaint(MUTED);
    }
    plot.getRenderer().setSeriesStroke(0, new BasicStroke(2f));
    return chart;
  }

  /**
   * Whether the stock ended the chart's range at or above where it started, so the line is
   * green over a range it rose and red over one it fell, whatever today did.
   */
  static boolean chartRose(ViewStockState state) {
    if (state.chart().isEmpty()) {
      return true;
    }
    ChartPoint first = state.chart().get(0);
    ChartPoint last = state.chart().get(state.chart().size() - 1);
    return last.close().compareTo(first.close()) >= 0;
  }

  /** The text on a range button. */
  static String label(ChartRange range) {
    return switch (range) {
      case ONE_MONTH -> "1M";
      case SIX_MONTHS -> "6M";
      case ONE_YEAR -> "1Y";
    };
  }

  /** Keeps a label's height when it has nothing to show. */
  private static String orSpace(String text) {
    return text.isEmpty() ? " " : text;
  }
}
