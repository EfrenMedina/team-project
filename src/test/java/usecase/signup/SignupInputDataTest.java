package usecase.signup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SignupInputDataTest {

  @Test
  void toString_anyInput_hidesBothPasswords() {
    // Inputs
    SignupInputData input = new SignupInputData("alice", "hunter222", "hunter223");

    // Action
    String text = input.toString();

    // Assertion
    assertFalse(text.contains("hunter222"));
    assertFalse(text.contains("hunter223"));
    assertTrue(text.contains("alice"));
  }
}
