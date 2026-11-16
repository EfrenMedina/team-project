package usecase.shared;

import entity.CompanyProfile;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import java.time.LocalDate;
import java.util.List;

/**
 * The one way use cases get stock data. The app uses an implementation that calls the real data
 * services; tests use {@code FakeStockDataGateway}, which serves recorded data and never touches
 * the network.
 */
public interface StockDataGateway {

  /**
   * Finds US stocks whose ticker or company name matches the query.
   *
   * <p>Every result includes its exchange. Implementations must not spend one request per
   * result to find it, because that would use up the rate limit quotes need; the Finnhub
   * implementation looks it up in Finnhub's list of US symbols, fetched at most once a day (see
   * the README).
   *
   * @param query what the user typed, for example {@code "apple"} or {@code "AAPL"}
   * @return the matching stocks, best match first; empty if nothing matches
   * @throws StockDataUnavailableException if the search couldn't be done
   */
  List<Stock> search(String query) throws StockDataUnavailableException;

  /**
   * Gets a stock's latest price and today's trading range.
   *
   * @param ticker the ticker symbol, for example {@code "AAPL"}
   * @return the latest quote
   * @throws StockDataUnavailableException if the quote couldn't be fetched or the ticker isn't
   *     known
   */
  Quote getQuote(String ticker) throws StockDataUnavailableException;

  /**
   * Gets the company details shown on a stock's page.
   *
   * @param ticker the ticker symbol, for example {@code "AAPL"}
   * @return the company's profile
   * @throws StockDataUnavailableException if the profile couldn't be fetched or the ticker isn't
   *     known
   */
  CompanyProfile getProfile(String ticker) throws StockDataUnavailableException;

  /**
   * Gets a stock's daily prices between two dates, for its chart.
   *
   * @param ticker the ticker symbol, for example {@code "AAPL"}
   * @param from the first day to include
   * @param to the last day to include; not before {@code from}
   * @return one point per trading day in the range, oldest first; empty if there were no trading
   *     days (for example, a weekend)
   * @throws StockDataUnavailableException if the history couldn't be fetched or the ticker isn't
   *     known
   */
  List<PricePoint> getDailyHistory(String ticker, LocalDate from, LocalDate to)
      throws StockDataUnavailableException;
}
