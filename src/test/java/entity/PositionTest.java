package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PositionTest {

  // Constructor

  @Test
  void constructor_storesSharesAndCost() {
    // Inputs
    Position pos = new Position("AAPL", 10, Money.of("1000.00"));

    // Assertion
    assertEquals("AAPL", pos.getTicker());
    assertEquals(10L, pos.getQuantity());
    assertEquals(Money.of("1000.00"), pos.getTotalCost());
    assertEquals(Money.of("100.00"), pos.getAverageCost());
  }

  @Test
  void constructor_zeroQuantity_throws() {
    assertThrows(IllegalArgumentException.class,
        () -> new Position("AAPL", 0, Money.of("1000.00")));
  }

  @Test
  void constructor_zeroCost_throws() {
    assertThrows(IllegalArgumentException.class, () -> new Position("AAPL", 10, Money.ZERO));
  }

  // addShares

  @Test
  void addShares_blendsAverageCost() {
    // Inputs
    Position pos = new Position("AAPL", 10, Money.of("1000.00"));

    // Action
    pos.addShares(10, Money.of("1200.00"));

    // Assertion
    assertEquals(20L, pos.getQuantity());
    assertEquals(Money.of("2200.00"), pos.getTotalCost());
    assertEquals(Money.of("110.00"), pos.getAverageCost());
  }

  // removeShares

  @Test
  void removeShares_reducesCostInProportion() {
    // Inputs
    Position pos = new Position("AAPL", 20, Money.of("2200.00"));

    // Action
    pos.removeShares(5);

    // Assertion
    assertEquals(15L, pos.getQuantity());
    assertEquals(Money.of("1650.00"), pos.getTotalCost());
    assertEquals(Money.of("110.00"), pos.getAverageCost());
  }

  @Test
  void removeShares_all_leavesEmptyPositionWithNoCost() {
    // Inputs
    Position pos = new Position("AAPL", 10, Money.of("1000.00"));

    // Action
    pos.removeShares(10);

    // Assertion
    assertTrue(pos.isEmpty());
    assertEquals(Money.ZERO, pos.getTotalCost());
    assertEquals(Money.ZERO, pos.getAverageCost());
  }

  @Test
  void removeShares_moreThanHeld_throws() {
    // Inputs
    Position pos = new Position("AAPL", 10, Money.of("1000.00"));

    // Action and assertion
    assertThrows(IllegalArgumentException.class, () -> pos.removeShares(11));
    assertEquals(10L, pos.getQuantity());
  }

  @Test
  void removeShares_zero_throws() {
    // Inputs
    Position pos = new Position("AAPL", 10, Money.of("1000.00"));

    // Action and assertion
    assertThrows(IllegalArgumentException.class, () -> pos.removeShares(0));
  }
}
