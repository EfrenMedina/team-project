package dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.CompanyProfile;
import entity.Money;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.shared.StockDataUnavailableException;

class FakeStockDataGatewayTest {

  private FakeStockDataGateway gateway;

  @BeforeEach
  void setUp() {
    gateway = new FakeStockDataGateway();
  }

  @Test
  void getQuote_knownTicker_returnsRecordedQuote() throws StockDataUnavailableException {
    // Action
    Quote quote = gateway.getQuote("AAPL");

    // Assertion: values from AAPL-quote.json, recorded on 2026-10-31
    assertEquals("AAPL", quote.ticker());
    assertEquals(Money.of("344.63"), quote.price());
    assertEquals(Money.of("349.19"), quote.high());
    assertEquals(Money.of("334.35"), quote.low());
    assertEquals(Money.of("348.91"), quote.previousClose());
    assertEquals(Instant.ofEpochSecond(1793390400L), quote.time());
  }

  @Test
  void getQuote_priceWithThreeDecimals_roundsHalfEvenToCents()
      throws StockDataUnavailableException {
    // Action: the recorded open is 346.715
    Quote quote = gateway.getQuote("AAPL");

    // Assertion
    assertEquals(Money.of("346.72"), quote.open());
  }

  @Test
  void getQuote_everyRecordedTicker_returnsQuote() throws StockDataUnavailableException {
    for (String ticker : FakeStockDataGateway.TICKERS) {
      // Action
      Quote quote = gateway.getQuote(ticker);

      // Assertion
      assertEquals(ticker, quote.ticker());
    }
    assertEquals(5, FakeStockDataGateway.TICKERS.size());
  }

  @Test
  void getQuote_unknownTicker_throws() {
    // Action / Assertion
    assertThrows(StockDataUnavailableException.class, () -> gateway.getQuote("XYZQ"));
  }

  @Test
  void setPrice_knownTicker_changesPriceHighAndLow() throws StockDataUnavailableException {
    // Inputs
    Money newPrice = Money.of("300.00");

    // Action
    gateway.setPrice("AAPL", newPrice);

    // Assertion
    Quote quote = gateway.getQuote("AAPL");
    assertEquals(newPrice, quote.price());
    assertEquals(newPrice, quote.high());
    assertEquals(newPrice, quote.low());
  }

  @Test
  void setPrice_knownTicker_keepsOpenPreviousCloseAndTime()
      throws StockDataUnavailableException {
    // Inputs
    Quote before = gateway.getQuote("AAPL");

    // Action
    gateway.setPrice("AAPL", Money.of("300.00"));

    // Assertion
    Quote after = gateway.getQuote("AAPL");
    assertEquals(before.open(), after.open());
    assertEquals(before.previousClose(), after.previousClose());
    assertEquals(before.time(), after.time());
    assertEquals(Money.of("-48.91"), after.change());
  }

  @Test
  void setPrice_oneTicker_leavesOtherTickersUnchanged() throws StockDataUnavailableException {
    // Inputs
    Quote msftBefore = gateway.getQuote("MSFT");

    // Action
    gateway.setPrice("AAPL", Money.of("300.00"));

    // Assertion
    assertEquals(msftBefore, gateway.getQuote("MSFT"));
  }

