package usecase.shared;

import entity.User;
import java.util.Optional;

/**
 * Remembers who is logged in. The login use case starts a session, the logout use case ends
 * it, and every other use case asks it whose portfolio to work with.
 *
 * <p>At most one user is logged in at a time. Starting a new session replaces the old one.
 */
public interface SessionHolder {

  /**
   * Starts a session for a user, replacing any session that was already open.
   *
   * @param user the user who just logged in
   * @throws NullPointerException if {@code user} is null
   */
  void start(User user);

  /**
   * Returns the logged-in user.
   *
   * @return the user whose session is open, or an empty {@code Optional} if nobody is logged in
   */
  Optional<User> currentUser();

  /** Ends the session, so nobody is logged in. Does nothing if nobody was logged in. */
  void clear();
}
