package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void add_tenAndTwentyCents_isExactlyThirtyCents() {
    // Inputs
    Money ten = Money.of("0.10");
    Money twenty = Money.of("0.20");

    // Action
    Money result = ten.add(twenty);

    // Assertion
    assertEquals(Money.of("0.30"), result);
  }

  @Test
  void equals_sameAmountMadeTwoWays_isTrue() {
    // Inputs
    Money fromCents = Money.ofCents(15000);
    Money fromString = Money.of("150.00");

    // Action / Assertion
    assertEquals(fromCents, fromString);
  }

  @Test
  void ofCents_thenToCents_roundTrips() {
    // Inputs
    Money money = Money.ofCents(15000);

    // Action
    long cents = money.toCents();

    // Assertion
    assertEquals(15000L, cents);
  }

  @Test
  void of_threeDecimals_throws() {
    // Inputs
    String amount = "1.005";

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> Money.of(amount));
  }

  @Test
  void of_letters_throws() {
    // Inputs
    String amount = "abc";

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> Money.of(amount));
  }

  @Test
  void of_null_throws() {
    // Inputs
    String amount = null;

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> Money.of(amount));
  }

  @Test
  void of_blank_throws() {
    // Inputs
    String amount = "   ";

    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> Money.of(amount));
  }

  @Test
  void of_trailingZeros_isAccepted() {
    // Inputs
    String amount = "1.500";

    // Action
    Money money = Money.of(amount);

    // Assertion
    assertEquals(Money.of("1.50"), money);
  }

  @Test
  void subtract_largerFromSmaller_isNegative() {
    // Inputs
    Money small = Money.of("1.00");
    Money large = Money.of("2.50");

    // Action
    Money result = small.subtract(large);

    // Assertion
    assertEquals(Money.of("-1.50"), result);
    assertTrue(result.isNegative());
  }

  @Test
  void multiply_priceByQuantity_isExact() {
    // Inputs
    Money price = Money.of("150.00");

    // Action
    Money total = price.multiply(10);

    // Assertion
    assertEquals(Money.of("1500.00"), total);
  }

  @Test
  void isPositive_positiveAmount_isTrue() {
    // Inputs
    Money oneCent = Money.ofCents(1);

    // Action / Assertion
    assertTrue(oneCent.isPositive());
  }

  @Test
  void isZero_zero_isTrue() {
    // Inputs
    Money zero = Money.ZERO;

    // Action / Assertion
    assertTrue(zero.isZero());
  }

  @Test
  void compareTo_smallerAmount_isNegative() {
    // Inputs
    Money one = Money.of("1.00");
    Money two = Money.of("2.00");

    // Action
    int result = one.compareTo(two);

    // Assertion
    assertTrue(result < 0);
  }

  @Test
  void equals_sameObject_isTrue() {
    // Inputs
    Money money = Money.of("1.00");

    // Action / Assertion
    assertTrue(money.equals(money));
  }

  @Test
  void equals_notMoney_isFalse() {
    // Inputs
    Money money = Money.of("1.00");

    // Action / Assertion
    assertFalse(money.equals("1.00"));
  }

  @Test
  void equals_differentAmount_isFalse() {
    // Inputs
    Money one = Money.of("1.00");
    Money two = Money.of("2.00");

    // Action / Assertion
    assertFalse(one.equals(two));
  }

  @Test
  void hashCode_equalAmounts_areEqual() {
    // Inputs
    Money fromCents = Money.ofCents(15000);
    Money fromString = Money.of("150.00");

    // Action / Assertion
    assertEquals(fromCents.hashCode(), fromString.hashCode());
  }
}
