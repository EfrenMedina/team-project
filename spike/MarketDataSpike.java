import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

/**
 * SCRUM-18 market data spike. Not part of the app: a throwaway program that checks Finnhub and
 * Twelve Data give us what we need, records sample responses and measures the rate limits.
 *
 * <p>Run from the project root (needs config.properties with both API keys):
 *
 * <pre>
 *   java spike/MarketDataSpike.java          # call every endpoint, save AAPL responses
 *   java spike/MarketDataSpike.java limits   # burst requests to find the real rate limits
 * </pre>
 */
public class MarketDataSpike {

  private static final String FINNHUB = "https://finnhub.io/api/v1";
  private static final String TWELVE_DATA = "https://api.twelvedata.com";
  private static final Path RECORD_DIR = Path.of("src/test/resources/api");
  private static final String[] SYMBOLS = {"AAPL", "MSFT"};

  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private final String finnhubKey;
  private final String twelveDataKey;

  private MarketDataSpike(String finnhubKey, String twelveDataKey) {
    this.finnhubKey = finnhubKey;
    this.twelveDataKey = twelveDataKey;
  }

  /** Entry point. */
  public static void main(String[] args) throws Exception {
    Properties config = new Properties();
    try (InputStream in = Files.newInputStream(Path.of("config.properties"))) {
      config.load(in);
    }
    MarketDataSpike spike =
        new MarketDataSpike(
            config.getProperty("finnhub.api.key"), config.getProperty("twelvedata.api.key"));
    if (args.length > 0 && args[0].equals("limits")) {
      spike.measureFinnhubLimit();
      spike.measureTwelveDataLimit();
    } else {
      spike.callEveryEndpoint();
    }
  }

  private void callEveryEndpoint() throws IOException, InterruptedException {
    Files.createDirectories(RECORD_DIR);
    for (String symbol : SYMBOLS) {
      System.out.println("== " + symbol + " ==");
      String name = symbol.equals("AAPL") ? "apple" : "microsoft";
      check("finnhub-quote", symbol, finnhub("/quote?symbol=" + symbol));
      check("finnhub-search", symbol, finnhub("/search?q=" + encode(name)));
      check("finnhub-profile", symbol, finnhub("/stock/profile2?symbol=" + symbol));
      check(
          "twelvedata-time-series",
          symbol,
          twelveData("/time_series?symbol=" + symbol + "&interval=1day&outputsize=30"));
      // Not used by the plan, checked to back up the provider decision
      check("twelvedata-quote", symbol, twelveData("/quote?symbol=" + symbol));
      String lastMonth = "&resolution=D&from=1759276800&to=1761696000";
      check("finnhub-candle", symbol, finnhub("/stock/candle?symbol=" + symbol + lastMonth));
    }
  }

  private void check(String endpoint, String symbol, HttpResponse<String> response)
      throws IOException {
    String body = response.body();
    boolean ok = response.statusCode() == 200 && !isTwelveDataError(body) && !body.isBlank();
    String preview = body.length() > 110 ? body.substring(0, 110) + "..." : body;
    System.out.printf(
        "  %-24s HTTP %d  %6d bytes  %s%n  %24s %s%n",
        endpoint, response.statusCode(), body.length(), ok ? "OK" : "FAILED", "", preview);
    if (ok && symbol.equals("AAPL") && !endpoint.equals("finnhub-candle")) {
      Files.writeString(RECORD_DIR.resolve(endpoint + "-AAPL.json"), body + "\n");
    }
  }

  /**
   * Sends quote requests back to back until Finnhub refuses one, and reports its rate-limit
   * headers.
   */
  private void measureFinnhubLimit() throws IOException, InterruptedException {
    System.out.println("== Finnhub: quote requests back to back ==");
    long start = System.nanoTime();
    for (int i = 1; i <= 80; i++) {
      HttpResponse<String> response = finnhub("/quote?symbol=AAPL");
      String limit = response.headers().firstValue("x-ratelimit-limit").orElse("?");
      String remaining = response.headers().firstValue("x-ratelimit-remaining").orElse("?");
      double seconds = (System.nanoTime() - start) / 1e9;
      if (response.statusCode() != 200) {
        System.out.printf(
            "  request %d at %.1fs: HTTP %d (limit header %s, remaining %s) %s%n",
            i, seconds, response.statusCode(), limit, remaining, response.body());
        return;
      }
      if (i == 1 || i % 10 == 0) {
        System.out.printf(
            "  request %d at %.1fs: OK (limit header %s, remaining %s)%n",
            i, seconds, limit, remaining);
      }
    }
    System.out.println("  no request refused in 80 tries");
  }

  /** Sends time series requests back to back until Twelve Data refuses one. */
  private void measureTwelveDataLimit() throws IOException, InterruptedException {
    System.out.println("== Twelve Data: time series requests back to back ==");
    System.out.println("  usage before: " + twelveData("/api_usage").body());
    long start = System.nanoTime();
    for (int i = 1; i <= 12; i++) {
      HttpResponse<String> response =
          twelveData("/time_series?symbol=AAPL&interval=1day&outputsize=1");
      double seconds = (System.nanoTime() - start) / 1e9;
      if (response.statusCode() != 200 || isTwelveDataError(response.body())) {
        System.out.printf(
            "  request %d at %.1fs: HTTP %d %s%n",
            i, seconds, response.statusCode(), response.body());
        break;
      }
      System.out.printf("  request %d at %.1fs: OK%n", i, seconds);
    }
    System.out.println("  usage after: " + twelveData("/api_usage").body());
  }

  private static boolean isTwelveDataError(String body) {
    return body.contains("\"status\":\"error\"");
  }

  private HttpResponse<String> finnhub(String pathAndQuery)
      throws IOException, InterruptedException {
    return get(FINNHUB + pathAndQuery + separator(pathAndQuery) + "token=" + finnhubKey);
  }

  private HttpResponse<String> twelveData(String pathAndQuery)
      throws IOException, InterruptedException {
    return get(TWELVE_DATA + pathAndQuery + separator(pathAndQuery) + "apikey=" + twelveDataKey);
  }

  private HttpResponse<String> get(String url) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static String separator(String pathAndQuery) {
    return pathAndQuery.contains("?") ? "&" : "?";
  }

  private static String encode(String text) {
    return URLEncoder.encode(text, StandardCharsets.UTF_8);
  }
}
