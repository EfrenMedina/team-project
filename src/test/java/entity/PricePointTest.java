package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricePointTest {

  private static final LocalDate DAY = LocalDate.of(2026, 10, 29);

  @Test
  void constructor_validValues_keepsValues() {
    // Action
    PricePoint point = new PricePoint(DAY, Money.of("348.00"), Money.of("351.43"),
        Money.of("347.30"), Money.of("348.91"), 41_340_227L);

    // Assertion
    assertEquals(DAY, point.date());
    assertEquals(Money.of("348.00"), point.open());
    assertEquals(Money.of("351.43"), point.high());
    assertEquals(Money.of("347.30"), point.low());
    assertEquals(Money.of("348.91"), point.close());
    assertEquals(41_340_227L, point.volume());
  }

  @Test
  void constructor_zeroVolume_isAllowed() {
    // Inputs
    Money price = Money.of("348.91");

    // Action
    PricePoint point = new PricePoint(DAY, price, price, price, price, 0);

    // Assertion
    assertEquals(0, point.volume());
  }

  @Test
  void constructor_negativeVolume_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, price, price, price, price, -1));
  }

  @Test
  void constructor_highBelowLow_throws() {
    // Inputs
    Money price = Money.of("345.00");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, price, Money.of("340.00"), Money.of("350.00"), price, 100));
  }

  @Test
  void constructor_zeroClose_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, price, price, price, Money.ZERO, 100));
  }

  @Test
  void constructor_nullDate_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new PricePoint(null, price, price, price, price, 100));
  }

  @Test
  void constructor_nullClose_throws() {
    // Inputs
    Money price = Money.of("348.91");

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new PricePoint(DAY, price, price, price, null, 100));
  }
}
