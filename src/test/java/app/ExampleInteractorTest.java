package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Template for testing a use case interactor. Copy this pattern for real interactors.
 *
 * <ul>
 *   <li>In-memory DAO instead of the database, so tests are fast and need no files.
 *   <li>Fake presenter that records what the interactor sent, instead of a real UI.
 *   <li>Fixed {@link Clock}, so anything time-based gives the same answer every run.
 *   <li>Test names describe behaviour: {@code <action>_<condition>_<expected result>}.
 *   <li>Model actions as types (records/enums), never as strings.
 * </ul>
 *
 * <p>The small "place an order" use case below exists only to show the pattern. It is
 * deliberately simplified: the real {@code Money} type comes in SCRUM-19 and the real {@code
 * Order} and order statuses in SCRUM-20. Until then, money here is a {@code long} number of
 * cents. Real interactors live in {@code src/main/java}; only their test doubles belong in the
 * test.
 */
class ExampleInteractorTest {

  private static final Instant NOW = Instant.parse("2026-10-27T14:30:00Z");

  private InMemoryOrderDataAccess dataAccess;
  private FakeOrderPresenter presenter;
  private PlaceOrderInteractor interactor;

  @BeforeEach
  void setUp() {
    dataAccess = new InMemoryOrderDataAccess();
    presenter = new FakeOrderPresenter();
    Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    interactor = new PlaceOrderInteractor(dataAccess, presenter, fixedClock);
  }

  @Test
  void placeOrder_validLimitBuy_savesOrderAndPresentsSuccess() {
    interactor.execute(
        new PlaceOrderInputData("acc-1", "AAPL", Side.BUY, OrderType.LIMIT, 10, 15_000L));

    Order saved = dataAccess.orders.get("order-1");
    assertEquals("order-1", saved.id());
    assertEquals("acc-1", saved.accountId());
    assertEquals("AAPL", saved.ticker());
    assertEquals(Side.BUY, saved.side());
    assertEquals(OrderType.LIMIT, saved.type());
    assertEquals(10, saved.quantity());
    assertEquals(15_000L, saved.limitPriceCents());
    assertEquals(NOW, saved.placedAt());
    assertEquals(new PlaceOrderOutputData("order-1", NOW), presenter.success);
    assertNull(presenter.error);
  }

  @Test
  void placeOrder_validMarketSell_savesOrderWithNoLimitPrice() {
    interactor.execute(
        new PlaceOrderInputData("acc-1", "MSFT", Side.SELL, OrderType.MARKET, 5, null));

    Order saved = dataAccess.orders.get("order-1");
    assertEquals(Side.SELL, saved.side());
    assertEquals(OrderType.MARKET, saved.type());
    assertEquals(5, saved.quantity());
    assertNull(saved.limitPriceCents());
    assertEquals(new PlaceOrderOutputData("order-1", NOW), presenter.success);
  }

  @Test
  void placeOrder_zeroQuantity_presentsErrorAndSavesNothing() {
    interactor.execute(
        new PlaceOrderInputData("acc-1", "AAPL", Side.BUY, OrderType.MARKET, 0, null));

    assertFailedWith("Quantity must be greater than zero.");
  }

  @Test
  void placeOrder_limitWithoutPrice_presentsErrorAndSavesNothing() {
    interactor.execute(
        new PlaceOrderInputData("acc-1", "AAPL", Side.BUY, OrderType.LIMIT, 10, null));

    assertFailedWith("Limit orders need a limit price greater than zero.");
  }

  @Test
  void placeOrder_marketWithLimitPrice_presentsErrorAndSavesNothing() {
    interactor.execute(
        new PlaceOrderInputData("acc-1", "AAPL", Side.BUY, OrderType.MARKET, 10, 15_000L));

    assertFailedWith("Market orders must not have a limit price.");
  }

  private void assertFailedWith(String expectedError) {
    assertEquals(expectedError, presenter.error);
    assertNull(presenter.success);
    assertTrue(dataAccess.orders.isEmpty());
  }

  // ---- Example use case (in real code these are separate files in src/main/java) ----

  enum Side {
    BUY,
    SELL
  }

  enum OrderType {
    MARKET,
    LIMIT
  }

  /** What the user asked for. {@code limitPriceCents} is null for market orders. */
  record PlaceOrderInputData(
      String accountId,
      String ticker,
      Side side,
      OrderType type,
      int quantity,
      Long limitPriceCents) {}

  record Order(
      String id,
      String accountId,
      String ticker,
      Side side,
      OrderType type,
      int quantity,
      Long limitPriceCents,
      Instant placedAt) {}

  record PlaceOrderOutputData(String orderId, Instant placedAt) {}

  interface OrderDataAccessInterface {
    void save(Order order);
  }

  interface PlaceOrderOutputBoundary {
    void prepareSuccessView(PlaceOrderOutputData outputData);

    void prepareFailView(String error);
  }

  static class PlaceOrderInteractor {
    private final OrderDataAccessInterface dataAccess;
    private final PlaceOrderOutputBoundary presenter;
    private final Clock clock;
    private int nextOrderNumber = 1;

    PlaceOrderInteractor(
        OrderDataAccessInterface dataAccess, PlaceOrderOutputBoundary presenter, Clock clock) {
      this.dataAccess = dataAccess;
      this.presenter = presenter;
      this.clock = clock;
    }

    void execute(PlaceOrderInputData input) {
      String error = validate(input);
      if (error != null) {
        presenter.prepareFailView(error);
        return;
      }
      // Deterministic ids keep tests repeatable; real code would use a UUID.
      Order order =
          new Order(
              "order-" + nextOrderNumber++,
              input.accountId(),
              input.ticker(),
              input.side(),
              input.type(),
              input.quantity(),
              input.limitPriceCents(),
              clock.instant());
      dataAccess.save(order);
      presenter.prepareSuccessView(new PlaceOrderOutputData(order.id(), order.placedAt()));
    }

    /** Returns an error message, or null if the input is valid. */
    private static String validate(PlaceOrderInputData input) {
      if (input.quantity() <= 0) {
        return "Quantity must be greater than zero.";
      }
      boolean hasLimitPrice = input.limitPriceCents() != null;
      if (input.type() == OrderType.LIMIT && (!hasLimitPrice || input.limitPriceCents() <= 0)) {
        return "Limit orders need a limit price greater than zero.";
      }
      if (input.type() == OrderType.MARKET && hasLimitPrice) {
        return "Market orders must not have a limit price.";
      }
      return null;
    }
  }

  // ---- Test doubles ----

  /** Stores orders in a map instead of SQLite. */
  static class InMemoryOrderDataAccess implements OrderDataAccessInterface {
    final Map<String, Order> orders = new HashMap<>();

    @Override
    public void save(Order order) {
      orders.put(order.id(), order);
    }
  }

  /** Records the last thing the interactor presented, so tests can assert on it. */
  static class FakeOrderPresenter implements PlaceOrderOutputBoundary {
    PlaceOrderOutputData success;
    String error;

    @Override
    public void prepareSuccessView(PlaceOrderOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String error) {
      this.error = error;
    }
  }
}
