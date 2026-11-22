package interfaceadapter.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.viewstock.ChartRange;
import usecase.viewstock.ViewStockInputData;

class ViewStockControllerTest {

  @Test
  void execute_tickerAndRange_passesThemToUseCase() {
    // Inputs
    List<ViewStockInputData> received = new ArrayList<>();
    ViewStockController controller = new ViewStockController(received::add);

    // Action
    controller.execute("AAPL", ChartRange.SIX_MONTHS);

    // Assertion
    assertEquals(List.of(new ViewStockInputData("AAPL", ChartRange.SIX_MONTHS)), received);
  }

  @Test
  void constructor_nullUseCase_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new ViewStockController(null));
  }
}
