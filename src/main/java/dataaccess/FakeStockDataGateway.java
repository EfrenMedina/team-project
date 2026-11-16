package dataaccess;

import entity.CompanyProfile;
import entity.Money;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import usecase.shared.StockDataGateway;
import usecase.shared.StockDataUnavailableException;

/**
 * A stock data gateway that serves real responses recorded from Finnhub and Twelve Data, so
 * tests and the app can run without calling the real services. The recorded files are in
 * {@code src/main/resources/fake-stock-data/}; {@code spike/MarketDataSpike.java} re-records
 * them.
 *
 * <p>The recorded history stops at {@link #LAST_RECORDED_DAY}. Anything that works out dates
 * from today, such as the chart's 1M, 6M and 1Y ranges, must use a {@code Clock} fixed to that
 * day when running on this fake (in offline mode too), or its results come back partly or
 * entirely empty.
 */
public class FakeStockDataGateway implements StockDataGateway {

  /** The tickers that have recorded data. */
  public static final List<String> TICKERS = List.of("AAPL", "MSFT", "GOOGL", "AMZN", "NVDA");

  /** The first day of recorded history. */
  public static final LocalDate FIRST_RECORDED_DAY = LocalDate.of(2025, 10, 20);

  /** The last day of recorded history, which is also the day the quotes are from. */
  public static final LocalDate LAST_RECORDED_DAY = LocalDate.of(2026, 10, 30);

  private static final String DATA_DIR = "/fake-stock-data/";

  private final Map<String, Quote> quotes = new HashMap<>();
  private final Map<String, CompanyProfile> profiles = new HashMap<>();
  private final Map<String, List<PricePoint>> histories = new HashMap<>();

