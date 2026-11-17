package dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemorySessionHolderTest {

  private static final User ALICE = new User("user-1", "alice", "hash");
  private static final User BOB = new User("user-2", "bob", "hash");

  private final InMemorySessionHolder session = new InMemorySessionHolder();

  @Test
  void currentUser_newHolder_isEmpty() {
    // Action / Assertion
    assertTrue(session.currentUser().isEmpty());
  }

  @Test
  void start_thenCurrentUser_returnsThatUser() {
    // Action
    session.start(ALICE);

    // Assertion
    assertEquals(Optional.of(ALICE), session.currentUser());
  }

  @Test
  void start_whileAnotherUserIsLoggedIn_replacesThem() {
    // Inputs
    session.start(ALICE);

    // Action
    session.start(BOB);

    // Assertion
    assertEquals(Optional.of(BOB), session.currentUser());
  }

  @Test
  void clear_afterStart_leavesNobodyLoggedIn() {
    // Inputs
    session.start(ALICE);

    // Action
    session.clear();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
  }

  @Test
  void clear_nobodyLoggedIn_doesNothing() {
    // Action
    session.clear();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
  }

  @Test
  void start_nullUser_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> session.start(null));
  }
}
