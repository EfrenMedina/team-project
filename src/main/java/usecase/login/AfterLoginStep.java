package usecase.login;

import entity.User;
import usecase.shared.StockDataUnavailableException;

/**
 * Work that has to happen right after a user logs in.
 *
 * <p>This is where SCRUM-41 plugs in the open-order check: limit orders whose price was
 * reached while the user was away are filled as soon as they log back in. Until then the app
 * uses {@link #NONE}.
 *
 * <p>The step runs only once login has fully succeeded: the session has started and the
 * presenter has been told. So nothing the step does, or fails to do, can leave a login half
 * done.
 */
public interface AfterLoginStep {

  /** A step that does nothing, for when there's no work to do after login. */
  AfterLoginStep NONE = user -> { };

  /**
   * Runs the step for the user who just logged in. Their session has already started and their
   * login has already been shown as successful.
   *
   * @param user the user who just logged in
   * @throws StockDataUnavailableException if the step needs prices that can't be fetched right
   *     now. Login still counts as successful; the step's work waits for a later chance, such as
   *     the next price refresh.
   */
  void afterLogin(User user) throws StockDataUnavailableException;
}
