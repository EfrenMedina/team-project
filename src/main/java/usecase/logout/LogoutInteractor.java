package usecase.logout;

import java.util.Objects;
import usecase.shared.SessionHolder;

/**
 * Logs the current user out: ends their session and tells the presenter, which clears the
 * user's data from the screens and shows Login again.
 *
 * <p>Logging out when nobody is logged in is allowed and gives the same result, so pressing
 * Log out twice is harmless.
 */
public class LogoutInteractor implements LogoutInputBoundary {

  /** Ends the session of the logged-in user. */
  private final SessionHolder sessionHolder;

  /** Receives the outcome. */
  private final LogoutOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param sessionHolder holds the session to end
   * @param presenter receives the outcome
   * @throws NullPointerException if any argument is null
   */
  public LogoutInteractor(SessionHolder sessionHolder, LogoutOutputBoundary presenter) {
    this.sessionHolder = Objects.requireNonNull(sessionHolder, "sessionHolder");
    this.presenter = Objects.requireNonNull(presenter, "presenter");
  }

  /** Ends the session and reports success. */
  @Override
  public void execute() {
    sessionHolder.clear();
    presenter.prepareSuccessView();
  }
}
