package interfaceadapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.login.LoginInputData;

class LoginControllerTest {

  @Test
  void execute_passesTypedValuesToUseCase() {
    // Inputs
    List<LoginInputData> received = new ArrayList<>();
    LoginController controller = new LoginController(received::add);

    // Action
    controller.execute("alice", "hunter222");

    // Assertion
    assertEquals(List.of(new LoginInputData("alice", "hunter222")), received);
  }
}
