package usecase.login;

import entity.User;

/**
 * Work that has to happen right after a user logs in, before their Dashboard is shown.
 *
 * <p>This is where SCRUM-41 (#32) plugs in the open-order check: limit orders whose price was
 * reached while the user was away are filled as soon as they log back in. Until then the app
 * uses {@link #NONE}.
 */
public interface AfterLoginStep {

  /** A step that does nothing, for when there's no work to do after login. */
  AfterLoginStep NONE = user -> { };

  /**
   * Runs the step for the user who just logged in. Their session has already started.
   *
   * @param user the user who just logged in
   */
  void afterLogin(User user);
}
