package dataaccess;

import entity.Account;
import entity.JournalEntry;
import entity.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import usecase.signup.SignupUserDataAccessInterface;

/**
 * Keeps users, their accounts and their journal entries in memory, so nothing is saved after
 * the app closes. Used until the SQLite version arrives in SCRUM-27, and in tests, where it
 * needs no files.
 *
 * <p>Users are stored under their lowercase username, so "Alice" and "alice" count as the
 * same name.
 */
public class InMemoryUserDataAccessObject implements SignupUserDataAccessInterface {

  /** Every saved user, keyed by lowercase username. */
  private final Map<String, User> usersByUsername = new HashMap<>();

  /** Every saved account, keyed by account id. */
  private final Map<String, Account> accountsById = new HashMap<>();

  /** Every journal entry, in the order it was saved. */
  private final List<JournalEntry> entries = new ArrayList<>();

  /**
   * Returns whether a user with this username already exists, ignoring case.
   *
   * @param username the username to look for
   * @return true if the username is taken
   */
  @Override
  public boolean existsByUsername(String username) {
    return usersByUsername.containsKey(key(username));
  }

  /**
   * Saves a new user together with their account and opening deposit. All three are saved,
   * or none is.
   *
   * @param user the user to save
   * @param account the user's trading account
   * @param openingDeposit the journal entry that puts the starting cash in the account
   * @throws IllegalStateException if a user with the same username (ignoring case) already
   *     exists; nothing is saved
   */
  @Override
  public void saveNewUser(User user, Account account, JournalEntry openingDeposit) {
    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(account, "account");
    Objects.requireNonNull(openingDeposit, "openingDeposit");
    String key = key(user.username());
    // Check before storing anything, so a clash leaves nothing half-saved.
    if (usersByUsername.containsKey(key)) {
      throw new IllegalStateException("Username already taken: " + user.username());
    }
    usersByUsername.put(key, user);
    accountsById.put(account.getId(), account);
    entries.add(openingDeposit);
  }

  /**
   * Returns the map key for a username.
   *
   * @param username the username as typed
   * @return the username in lowercase
   */
  private static String key(String username) {
    // Locale.ROOT gives the same result on every computer; in a Turkish locale, for
    // example, "I".toLowerCase() is a dotless "ı", not "i".
    return username.toLowerCase(Locale.ROOT);
  }
}
