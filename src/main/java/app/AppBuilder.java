package app;

import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import view.DashboardView;
import view.NavLink;
import view.PlaceholderView;
import view.ViewManager;

/**
 * Builds the app window and wires every feature together.
 *
 * <p>Each feature owns two methods here and touches nothing else:
 *
 * <ul>
 *   <li>{@code addXView()} creates the feature's view model and view, and adds the view as a card.
 *   <li>{@code addXUseCase()} creates the interactor, presenter and controller, and hands the
 *       controller to the view. Add it when the feature gets its first use case, then call it from
 *       {@link Main} after the views.
 * </ul>
 *
 * <p>Pick data sources from {@link #config} in your {@code addXUseCase()}, so one setting switches
 * the whole app between fakes and the real thing:
 *
 * <pre>{@code
 * XDataAccessInterface dao = config.dataMode() == AppConfig.DataMode.REAL
 *     ? new SqliteXDataAccessObject(...)
 *     : new InMemoryXDataAccessObject();
 * }</pre>
 *
 * <p>Until a feature is built, its {@code addXView()} adds a {@link PlaceholderView} with links
 * that follow {@code docs/wireframes/00-navigation-map.svg}.
 */
public class AppBuilder {

  private static final String TITLE = "Paper Trading App";
  private static final Dimension WINDOW_SIZE = new Dimension(1280, 800);
  private static final NavLink TO_DASHBOARD = new NavLink("← Dashboard", ViewNames.DASHBOARD);

  private final JPanel cardPanel = new JPanel();
  private final CardLayout cardLayout = new CardLayout();
  private final ViewManagerModel viewManagerModel = new ViewManagerModel();
  private final NavigationController navigation = new NavigationController(viewManagerModel);
  private final AppConfig config;

  /**
   * Starts an empty app.
   *
   * @param config start-up settings, including which data sources to use
   */
  public AppBuilder(AppConfig config) {
    this.config = config;
    cardPanel.setLayout(cardLayout);
    new ViewManager(cardPanel, cardLayout, viewManagerModel);
  }

  // ---- 01 Login / Sign up (US-01, US-02) ----

  /** Adds the login and sign-up screen. */
  public AppBuilder addLoginView() {
    return addPlaceholder(
        ViewNames.LOGIN,
        "Log in / Sign up",
        "US-01, US-02",
        List.of(new NavLink("Log in", ViewNames.DASHBOARD)));
  }

  // ---- 02 Dashboard + watchlist (US-09) ----

  /** Adds the dashboard, whose sidebar reaches every other screen. */
  public AppBuilder addDashboardView() {
    addView(ViewNames.DASHBOARD, new DashboardView(navigation));
    return this;
  }

  // ---- 03 Search (US-03) ----

  /** Adds the stock search screen. */
  public AppBuilder addSearchView() {
    return addPlaceholder(
        ViewNames.SEARCH,
        "Search",
        "US-03",
        List.of(TO_DASHBOARD, new NavLink("Pick a result", ViewNames.STOCK_DETAIL)));
  }

  // ---- 04 Stock detail (US-04) ----

  /** Adds the stock detail screen. */
  public AppBuilder addStockDetailView() {
    return addPlaceholder(
        ViewNames.STOCK_DETAIL,
        "Stock detail",
        "US-04",
        List.of(
            TO_DASHBOARD,
            new NavLink("Buy", ViewNames.TRADE_TICKET),
            new NavLink("Sell", ViewNames.TRADE_TICKET)));
  }

  // ---- 05 Trade ticket (US-05, US-06, US-10) ----

  /** Adds the buy/sell ticket. */
  public AppBuilder addTradeTicketView() {
    return addPlaceholder(
        ViewNames.TRADE_TICKET,
        "Trade ticket",
        "US-05, US-06, US-10",
        List.of(
            new NavLink("Filled → back", ViewNames.STOCK_DETAIL),
            new NavLink("Limit order placed", ViewNames.OPEN_ORDERS)));
  }

  // ---- 06 Open orders (US-10) ----

  /** Adds the open limit orders screen. */
  public AppBuilder addOpenOrdersView() {
    return addPlaceholder(ViewNames.OPEN_ORDERS, "Open orders", "US-10", List.of(TO_DASHBOARD));
  }

  // ---- 07 Portfolio (US-07) ----

  /** Adds the portfolio screen. */
  public AppBuilder addPortfolioView() {
    return addPlaceholder(
        ViewNames.PORTFOLIO,
        "Portfolio",
        "US-07",
        List.of(TO_DASHBOARD, new NavLink("Click holding", ViewNames.STOCK_DETAIL)));
  }

  // ---- 08 Trade history (US-08) ----

  /** Adds the trade history screen. */
  public AppBuilder addTradeHistoryView() {
    return addPlaceholder(
        ViewNames.TRADE_HISTORY,
        "Trade history",
        "US-08",
        List.of(TO_DASHBOARD, new NavLink("Click trade", ViewNames.STOCK_DETAIL)));
  }

  // ---- 09 Account statement (US-11) ----

  /** Adds the account statement screen. */
  public AppBuilder addAccountStatementView() {
    return addPlaceholder(
        ViewNames.ACCOUNT_STATEMENT, "Account statement", "US-11", List.of(TO_DASHBOARD));
  }

  // ---- 10 Realized gains (US-12) ----

  /** Adds the realized gains report. */
  public AppBuilder addRealizedGainsView() {
    return addPlaceholder(
        ViewNames.REALIZED_GAINS, "Realized gains", "US-12", List.of(TO_DASHBOARD));
  }

  // ---- Build ----

  /**
   * Creates the window, showing the login screen first.
   *
   * @return the app window, not yet visible
   */
  public JFrame build() {
    String title = config.dataMode() == AppConfig.DataMode.FAKE ? TITLE + " (fake data)" : TITLE;
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(cardPanel);
    frame.setSize(WINDOW_SIZE);
    frame.setLocationRelativeTo(null);
    showFirstScreen();
    return frame;
  }

  /** Switches to the login screen. Split out of {@link #build()} so tests can run headless. */
  void showFirstScreen() {
    navigation.navigateTo(ViewNames.LOGIN);
  }

  JPanel getCardPanel() {
    return cardPanel;
  }

  ViewManagerModel getViewManagerModel() {
    return viewManagerModel;
  }

  // ---- Helpers ----

  private AppBuilder addPlaceholder(
      String viewName, String title, String userStories, List<NavLink> links) {
    addView(viewName, new PlaceholderView(viewName, title, userStories, navigation, links));
    return this;
  }

  private void addView(String viewName, JPanel view) {
    view.setName(viewName);
    cardPanel.add(view, viewName);
  }
}
