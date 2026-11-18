package app;

import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import view.DashboardView;
import view.LoginView;
import view.NavLink;
import view.PlaceholderView;
import view.ShellView;
import view.SidebarItem;
import view.TabBar;
import view.Theme;
import view.ViewManager;

/**
 * Builds the app window and wires every feature together.
 *
 * <p>Each feature owns two methods here and touches nothing else:
 *
 * <ul>
 *   <li>{@code addXView()} creates the feature's view model and view, and adds the view as a card.
 *       Screens after login go through {@link #addScreen}, which puts the sidebar around them.
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
    cardPanel.setBackground(Theme.BACKGROUND);
    new ViewManager(cardPanel, cardLayout, viewManagerModel);
  }

  // ---- 01 Login / Sign up (US-01, US-02) ----

  /** Adds the login and sign-up screen. */
  public AppBuilder addLoginView() {
    LoginView view = new LoginView(navigation);
    cardPanel.add(view, ViewNames.LOGIN);
    return this;
  }

  // ---- 02 Dashboard + watchlist (US-09) ----

  /** Adds the dashboard. */
  public AppBuilder addDashboardView() {
    return addScreen(ViewNames.DASHBOARD, SidebarItem.DASHBOARD, new DashboardView(navigation));
  }

  // ---- 03 Search (US-03) ----

  /** Adds the stock search screen. */
  public AppBuilder addSearchView() {
    return addScreen(
        ViewNames.SEARCH,
        SidebarItem.SEARCH,
        new PlaceholderView(
            "Search",
            null,
            "US-03",
            "03-search.svg",
            navigation,
            List.of(new NavLink("Pick a result", ViewNames.STOCK_DETAIL, true))));
  }

  // ---- 04 Stock detail (US-04) ----

  /** Adds the stock detail screen. */
  public AppBuilder addStockDetailView() {
    return addScreen(
        ViewNames.STOCK_DETAIL,
        SidebarItem.SEARCH,
        new PlaceholderView(
            "Stock detail",
            null,
            "US-04",
            "04-stock-detail.svg",
            navigation,
            List.of(
                new NavLink("Buy", ViewNames.TRADE_TICKET, true),
                new NavLink("Sell", ViewNames.TRADE_TICKET))));
  }

  // ---- 05 Trade ticket (US-05, US-06, US-10) ----

  /** Adds the buy/sell ticket. */
  public AppBuilder addTradeTicketView() {
    return addScreen(
        ViewNames.TRADE_TICKET,
        SidebarItem.SEARCH,
        new PlaceholderView(
            "Trade ticket",
            null,
            "US-05, US-06, US-10",
            "05-trade-ticket.svg",
            navigation,
            List.of(
                new NavLink("Filled → back to stock", ViewNames.STOCK_DETAIL, true),
                new NavLink("Limit order placed", ViewNames.OPEN_ORDERS))));
  }

  // ---- 06 Open orders (US-10) ----

  /** Adds the open limit orders screen. */
  public AppBuilder addOpenOrdersView() {
    return addScreen(
        ViewNames.OPEN_ORDERS,
        SidebarItem.ORDERS,
        new PlaceholderView(
            "Open orders", null, "US-10", "06-open-orders.svg", navigation, List.of()));
  }

  // ---- 07 Portfolio (US-07) ----

  /** Adds the portfolio screen. */
  public AppBuilder addPortfolioView() {
    return addScreen(
        ViewNames.PORTFOLIO,
        SidebarItem.PORTFOLIO,
        new PlaceholderView(
            "Portfolio",
            null,
            "US-07",
            "07-portfolio.svg",
            navigation,
            List.of(new NavLink("Click a holding", ViewNames.STOCK_DETAIL))));
  }

  // ---- 08 Trade history (US-08) ----

  /** Adds the trade history screen. */
  public AppBuilder addTradeHistoryView() {
    return addScreen(
        ViewNames.TRADE_HISTORY,
        SidebarItem.HISTORY,
        new PlaceholderView(
            "Trade history",
            null,
            "US-08",
            "08-trade-history.svg",
            navigation,
            List.of(new NavLink("Click a trade", ViewNames.STOCK_DETAIL))));
  }

  // ---- 09 Account statement (US-11) ----

  /** Adds the account statement, the first tab under Statements. */
  public AppBuilder addAccountStatementView() {
    return addScreen(
        ViewNames.ACCOUNT_STATEMENT,
        SidebarItem.STATEMENTS,
        new PlaceholderView(
            "Statements",
            statementTabs(ViewNames.ACCOUNT_STATEMENT),
            "US-11",
            "09-account-statement.svg",
            navigation,
            List.of()));
  }

  // ---- 10 Realized gains (US-12) ----

  /** Adds the realized gains report, the second tab under Statements. */
  public AppBuilder addRealizedGainsView() {
    return addScreen(
        ViewNames.REALIZED_GAINS,
        SidebarItem.STATEMENTS,
        new PlaceholderView(
            "Statements",
            statementTabs(ViewNames.REALIZED_GAINS),
            "US-12",
            "10-realized-gains.svg",
            navigation,
            List.of()));
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
    frame.setMinimumSize(new Dimension(1100, 720));
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

  /**
   * Adds a screen that appears after login, with the sidebar around it.
   *
   * @param viewName the card name, from {@link ViewNames}
   * @param selected the sidebar item to highlight while this screen is showing
   * @param content the screen's own view
   * @return this builder
   */
  private AppBuilder addScreen(String viewName, SidebarItem selected, JComponent content) {
    cardPanel.add(new ShellView(viewName, selected, navigation, content), viewName);
    return this;
  }

  private TabBar statementTabs(String selected) {
    return new TabBar(
        List.of(
            new NavLink("Account statement", ViewNames.ACCOUNT_STATEMENT),
            new NavLink("Realized gains", ViewNames.REALIZED_GAINS)),
        selected,
        navigation);
  }
}
