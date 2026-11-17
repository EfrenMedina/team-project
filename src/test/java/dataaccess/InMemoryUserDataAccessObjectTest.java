package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Account;
import entity.JournalEntry;
import entity.JournalEntryFactory;
import entity.Money;
import entity.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InMemoryUserDataAccessObjectTest {

  private static final Instant NOW = Instant.parse("2026-11-17T14:30:00Z");

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
    dataAccess.saveNewUser(alice, accountOf(alice), depositFor(alice));

    // Assertion
    assertTrue(dataAccess.existsByUsername("alice"));
    assertFalse(dataAccess.existsByUsername("bob"));
  }

  @Test
  void existsByUsername_differentCase_returnsTrue() {
    // Inputs
    save(new User("user-1", "Alice", "hash"));

    // Action / Assertion
    assertTrue(dataAccess.existsByUsername("aLiCe"));
  }

  @Test
  void saveNewUser_sameNameInOtherCase_throws() {
    // Inputs
    save(new User("user-1", "Alice", "hash"));
    User duplicate = new User("user-2", "ALICE", "hash");

    // Action / Assertion
    assertThrows(IllegalStateException.class,
        () -> dataAccess.saveNewUser(duplicate, accountOf(duplicate), depositFor(duplicate)));
  }

  @Test
  void saveNewUser_differentNames_savesBoth() {
    // Inputs
    save(new User("user-1", "alice", "hash"));
    User bob = new User("user-2", "bob", "hash");

    // Action / Assertion
    assertDoesNotThrow(() -> dataAccess.saveNewUser(bob, accountOf(bob), depositFor(bob)));
    assertTrue(dataAccess.existsByUsername("bob"));
  }

  @Test
  void saveNewUser_nullArgument_throwsAndSavesNothing() {
    // Inputs
    User alice = new User("user-1", "alice", "hash");
    Account account = accountOf(alice);
    JournalEntry deposit = depositFor(alice);

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> dataAccess.saveNewUser(null, account, deposit));
    assertThrows(NullPointerException.class,
        () -> dataAccess.saveNewUser(alice, null, deposit));
    assertThrows(NullPointerException.class,
        () -> dataAccess.saveNewUser(alice, account, null));
    assertFalse(dataAccess.existsByUsername("alice"));
  }

  /** Saves a user with an account and an opening deposit made from their id. */
  private void save(User user) {
    dataAccess.saveNewUser(user, accountOf(user), depositFor(user));
  }

  private static Account accountOf(User user) {
    return new Account("acc-" + user.id(), user.id());
  }

  private static JournalEntry depositFor(User user) {
    return JournalEntryFactory.forOpeningDeposit(
        "entry-" + user.id(), "acc-" + user.id(), NOW, Money.of("100000.00"));
  }
}
