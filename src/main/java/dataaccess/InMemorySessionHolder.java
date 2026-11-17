package dataaccess;

import entity.User;
import java.util.Objects;
import java.util.Optional;
import usecase.shared.SessionHolder;

/**
 * Keeps the logged-in user in memory. The session ends when the app closes, so users log in
 * again every time they open it.
 */
public class InMemorySessionHolder implements SessionHolder {

  /** The logged-in user, or {@code null} if nobody is logged in. */
  private User currentUser;

  /**
   * Starts a session for a user, replacing any session that was already open.
   *
   * @param user the user who just logged in
   * @throws NullPointerException if {@code user} is null
   */
  @Override
  public void start(User user) {
    currentUser = Objects.requireNonNull(user, "user");
  }

  /**
   * Returns the logged-in user.
   *
   * @return the user whose session is open, or an empty {@code Optional} if nobody is logged in
   */
  @Override
  public Optional<User> currentUser() {
    return Optional.ofNullable(currentUser);
  }

  /** Ends the session, so nobody is logged in. Does nothing if nobody was logged in. */
  @Override
  public void clear() {
    currentUser = null;
  }
}
