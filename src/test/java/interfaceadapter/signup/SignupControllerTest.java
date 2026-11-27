package interfaceadapter.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import usecase.signup.SignupInputData;

class SignupControllerTest {

  @Test
  void execute_typedValues_passesThemToUseCase() {
    // Inputs
    List<SignupInputData> received = new ArrayList<>();
    SignupController controller = new SignupController(received::add);

    // Action
    controller.execute("alice", "hunter222", "hunter223");

    // Assertion
    assertEquals(List.of(new SignupInputData("alice", "hunter222", "hunter223")), received);
  }

  @Test
  void constructor_nullUseCase_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new SignupController(null));
  }
}
