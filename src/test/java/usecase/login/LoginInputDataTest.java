package usecase.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class LoginInputDataTest {

  @Test
  void toString_hidesPassword() {
    // Inputs
    LoginInputData input = new LoginInputData("alice", "hunter222");

    // Action
    String text = input.toString();

    // Assertion
    assertEquals("LoginInputData[username=alice, password=***]", text);
    assertFalse(text.contains("hunter222"));
  }
}
