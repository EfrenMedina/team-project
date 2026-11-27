package interfaceadapter.viewstock;

/**
 * Opens the Trade ticket (wireframe 05) for a stock, from the Stock detail screen's Buy, Sell
 * and Limit buttons.
 *
 * <p>The Trade ticket belongs to US-05, US-06 and US-10 and doesn't exist yet, so the Stock
 * detail screen only knows this interface. Once the ticket exists, the app wires in a version
 * that fills in its ticker and mode and switches to it, for example through the app shell's
 * {@code ViewManagerModel} (SCRUM-23).
 */
@FunctionalInterface
public interface TradeTicketOpener {

  /** Which button was clicked, and so which mode the ticket opens in. */
  enum Action {
    /** Buy at the market price (US-05). */
    BUY,
    /** Sell at the market price (US-06). */
    SELL,
    /** Place a limit order; the ticket asks whether to buy or sell (US-10). */
    LIMIT
  }

  /**
   * Opens the Trade ticket with the ticker filled in.
   *
   * @param ticker the stock's ticker, for example {@code "AAPL"}
   * @param action which button was clicked
   */
  void open(String ticker, Action action);
}