  @Test
  void setPrice_unknownTicker_throws() {
    // Inputs
    Money price = Money.of("300.00");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> gateway.setPrice("XYZQ", price));
  }

  @Test
  void setPrice_zeroPrice_throwsAndKeepsOldQuote() throws StockDataUnavailableException {
    // Inputs
    Quote before = gateway.getQuote("AAPL");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> gateway.setPrice("AAPL", Money.ZERO));
    assertEquals(before, gateway.getQuote("AAPL"));
  }

  @Test
  void getProfile_knownTicker_returnsRecordedProfile() throws StockDataUnavailableException {
    // Action
    CompanyProfile profile = gateway.getProfile("AAPL");

    // Assertion: values from AAPL-profile.json, recorded on 2026-10-31
    assertEquals(new Stock("AAPL", "Apple Inc.", "NASDAQ"), profile.stock());
    assertEquals("Technology", profile.industry());
  }

  @Test
  void getProfile_marketCapInMillions_convertsToDollars() throws StockDataUnavailableException {
    // Action: the recorded market cap is 5029592.198492898 (millions)
    CompanyProfile profile = gateway.getProfile("AAPL");

    // Assertion
    assertEquals(Money.of("5029592198492.90"), profile.marketCap());
  }

  @Test
  void getProfile_everyRecordedTicker_returnsProfile() throws StockDataUnavailableException {
    for (String ticker : FakeStockDataGateway.TICKERS) {
      // Action
      CompanyProfile profile = gateway.getProfile(ticker);

      // Assertion
      assertEquals(ticker, profile.stock().ticker());
    }
  }

  @Test
  void getProfile_unknownTicker_throws() {
    // Action / Assertion
    assertThrows(StockDataUnavailableException.class, () -> gateway.getProfile("XYZQ"));
  }

  @Test
  void getDailyHistory_oneWeek_returnsTradingDaysOldestFirst()
      throws StockDataUnavailableException {
    // Action: Saturday 2026-10-24 to Friday 2026-10-30
    List<PricePoint> history =
        gateway.getDailyHistory("AAPL", LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 30));

    // Assertion: Monday to Friday, weekend skipped
    assertEquals(5, history.size());
    assertEquals(LocalDate.of(2026, 10, 26), history.get(0).date());
    assertEquals(LocalDate.of(2026, 10, 30), history.get(4).date());
  }

  @Test
  void getDailyHistory_singleDay_returnsRecordedValues() throws StockDataUnavailableException {
    // Inputs
    LocalDate day = LocalDate.of(2026, 10, 29);

    // Action: from and to are the same day, so both ends must be included
    List<PricePoint> history = gateway.getDailyHistory("AAPL", day, day);

    // Assertion: values from AAPL-history.json, rounded to cents
    assertEquals(
        List.of(new PricePoint(day, Money.of("348.00"), Money.of("351.43"), Money.of("347.30"),
            Money.of("348.91"), 42_582_100L)),
        history);
  }

  @Test
  void getDailyHistory_recordedRange_matchesFirstAndLastRecordedDay()
      throws StockDataUnavailableException {
    for (String ticker : FakeStockDataGateway.TICKERS) {
      // Action
      List<PricePoint> history = gateway.getDailyHistory(ticker,
          FakeStockDataGateway.FIRST_RECORDED_DAY, FakeStockDataGateway.LAST_RECORDED_DAY);

      // Assertion: fails after re-recording until the constants are updated
      assertEquals(FakeStockDataGateway.FIRST_RECORDED_DAY, history.get(0).date(), ticker);
      assertEquals(FakeStockDataGateway.LAST_RECORDED_DAY,
          history.get(history.size() - 1).date(), ticker);
    }
  }

  @Test
  void getDailyHistory_oneYear_everyRecordedTickerHasData()
      throws StockDataUnavailableException {
    for (String ticker : FakeStockDataGateway.TICKERS) {
      // Action
      List<PricePoint> history =
          gateway.getDailyHistory(ticker, LocalDate.of(2025, 10, 31), LocalDate.of(2026, 10, 30));

      // Assertion: the recorded year has 251 trading days
      assertEquals(251, history.size(), ticker);
    }
  }

  @Test
  void getDailyHistory_weekendOnly_returnsEmptyList() throws StockDataUnavailableException {
    // Action
    List<PricePoint> history =
        gateway.getDailyHistory("AAPL", LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25));

    // Assertion
    assertTrue(history.isEmpty());
  }

  @Test
  void getDailyHistory_endBeforeStart_throws() {
    // Inputs
    LocalDate from = LocalDate.of(2026, 10, 30);
    LocalDate to = LocalDate.of(2026, 10, 1);

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> gateway.getDailyHistory("AAPL", from, to));
  }

  @Test
  void getDailyHistory_unknownTicker_throws() {
    // Inputs
    LocalDate day = LocalDate.of(2026, 10, 29);

    // Action / Assertion
    assertThrows(StockDataUnavailableException.class,
        () -> gateway.getDailyHistory("XYZQ", day, day));
  }

  @Test
  void search_exactTicker_returnsThatStock() throws StockDataUnavailableException {
    // Action
    List<Stock> results = gateway.search("AAPL");

    // Assertion
    assertEquals(List.of(new Stock("AAPL", "Apple Inc.", "NASDAQ")), results);
  }

  @Test
  void search_lowercaseWithSpaces_ignoresCaseAndSpaces() throws StockDataUnavailableException {
    // Action
    List<Stock> results = gateway.search("  aapl ");

    // Assertion
    assertEquals(List.of(new Stock("AAPL", "Apple Inc.", "NASDAQ")), results);
  }

  @Test
  void search_partOfCompanyName_findsByName() throws StockDataUnavailableException {
    // Action
    List<Stock> results = gateway.search("apple");

    // Assertion
    assertEquals(List.of(new Stock("AAPL", "Apple Inc.", "NASDAQ")), results);
  }

  @Test
  void search_tickerStartAndNameMatches_listsTickerMatchesFirst()
      throws StockDataUnavailableException {
    // Action: NVDA starts with N; the other four have an N in their name
    List<Stock> results = gateway.search("n");

    // Assertion
    assertEquals(5, results.size());
    assertEquals("NVDA", results.get(0).ticker());
  }

  @Test
  void search_noMatch_returnsEmptyList() throws StockDataUnavailableException {
    // Action / Assertion
    assertTrue(gateway.search("xyzq").isEmpty());
  }

  @Test
  void search_blankQuery_returnsEmptyList() throws StockDataUnavailableException {
    // Action / Assertion
    assertTrue(gateway.search("   ").isEmpty());
  }

  @Test
  void exchangeName_nasdaqTiers_becomeNasdaq() {
    // Action / Assertion
    assertEquals("NASDAQ", FakeStockDataGateway.exchangeName("NASDAQ NMS - GLOBAL MARKET"));
    assertEquals("NASDAQ", FakeStockDataGateway.exchangeName("NASDAQ NMS - GLOBAL SELECT"));
  }

  @Test
  void exchangeName_newYorkStockExchange_becomesNyse() {
    // Action / Assertion
    assertEquals("NYSE", FakeStockDataGateway.exchangeName("NEW YORK STOCK EXCHANGE, INC."));
  }

  @Test
  void exchangeName_unknownExchange_isKept() {
    // Action / Assertion
    assertEquals("NYSE ARCA", FakeStockDataGateway.exchangeName("NYSE ARCA"));
  }
}
