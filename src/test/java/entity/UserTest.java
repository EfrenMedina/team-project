package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  private static final String HASH = "pbkdf2$1000$c2FsdA==$aGFzaA==";

  @Test
  void constructor_validValues_keepsThem() {
    // Action
    User user = new User("user-1", "alice", HASH);

    // Assertion
    assertEquals("user-1", user.id());
    assertEquals("alice", user.username());
    assertEquals(HASH, user.passwordHash());
  }

  @Test
  void constructor_nullComponent_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new User(null, "alice", HASH));
    assertThrows(NullPointerException.class, () -> new User("user-1", null, HASH));
    assertThrows(NullPointerException.class, () -> new User("user-1", "alice", null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void constructor_blankUsername_throws(String username) {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> new User("user-1", username, HASH));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void constructor_blankPasswordHash_throws(String passwordHash) {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new User("user-1", "alice", passwordHash));
  }

  @Test
  void toString_anyUser_hidesPasswordHash() {
    // Inputs
    User user = new User("user-1", "alice", HASH);

    // Action
    String text = user.toString();

    // Assertion
    assertFalse(text.contains(HASH));
    assertTrue(text.contains("alice"));
    assertTrue(text.contains("***"));
  }
}
