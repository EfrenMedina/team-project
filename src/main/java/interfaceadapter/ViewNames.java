package interfaceadapter;

/**
 * Card names of every screen. Numbers match the wireframes in {@code docs/wireframes/}.
 *
 * <p>When a feature gets its own view model, its {@code getViewName()} should return the same
 * constant, so links from other screens keep working.
 */
public final class ViewNames {

  public static final String LOGIN = "login"; // 01
  public static final String DASHBOARD = "dashboard"; // 02
  public static final String SEARCH = "search"; // 03
  public static final String STOCK_DETAIL = "stock detail"; // 04
  public static final String TRADE_TICKET = "trade ticket"; // 05
  public static final String OPEN_ORDERS = "open orders"; // 06
  public static final String PORTFOLIO = "portfolio"; // 07
  public static final String TRADE_HISTORY = "trade history"; // 08
  public static final String ACCOUNT_STATEMENT = "account statement"; // 09
  public static final String REALIZED_GAINS = "realized gains"; // 10

  private ViewNames() {}
}
