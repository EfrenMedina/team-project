package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LedgerAccountTest {

  @Test
  void normalSide_assetsAndExpenses_areDebit() {
    // Inputs
    LedgerAccount cash = LedgerAccount.CASH;
    LedgerAccount investments = LedgerAccount.INVESTMENTS_AT_COST;
    LedgerAccount commission = LedgerAccount.COMMISSION_EXPENSE;

    // Action / Assertion
    assertEquals(EntrySide.DEBIT, cash.getNormalSide());
    assertEquals(EntrySide.DEBIT, investments.getNormalSide());
    assertEquals(EntrySide.DEBIT, commission.getNormalSide());
  }

  @Test
  void normalSide_equityAndIncome_areCredit() {
    // Inputs
    LedgerAccount capital = LedgerAccount.CONTRIBUTED_CAPITAL;
    LedgerAccount gains = LedgerAccount.REALIZED_GAINS;

    // Action / Assertion
    assertEquals(EntrySide.CREDIT, capital.getNormalSide());
    assertEquals(EntrySide.CREDIT, gains.getNormalSide());
  }
}
