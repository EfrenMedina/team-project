package usecase.login;

import entity.User;
import java.util.Optional;

/**
 * The storage the login use case needs: finding a user by the username they typed.
 * Implemented in the data access layer, in memory for now and with SQLite later.
 *
 * <p>Usernames are compared ignoring case, the same way sign-up checks that they are free.
 */
public interface LoginUserDataAccessInterface {

  /**
   * Finds the user with this username, ignoring case.
   *
   * @param username the username to look for
   * @return the user, or an empty {@code Optional} if nobody has that username
   */
  Optional<User> findByUsername(String username);
}
