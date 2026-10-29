package usecase.signup;

import entity.User;

/**
 * The storage the sign-up use case needs: checking whether a username is free and saving a
 * new user. Implemented in the data access layer, in memory for now and with SQLite later.
 *
 * <p>Usernames are compared ignoring case, so "Alice" and "alice" can't both exist.
 */
public interface SignupUserDataAccessInterface {

  /**
   * Returns whether a user with this username already exists, ignoring case.
   *
   * @param username the username to look for
   * @return true if the username is taken
   */
  boolean existsByUsername(String username);

  /**
   * Saves a new user. The caller must check that the username is free first.
   *
   * @param user the user to save
   * @throws IllegalStateException if a user with the same username (ignoring case) already
   *     exists
   */
  void saveNewUser(User user);
}
