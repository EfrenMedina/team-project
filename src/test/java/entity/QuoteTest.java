package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuoteTest {

  private static final Instant TIME = Instant.parse("2026-10-29T20:00:00Z");

  @Test
  void constructor_validValues_keepsValues() {
    // Action
    Quote quote = appleQuote("348.91", "343.83");

    // Assertion
    assertEquals("AAPL", quote.ticker());
    assertEquals(Money.of("348.91"), quote.price());
    assertEquals(Money.of("348.00"), quote.open());
    assertEquals(Money.of("351.43"), quote.high());
    assertEquals(Money.of("347.30"), quote.low());
    assertEquals(Money.of("343.83"), quote.previousClose());
    assertEquals(TIME, quote.time());
  }

  @Test
  void change_priceAbovePreviousClose_isPositive() {
    // Action
    Quote quote = appleQuote("348.91", "343.83");

    // Assertion
    assertEquals(Money.of("5.08"), quote.change());
    assertEquals(new BigDecimal("1.48"), quote.changePercent());
  }

  @Test
  void change_priceBelowPreviousClose_isNegative() {
    // Action
    Quote quote = appleQuote("530.14", "531.01");

    // Assertion
    assertEquals(Money.of("-0.87"), quote.change());
    assertEquals(new BigDecimal("-0.16"), quote.changePercent());
  }

  @Test
  void change_priceEqualsPreviousClose_isZero() {
    // Action
    Quote quote = appleQuote("348.91", "348.91");

    // Assertion
    assertEquals(Money.ZERO, quote.change());
    assertEquals(new BigDecimal("0.00"), quote.changePercent());
  }

  @Test
  void changePercent_veryLargePrices_doesNotOverflow() {
    // Inputs: cents times 100 is beyond the range of a long here
    Money previousClose = Money.of("1000000000000000.00");
    Money price = Money.of("2000000000000000.00");

    // Action
    Quote quote = new Quote("AAPL", price, price, price, price, previousClose, TIME);

    // Assertion
    assertEquals(new BigDecimal("100.00"), quote.changePercent());
  }

  @Test
  void constructor_highBelowLow_throws() {
    // Inputs
    Money high = Money.of("340.00");
    Money low = Money.of("350.00");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new Quote("AAPL", Money.of("345.00"), Money.of("345.00"), high, low,
            Money.of("343.83"), TIME));
  }

  @Test
  void constructor_zeroPrice_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> appleQuote("0.00", "343.83"));
  }

  @Test
  void constructor_zeroPreviousClose_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> appleQuote("348.91", "0.00"));
  }

  @Test
  void constructor_blankTicker_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new Quote(" ", price, price, price, price, price, TIME));
  }

  @Test
  void constructor_nullPrice_throws() {
    // Inputs
    Money other = Money.of("348.91");

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new Quote("AAPL", null, other, other, other, other, TIME));
  }

  @Test
  void constructor_nullTime_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new Quote("AAPL", price, price, price, price, price, null));
  }

  /** Apple's real quote from 2026-10-29, with the price and previous close varied per test. */
  private static Quote appleQuote(String price, String previousClose) {
    return new Quote("AAPL", Money.of(price), Money.of("348.00"), Money.of("351.43"),
        Money.of("347.30"), Money.of(previousClose), TIME);
  }
}
