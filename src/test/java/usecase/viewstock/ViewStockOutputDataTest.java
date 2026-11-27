package usecase.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.CompanyProfile;
import entity.Money;
import entity.PricePoint;
import entity.Quote;
import entity.Stock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewStockOutputDataTest {

  private static final Quote QUOTE =
      new Quote("AAPL", Money.of("344.63"), Money.of("346.72"), Money.of("349.19"),
          Money.of("334.35"), Money.of("348.91"), Instant.ofEpochSecond(1793390400L));
  private static final CompanyProfile PROFILE =
      new CompanyProfile(
          new Stock("AAPL", "Apple Inc.", "NASDAQ"), "Technology", Money.of("1000.00"));
  private static final PricePoint POINT =
      new PricePoint(LocalDate.of(2026, 10, 29), Money.of("348.00"), Money.of("351.43"),
          Money.of("347.30"), Money.of("348.91"), 42_582_100L);

  @Test
  void constructor_historyChangedAfterwards_keepsOriginalHistory() {
    // Inputs
    List<PricePoint> history = new ArrayList<>(List.of(POINT));

    // Action
    ViewStockOutputData output =
        new ViewStockOutputData(QUOTE, PROFILE, ChartRange.ONE_MONTH, history);
    history.clear();

    // Assertion
    assertEquals(List.of(POINT), output.history());
  }

  @Test
  void history_returnedList_cannotBeChanged() {
    // Inputs
    ViewStockOutputData output =
        new ViewStockOutputData(QUOTE, PROFILE, ChartRange.ONE_MONTH, List.of(POINT));
    List<PricePoint> history = output.history();

    // Action / Assertion
    assertThrows(UnsupportedOperationException.class, history::clear);
  }

  @Test
  void constructor_emptyHistory_isAllowed() {
    // Action
    ViewStockOutputData output =
        new ViewStockOutputData(QUOTE, PROFILE, ChartRange.ONE_MONTH, List.of());

    // Assertion
    assertEquals(List.of(), output.history());
  }

  @Test
  void constructor_nullQuote_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new ViewStockOutputData(null, PROFILE, ChartRange.ONE_MONTH, List.of()));
  }

  @Test
  void constructor_nullProfile_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new ViewStockOutputData(QUOTE, null, ChartRange.ONE_MONTH, List.of()));
  }

  @Test
  void constructor_nullRange_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new ViewStockOutputData(QUOTE, PROFILE, null, List.of()));
  }

  @Test
  void constructor_nullHistory_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new ViewStockOutputData(QUOTE, PROFILE, ChartRange.ONE_MONTH, null));
  }
}
