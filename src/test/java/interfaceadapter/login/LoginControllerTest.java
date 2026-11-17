package interfaceadapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.login.LoginInputData;

class LoginControllerTest {

  @Test
  void execute_typedValues_passesThemToUseCase() {
    // Inputs
    List<LoginInputData> received = new ArrayList<>();
    LoginController controller = new LoginController(received::add);

    // Action
    controller.execute("alice", "hunter222");

    // Assertion
    assertEquals(List.of(new LoginInputData("alice", "hunter222")), received);
  }

  @Test
  void constructor_nullUseCase_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LoginController(null));
  }
}
