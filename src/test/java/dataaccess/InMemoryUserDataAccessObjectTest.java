package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.User;
import org.junit.jupiter.api.Test;

class InMemoryUserDataAccessObjectTest {

  private final InMemoryUserDataAccessObject dataAccess = new InMemoryUserDataAccessObject();

  @Test
  void existsByUsername_noUsers_returnsFalse() {
    // Action / Assertion
    assertFalse(dataAccess.existsByUsername("alice"));
  }

  @Test
  void saveNewUser_thenExistsByUsername_returnsTrue() {
    // Inputs
    User alice = new User("user-1", "alice", "hash");

    // Action
    dataAccess.saveNewUser(alice);

    // Assertion
    assertTrue(dataAccess.existsByUsername("alice"));
    assertFalse(dataAccess.existsByUsername("bob"));
  }

  @Test
  void existsByUsername_differentCase_returnsTrue() {
    // Inputs
    dataAccess.saveNewUser(new User("user-1", "Alice", "hash"));

    // Action / Assertion
    assertTrue(dataAccess.existsByUsername("aLiCe"));
  }

  @Test
  void saveNewUser_sameNameInOtherCase_throws() {
    // Inputs
    dataAccess.saveNewUser(new User("user-1", "Alice", "hash"));
    User duplicate = new User("user-2", "ALICE", "hash");

    // Action / Assertion
    assertThrows(IllegalStateException.class, () -> dataAccess.saveNewUser(duplicate));
  }

  @Test
  void saveNewUser_differentNames_savesBoth() {
    // Inputs
    dataAccess.saveNewUser(new User("user-1", "alice", "hash"));
    User bob = new User("user-2", "bob", "hash");

    // Action / Assertion
    assertDoesNotThrow(() -> dataAccess.saveNewUser(bob));
    assertTrue(dataAccess.existsByUsername("bob"));
  }

  @Test
  void saveNewUser_null_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> dataAccess.saveNewUser(null));
  }
}
