package dataaccess;

import entity.User;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import usecase.signup.SignupUserDataAccessInterface;

/**
 * Keeps users in memory, so nothing is saved after the app closes. Used until the SQLite
 * version arrives in SCRUM-27, and in tests, where it needs no files.
 *
 * <p>Users are stored under their lowercase username, so "Alice" and "alice" count as the
 * same name.
 */
public class InMemoryUserDataAccessObject implements SignupUserDataAccessInterface {

  /** Every saved user, keyed by lowercase username. */
  private final Map<String, User> usersByUsername = new HashMap<>();

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
   * Saves a new user.
   *
   * @param user the user to save
   * @throws IllegalStateException if a user with the same username (ignoring case) already
   *     exists
   */
  @Override
  public void saveNewUser(User user) {
    Objects.requireNonNull(user, "user");
    String key = key(user.username());
    if (usersByUsername.containsKey(key)) {
      throw new IllegalStateException("Username already taken: " + user.username());
    }
    usersByUsername.put(key, user);
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
