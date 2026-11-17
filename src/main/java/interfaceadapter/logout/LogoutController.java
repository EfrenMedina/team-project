package interfaceadapter.logout;

import java.util.Objects;
import usecase.logout.LogoutInputBoundary;

/**
 * Turns the Dashboard's "Log out" click into a call to the logout use case.
 */
public class LogoutController {

  /** The logout use case. */
  private final LogoutInputBoundary logoutInteractor;

  /**
   * Creates the controller.
   *
   * @param logoutInteractor the logout use case to call
   * @throws NullPointerException if {@code logoutInteractor} is null
   */
  public LogoutController(LogoutInputBoundary logoutInteractor) {
    this.logoutInteractor = Objects.requireNonNull(logoutInteractor, "logoutInteractor");
  }

  /** Logs out whoever is logged in. */
  public void execute() {
    logoutInteractor.execute();
  }
}
