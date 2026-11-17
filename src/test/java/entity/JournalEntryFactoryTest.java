package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class JournalEntryFactoryTest {

  private static final Instant NOW = Instant.parse("2026-11-17T14:30:00Z");

  @Test
  void forOpeningDeposit_hundredThousand_putsItInCashAndContributedCapital() {
    // Inputs
    Money amount = Money.of("100000.00");

    // Action
    JournalEntry entry = JournalEntryFactory.forOpeningDeposit("entry-1", "acc-1", NOW, amount);

    // Assertion
    assertEquals("entry-1", entry.id());
    assertEquals("acc-1", entry.accountId());
    assertEquals(NOW, entry.postedAt());
    List<JournalEntry> entries = List.of(entry);
    assertEquals(amount, LedgerBalance.balanceOf(LedgerAccount.CASH, entries));
    assertEquals(amount, LedgerBalance.balanceOf(LedgerAccount.CONTRIBUTED_CAPITAL, entries));
  }

  @Test
  void forOpeningDeposit_zeroAmount_throws() {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> JournalEntryFactory.forOpeningDeposit("entry-1", "acc-1", NOW, Money.ZERO));
  }
}
