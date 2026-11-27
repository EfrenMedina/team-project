package usecase.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ChartRangeTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

  @Test
  void startDate_oneMonth_isOneMonthBefore() {
    // Action / Assertion
    assertEquals(LocalDate.of(2026, 9, 30), ChartRange.ONE_MONTH.startDate(TODAY));
  }

  @Test
  void startDate_sixMonths_isSixMonthsBefore() {
    // Action / Assertion
    assertEquals(LocalDate.of(2026, 4, 30), ChartRange.SIX_MONTHS.startDate(TODAY));
  }

  @Test
  void startDate_oneYear_isOneYearBefore() {
    // Action / Assertion
    assertEquals(LocalDate.of(2025, 10, 30), ChartRange.ONE_YEAR.startDate(TODAY));
  }

  @Test
  void startDate_endOfLongerMonth_movesBackToEndOfShorterMonth() {
    // Action / Assertion: February 2026 has 28 days
    assertEquals(
        LocalDate.of(2026, 2, 28), ChartRange.ONE_MONTH.startDate(LocalDate.of(2026, 3, 31)));
  }

  @Test
  void startDate_nullToday_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> ChartRange.ONE_MONTH.startDate(null));
  }
}
