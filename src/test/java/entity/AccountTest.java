package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class AccountTest {

  private static final String ACCOUNT_ID = "account-1";
  private static final String OTHER_ACCOUNT_ID = "account-2";
  private static final String SELLER_LOT_ID = "seller-lot";
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
  void getTotalTicketHeld_noLots_returnsZero() {
    // Inputs
    Account account = emptyAccount();

    // Action and assertion
    assertEquals(0L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void getAllPositions_noLots_isEmpty() {
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
    long held = account.addPosition(buyTrade("AAPL", 10), fixedId("lot-1"));

    // Assertion
    assertEquals(10L, held);
    assertEquals(10L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void addPosition_sameTickerTwice_sumsLots() {
    // Inputs
    Account account = emptyAccount();
    account.addPosition(buyTrade("AAPL", 10), fixedId("lot-1"));

    // Action
    account.addPosition(buyTrade("AAPL", 5), fixedId("lot-2"));

    // Assertion
    assertEquals(15L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void addPosition_differentTickers_listsEach() {
    // Inputs
    Account account = emptyAccount();
    account.addPosition(buyTrade("AAPL", 10), fixedId("lot-1"));

    // Action
    account.addPosition(buyTrade("MSFT", 3), fixedId("lot-2"));

    // Assertion
    assertEquals(Map.of("AAPL", 10L, "MSFT", 3L), account.getAllPositions());
  }

  @Test
  void addPosition_accountIsNotBuyer_throws() {
    // Inputs
    Account account = emptyAccount();
    Trade trade = sellTrade("lot-1", "AAPL", 10);

    // Action and assertion
    assertReason(TradeException.Reason.BUYER_ID_DOESNT_MATCH,
        () -> account.addPosition(trade, fixedId("lot-1")));
    assertEquals(0L, account.getTotalTicketHeld("AAPL"));
  }

  // popPosition

  @Test
  void popPosition_partialSell_reducesShares() {
    // Inputs
    Account account = accountHolding("lot-1", "AAPL", 10);

    // Action
    long held = account.popPosition(sellTrade("lot-1", "AAPL", 4));

    // Assertion
    assertEquals(6L, held);
    assertEquals(6L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void popPosition_moreThanLotHolds_throws() {
    // Inputs
    Account account = accountHolding("lot-1", "AAPL", 10);
    Trade trade = sellTrade("lot-1", "AAPL", 11);

    // Action and assertion
    assertReason(TradeException.Reason.SELL_LESS_THAN_HELD, () -> account.popPosition(trade));
    assertEquals(10L, account.getTotalTicketHeld("AAPL"));
  }

  @Test
  void popPosition_unknownLot_throws() {
    // Inputs
    Account account = accountHolding("lot-1", "AAPL", 10);
    Trade trade = sellTrade("lot-999", "AAPL", 1);

    // Action and assertion
    assertReason(TradeException.Reason.LOTID_INCONSISTENCY, () -> account.popPosition(trade));
  }

  @Test
  void popPosition_tickerNotHeld_throws() {
    // Inputs
    Account account = emptyAccount();
    Trade trade = sellTrade("lot-1", "AAPL", 1);

    // Action and assertion
    assertReason(TradeException.Reason.SELLER_DOES_NOT_HOLD_TICKER,
        () -> account.popPosition(trade));
  }

  @Test
  void popPosition_accountIsNotSeller_throws() {
    // Inputs
    Account account = accountHolding("lot-1", "AAPL", 10);
    Trade trade = buyTrade("AAPL", 1);

    // Action and assertion
    assertReason(TradeException.Reason.SELLER_ID_DOESNT_MATCH, () -> account.popPosition(trade));
    assertEquals(10L, account.getTotalTicketHeld("AAPL"));
  }

  // Helpers

  /** An account with no lots. */
  private static Account emptyAccount() {
    return new Account(ACCOUNT_ID, "user-1");
  }

  /** An account that bought one lot of a ticker, stored under the given lot id. */
  private static Account accountHolding(String lotId, String ticker, long quantity) {
    Account account = emptyAccount();
    account.addPosition(buyTrade(ticker, quantity), fixedId(lotId));
    return account;
  }

  /** An id generator that always returns the same id, so tests know the new lot's id. */
  private static IdGenerator fixedId(String id) {
    return () -> id;
  }

  /** A trade where the test account is the buyer. The lot id is the seller's lot. */
  private static Trade buyTrade(String ticker, long quantity) {
    return new Trade(SELLER_LOT_ID, ticker, quantity, Money.of("100.00"), NOW, ACCOUNT_ID,
        OTHER_ACCOUNT_ID);
  }

  /** A trade where the test account is the seller, selling from the given lot. */
  private static Trade sellTrade(String lotId, String ticker, long quantity) {
    return new Trade(lotId, ticker, quantity, Money.of("100.00"), NOW, OTHER_ACCOUNT_ID,
        ACCOUNT_ID);
  }

  private static void assertReason(TradeException.Reason expected, Executable action) {
    TradeException e = assertThrows(TradeException.class, action);
    assertEquals(expected, e.getReason());
  }
}
