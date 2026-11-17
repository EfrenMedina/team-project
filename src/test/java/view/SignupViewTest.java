package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.signup.SignupInputData;

class SignupViewTest {

  private SignupViewModel viewModel;
  private SignupView view;
  private List<SignupInputData> received;

  @BeforeEach
  void setUp() {
    viewModel = new SignupViewModel();
    view = new SignupView(viewModel);
    received = new ArrayList<>();
    view.setSignupController(new SignupController(received::add));
  }

  @Test
  void createAccount_typedValues_sendsThemAndClearsPasswords() {
    // Inputs
    view.usernameField.setText("alice");
    view.passwordField.setText("hunter222");
    view.repeatPasswordField.setText("hunter222");

    // Action
    view.createAccountButton.doClick();

    // Assertion
    assertEquals(List.of(new SignupInputData("alice", "hunter222", "hunter222")), received);
    assertEquals(0, view.passwordField.getPassword().length);
    assertEquals(0, view.repeatPasswordField.getPassword().length);
  }

  @Test
  void stateWithError_shown_displaysErrorUnderUsername() {
    // Action
    viewModel.setState(new SignupState("alice", "That username is already taken.", null, null));

    // Assertion
    assertEquals("! That username is already taken.", view.errorLabel.getText());
    assertEquals("alice", view.usernameField.getText());
  }

  @Test
  void stateAfterSignup_shown_displaysWelcomeWithCash() {
    // Action
    viewModel.setState(new SignupState("alice", null, "alice", "$100,000.00"));

    // Assertion
    assertTrue(view.successLabel.getText().contains("$100,000.00"));
    assertEquals(" ", view.errorLabel.getText());
  }
}
