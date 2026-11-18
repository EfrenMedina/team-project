package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class AccountTest {

  private static final String ACCOUNT_ID = "account-1";
  private static final String OTHER_ACCOUNT_ID = "account-2";
  private static final String TRADE_ID = "trade-1";
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  // Constructor

  @Test
  void constructor_storesIds() {
    // Inputs
    Account account = new Account(ACCOUNT_ID, "user-1");

    // Assertion
    assertEquals(ACCOUNT_ID, account.getId());
    assertEquals("user-1", account.getUserId());
  }

  // getTotalTicketHeld and getAllPositions

  @Test
  void getTotalTicketHeld_noPositions_returnsZero() {
    // Inputs
    Account account = emptyAccount();

    // Action and assertion
    assertEquals(0L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void getAllPositions_noPositions_isEmpty() {
    // Inputs
    Account account = emptyAccount();

    // Action and assertion
    assertTrue(account.getAllPositions().isEmpty());
  }

  // addPosition

  @Test
  void addPosition_accountIsBuyer_increasesShares() {
    // Inputs
    Account account = emptyAccount();

    // Action
    long held = account.addPosition(buyTrade("AAPL", 10, "100.00"));

    // Assertion
    assertEquals(10L, held);
    assertEquals(10L, account.getTotalTicketHeld("AAPL"));
    assertEquals(Money.of("1000.00"), account.getPosition("AAPL").getTotalCost());
  }

  @Test
  void addPosition_sameTickerTwice_mergesIntoOnePositionAtAverageCost() {
    // Inputs
    Account account = emptyAccount();
    account.addPosition(buyTrade("AAPL", 10, "100.00"));

    // Action
    account.addPosition(buyTrade("AAPL", 10, "120.00"));

    // Assertion
    Position pos = account.getPosition("AAPL");
    assertEquals(20L, pos.getQuantity());
    assertEquals(Money.of("2200.00"), pos.getTotalCost());
    assertEquals(Money.of("110.00"), pos.getAverageCost());
    assertEquals(Map.of("AAPL", 20L), account.getAllPositions());
  }

  @Test
  void addPosition_differentTickers_listsEach() {
    // Inputs
    Account account = emptyAccount();
    account.addPosition(buyTrade("AAPL", 10, "100.00"));

    // Action
    account.addPosition(buyTrade("MSFT", 3, "100.00"));

    // Assertion
    assertEquals(Map.of("AAPL", 10L, "MSFT", 3L), account.getAllPositions());
  }

  @Test
  void addPosition_accountIsNotBuyer_throws() {
    // Inputs
    Account account = emptyAccount();
    Trade trade = sellTrade("AAPL", 10);

    // Action and assertion
    assertReason(TradeException.Reason.BUYER_ID_DOESNT_MATCH, () -> account.addPosition(trade));
    assertEquals(0L, account.getTotalTicketHeld("AAPL"));
  }

  // popPosition

  @Test
  void popPosition_partialSell_reducesSharesAndKeepsAverageCost() {
    // Inputs
    Account account = accountHolding("AAPL", 10, "100.00");
    account.addPosition(buyTrade("AAPL", 10, "120.00"));

    // Action
    long held = account.popPosition(sellTrade("AAPL", 5));

    // Assertion
    Position pos = account.getPosition("AAPL");
    assertEquals(15L, held);
    assertEquals(Money.of("1650.00"), pos.getTotalCost());
    assertEquals(Money.of("110.00"), pos.getAverageCost());
  }

  @Test
  void popPosition_sellsEverything_removesPosition() {
    // Inputs
    Account account = accountHolding("AAPL", 10, "100.00");

    // Action
    long held = account.popPosition(sellTrade("AAPL", 10));

    // Assertion
    assertEquals(0L, held);
    assertNull(account.getPosition("AAPL"));
    assertTrue(account.getAllPositions().isEmpty());
  }

  @Test
  void popPosition_moreThanHeld_throws() {
    // Inputs
    Account account = accountHolding("AAPL", 10, "100.00");
    Trade trade = sellTrade("AAPL", 11);

    // Action and assertion
    assertReason(TradeException.Reason.SELL_LESS_THAN_HELD, () -> account.popPosition(trade));
    assertEquals(10L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void popPosition_tickerNotHeld_throws() {
    // Inputs
    Account account = emptyAccount();
    Trade trade = sellTrade("AAPL", 1);

    // Action and assertion
    assertReason(TradeException.Reason.SELLER_DOES_NOT_HOLD_TICKER,
        () -> account.popPosition(trade));
  }

  // Helpers

  /** An account with no positions. */
  private static Account emptyAccount() {
    return new Account(ACCOUNT_ID, "user-1");
  }

  /** An account that bought some shares of one ticker. */
  private static Account accountHolding(String ticker, long quantity, String price) {
    Account account = emptyAccount();
    account.addPosition(buyTrade(ticker, quantity, price));
    return account;
  }

  /** A trade where the test account is the buyer. */
  private static Trade buyTrade(String ticker, long quantity, String price) {
    return new Trade(ticker, quantity, Money.of(price), NOW, ACCOUNT_ID, TRADE_ID);
  }

  /** A trade where the test account is the seller. */
  private static Trade sellTrade(String ticker, long quantity) {
    return new Trade(ticker, quantity, Money.of("100.00"), NOW, OTHER_ACCOUNT_ID, TRADE_ID);
  }

  private static void assertReason(TradeException.Reason expected, Executable action) {
    TradeException e = assertThrows(TradeException.class, action);
    assertEquals(expected, e.getReason());
  }
}
