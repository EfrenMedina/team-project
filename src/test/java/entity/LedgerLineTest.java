package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LedgerLineTest {

  @Test
  void constructor_positiveAmount_keepsValues() {
    // Inputs
    Money amount = Money.of("849.00");

    // Action
    LedgerLine line = new LedgerLine(LedgerAccount.CASH, EntrySide.DEBIT, amount);

    // Assertion
    assertEquals(LedgerAccount.CASH, line.account());
    assertEquals(EntrySide.DEBIT, line.side());
    assertEquals(amount, line.amount());
  }

  @Test
  void constructor_zeroAmount_throws() {
    // Inputs
    Money zero = Money.ZERO;

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new LedgerLine(LedgerAccount.CASH, EntrySide.DEBIT, zero));
  }

  @Test
  void constructor_negativeAmount_throws() {
    // Inputs
    Money negative = Money.ofCents(-100);

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new LedgerLine(LedgerAccount.CASH, EntrySide.DEBIT, negative));
  }

  @Test
  void constructor_nullAccount_throws() {
    // Inputs
    Money amount = Money.ofCents(100);

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new LedgerLine(null, EntrySide.DEBIT, amount));
  }
}