  /**
   * Creates the gateway and loads the recorded data for every ticker in {@link #TICKERS}.
   *
   * @throws IllegalStateException if a recorded file is missing or can't be read
   */
  public FakeStockDataGateway() {
    for (String ticker : TICKERS) {
      quotes.put(ticker, parseQuote(ticker, readJson(ticker + "-quote.json")));
      profiles.put(ticker, parseProfile(readJson(ticker + "-profile.json")));
      histories.put(ticker, parseHistory(readJson(ticker + "-history.json")));
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Searches only the recorded tickers, ignoring case. An exact ticker comes first, then
   * tickers starting with the query, then companies whose name contains it. A blank query
   * matches nothing.
   */
  @Override
  public List<Stock> search(String query) throws StockDataUnavailableException {
    String wanted = query.trim().toUpperCase(Locale.ROOT);
    if (wanted.isEmpty()) {
      return List.of();
    }
    List<Stock> exactTicker = new ArrayList<>();
    List<Stock> tickerStart = new ArrayList<>();
    List<Stock> nameContains = new ArrayList<>();
    for (String ticker : TICKERS) {
      Stock stock = profiles.get(ticker).stock();
      if (ticker.equals(wanted)) {
        exactTicker.add(stock);
      } else if (ticker.startsWith(wanted)) {
        tickerStart.add(stock);
      } else if (stock.name().toUpperCase(Locale.ROOT).contains(wanted)) {
        nameContains.add(stock);
      }
    }
    List<Stock> results = new ArrayList<>(exactTicker);
    results.addAll(tickerStart);
    results.addAll(nameContains);
    return List.copyOf(results);
  }

  @Override
  public Quote getQuote(String ticker) throws StockDataUnavailableException {
    Quote quote = quotes.get(ticker);
    if (quote == null) {
      throw new StockDataUnavailableException("No quote for ticker: " + ticker);
    }
    return quote;
  }

  /**
   * Moves a stock's price, for tests. The day's high and low are both set to the new price, so
   * a limit order's fill depends only on the price the test chose and not on the recorded
   * range. The open, previous close and time are unchanged.
   *
   * @param ticker one of {@link #TICKERS}
   * @param price the new price; greater than zero
   * @throws IllegalArgumentException if {@code ticker} has no recorded data or {@code price} is
   *     not greater than zero
   */
  public void setPrice(String ticker, Money price) {
    Quote old = quotes.get(ticker);
    if (old == null) {
      throw new IllegalArgumentException("No recorded data for ticker: " + ticker);
    }
    quotes.put(
        ticker,
        new Quote(ticker, price, old.open(), price, price, old.previousClose(), old.time()));
  }

  @Override
  public CompanyProfile getProfile(String ticker) throws StockDataUnavailableException {
    CompanyProfile profile = profiles.get(ticker);
    if (profile == null) {
      throw new StockDataUnavailableException("No profile for ticker: " + ticker);
    }
    return profile;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The recorded history covers {@link #FIRST_RECORDED_DAY} to {@link #LAST_RECORDED_DAY},
   * so days outside that range are missing from the result.
   *
   * @throws IllegalArgumentException if {@code to} is before {@code from}
   */
  @Override
  public List<PricePoint> getDailyHistory(String ticker, LocalDate from, LocalDate to)
      throws StockDataUnavailableException {
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("End date " + to + " is before start date " + from);
    }
    List<PricePoint> history = histories.get(ticker);
    if (history == null) {
      throw new StockDataUnavailableException("No history for ticker: " + ticker);
    }
    List<PricePoint> inRange = new ArrayList<>();
    for (PricePoint point : history) {
      if (!point.date().isBefore(from) && !point.date().isAfter(to)) {
        inRange.add(point);
      }
    }
    return List.copyOf(inRange);
  }

  /** Converts a Finnhub {@code /quote} response. */
  private static Quote parseQuote(String ticker, JSONObject json) {
    return new Quote(
        ticker,
        money(json, "c"),
        money(json, "o"),
        money(json, "h"),
        money(json, "l"),
        money(json, "pc"),
        Instant.ofEpochSecond(json.getLong("t")));
  }

  /** Converts a Finnhub {@code /stock/profile2} response. */
  private static CompanyProfile parseProfile(JSONObject json) {
    Stock stock =
        new Stock(
            json.getString("ticker"),
            json.getString("name"),
            exchangeName(json.getString("exchange")));
    // Finnhub gives the market cap in millions of dollars
    BigDecimal marketCap =
        new BigDecimal(json.get("marketCapitalization").toString()).movePointRight(6);
    return new CompanyProfile(
        stock,
        json.optString("finnhubIndustry", ""),
        Money.of(marketCap.setScale(2, RoundingMode.HALF_EVEN).toPlainString()));
  }

  /**
   * Converts a Twelve Data {@code /time_series} response, which lists days newest first, into
   * points oldest first.
   */
  private static List<PricePoint> parseHistory(JSONObject json) {
    JSONArray values = json.getJSONArray("values");
    List<PricePoint> points = new ArrayList<>();
    for (int i = values.length() - 1; i >= 0; i--) {
      JSONObject day = values.getJSONObject(i);
      points.add(
          new PricePoint(
              LocalDate.parse(day.getString("datetime")),
              money(day, "open"),
              money(day, "high"),
              money(day, "low"),
              money(day, "close"),
              Long.parseLong(day.getString("volume"))));
    }
    return points;
  }

  /**
   * Shortens Finnhub's exchange name to the one users know, for example {@code "NASDAQ NMS -
   * GLOBAL MARKET"} to {@code "NASDAQ"}. Names it doesn't recognize are kept as they are.
   */
  static String exchangeName(String finnhubExchange) {
    if (finnhubExchange.startsWith("NASDAQ")) {
      return "NASDAQ";
    }
    if (finnhubExchange.startsWith("NEW YORK STOCK EXCHANGE")) {
      return "NYSE";
    }
    return finnhubExchange;
  }

  /**
   * Reads a price, rounding it to whole cents. Goes through the number's text so that, for
   * example, 346.715 rounds to 346.72 rather than being misread as 346.71499999.
   */
  private static Money money(JSONObject json, String key) {
    BigDecimal value = new BigDecimal(json.get(key).toString());
    return Money.of(value.setScale(2, RoundingMode.HALF_EVEN).toPlainString());
  }

  private static JSONObject readJson(String fileName) {
    try (InputStream in = FakeStockDataGateway.class.getResourceAsStream(DATA_DIR + fileName)) {
      if (in == null) {
        throw new IllegalStateException("Missing recorded file: " + DATA_DIR + fileName);
      }
      return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Can't read recorded file: " + DATA_DIR + fileName, e);
    }
  }
}
