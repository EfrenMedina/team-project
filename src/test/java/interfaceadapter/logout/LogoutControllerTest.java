package interfaceadapter.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LogoutControllerTest {

  @Test
  void execute_callsUseCaseOnce() {
    // Inputs
    AtomicInteger calls = new AtomicInteger();
    LogoutController controller = new LogoutController(calls::incrementAndGet);

    // Action
    controller.execute();

    // Assertion
    assertEquals(1, calls.get());
  }
}
