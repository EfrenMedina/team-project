package interfaceadapter.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LogoutControllerTest {

  @Test
  void execute_click_callsUseCaseOnce() {
    // Inputs
    AtomicInteger calls = new AtomicInteger();
    LogoutController controller = new LogoutController(calls::incrementAndGet);

    // Action
    controller.execute();

    // Assertion
    assertEquals(1, calls.get());
  }

  @Test
  void constructor_nullUseCase_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LogoutController(null));
  }
}
