package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StockTest {

  @Test
  void constructor_validValues_keepsValues() {
    // Action
    Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ");

    // Assertion
    assertEquals("AAPL", stock.ticker());
    assertEquals("Apple Inc.", stock.name());
    assertEquals("NASDAQ", stock.exchange());
  }

  @Test
  void constructor_blankTicker_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> new Stock("  ", "Apple Inc.", "NASDAQ"));
  }

  @Test
  void constructor_blankName_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> new Stock("AAPL", "", "NASDAQ"));
  }

  @Test
  void constructor_blankExchange_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> new Stock("AAPL", "Apple Inc.", " "));
  }

  @Test
  void constructor_nullTicker_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new Stock(null, "Apple Inc.", "NASDAQ"));
  }

  @Test
  void constructor_nullName_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new Stock("AAPL", null, "NASDAQ"));
  }

  @Test
  void constructor_nullExchange_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new Stock("AAPL", "Apple Inc.", null));
  }
}
