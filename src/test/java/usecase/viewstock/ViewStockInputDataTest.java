package usecase.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ViewStockInputDataTest {

  @Test
  void constructor_untrimmedTicker_keepsTickerAsGiven() {
    // Action
    ViewStockInputData input = new ViewStockInputData(" aapl ", ChartRange.ONE_MONTH);

    // Assertion: the use case trims it, not the input
    assertEquals(" aapl ", input.ticker());
    assertEquals(ChartRange.ONE_MONTH, input.range());
  }

  @Test
  void constructor_nullRange_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ViewStockInputData("AAPL", null));
  }
}
