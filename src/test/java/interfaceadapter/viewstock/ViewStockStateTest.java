package interfaceadapter.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interfaceadapter.viewstock.ViewStockState.ChartPoint;
import interfaceadapter.viewstock.ViewStockState.KeyStat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.viewstock.ChartRange;

class ViewStockStateTest {

  @Test
  void empty_beforeFirstLoad_hasNoTextNoPointsAndNoError() {
    // Inputs
    ViewStockState empty = ViewStockState.EMPTY;

    // Assertion
    assertEquals("", empty.ticker());
    assertEquals(ChartRange.ONE_MONTH, empty.range());
    assertEquals("", empty.price());
    assertTrue(empty.keyStats().isEmpty());
    assertTrue(empty.chart().isEmpty());
    assertNull(empty.error());
  }

  @Test
  void constructor_listsChangedAfterwards_keepsOriginalLists() {
    // Inputs
    List<KeyStat> keyStats = new ArrayList<>(List.of(new KeyStat("Open", "$346.72")));
    List<ChartPoint> chart =
        new ArrayList<>(List.of(new ChartPoint(LocalDate.of(2026, 10, 30), new BigDecimal("1"))));

    // Action
    ViewStockState state = new ViewStockState("AAPL", ChartRange.ONE_MONTH, "Apple Inc.",
        "AAPL · NASDAQ", "$344.63", "−$4.28 (−1.23%)", false, keyStats, chart, null);
    keyStats.clear();
    chart.clear();

    // Assertion
    assertEquals(1, state.keyStats().size());
    assertEquals(1, state.chart().size());
  }

  @Test
  void constructor_nullText_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ViewStockState("AAPL",
        ChartRange.ONE_MONTH, "Apple Inc.", "AAPL · NASDAQ", null, "", true, List.of(),
        List.of(), null));
  }

  @Test
  void constructor_nullRange_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ViewStockState("AAPL", null, "", "", "",
        "", true, List.of(), List.of(), null));
  }

  @Test
  void keyStat_nullValue_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new KeyStat("Open", null));
  }

  @Test
  void chartPoint_nullClose_throws() {
    // Inputs
    LocalDate day = LocalDate.of(2026, 10, 30);

    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ChartPoint(day, null));
  }
}
