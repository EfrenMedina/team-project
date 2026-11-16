package entity;

import java.util.Objects;

/**
 * A stock as it appears in search results: its ticker symbol, company name and exchange.
 *
 * @param ticker the ticker symbol, for example {@code "AAPL"}; never blank
 * @param name the company name, for example {@code "Apple Inc."}; never blank
 * @param exchange the exchange it trades on, for example {@code "NASDAQ"}; never blank
 */
public record Stock(String ticker, String name, String exchange) {

  /**
   * Creates a stock.
   *
   * @throws NullPointerException if any component is null
   * @throws IllegalArgumentException if any component is blank
   */
  public Stock {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(exchange, "exchange");

    if (ticker.isBlank()) {
      throw new IllegalArgumentException("Ticker must not be blank");
    }
    if (name.isBlank()) {
      throw new IllegalArgumentException("Name must not be blank");
    }
    if (exchange.isBlank()) {
      throw new IllegalArgumentException("Exchange must not be blank");
    }
  }
}
