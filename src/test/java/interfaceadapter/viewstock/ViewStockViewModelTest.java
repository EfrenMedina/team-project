package interfaceadapter.viewstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.viewstock.ChartRange;

class ViewStockViewModelTest {

  private static final ViewStockState FAILED =
      new ViewStockState("AAPL", ChartRange.ONE_YEAR, "", "", "", "", true, List.of(), List.of(),
          "Prices are unavailable right now.");

  private ViewStockViewModel viewModel;
  private List<PropertyChangeEvent> events;

  @BeforeEach
  void setUp() {
    viewModel = new ViewStockViewModel();
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
  }

  @Test
  void getState_newViewModel_isEmpty() {
    // Action / Assertion
    assertSame(ViewStockState.EMPTY, viewModel.getState());
  }

  @Test
  void setState_newState_storesItAndNotifiesListener() {
    // Action
    viewModel.setState(FAILED);

    // Assertion
    assertSame(FAILED, viewModel.getState());
    assertEquals(1, events.size());
    assertEquals(ViewStockViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
    assertSame(FAILED, events.get(0).getNewValue());
  }

  @Test
  void setState_sameStateTwice_notifiesBothTimes() {
    // Action: the same error after a retry
    viewModel.setState(FAILED);
    viewModel.setState(FAILED);

    // Assertion
    assertEquals(2, events.size());
  }

  @Test
  void setState_null_throwsAndKeepsOldState() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> viewModel.setState(null));
    assertSame(ViewStockState.EMPTY, viewModel.getState());
    assertEquals(0, events.size());
  }
}
