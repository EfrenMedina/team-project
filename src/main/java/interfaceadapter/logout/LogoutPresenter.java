package interfaceadapter.logout;

import interfaceadapter.ClearedOnLogout;
import java.util.List;
import java.util.Objects;
import usecase.logout.LogoutOutputBoundary;

/**
 * Turns the logout use case's result into what the screens show: the logged-out user's data
 * is wiped from every one of them.
 *
 * <p>Going back to the Login screen belongs to the app shell from SCRUM-23: clearing the
 * Login screen's state sets its {@code loggedInUsername} back to {@code null}, which the shell
 * can listen for.
 */
public class LogoutPresenter implements LogoutOutputBoundary {

  /** Every screen's state that holds user data, which this presenter clears. */
  private final List<ClearedOnLogout> viewModelsToClear;

  /**
   * Creates the presenter.
   *
   * @param viewModelsToClear every view model that holds the user's data, including the Login
   *     screen's
   * @throws NullPointerException if the list or any item in it is null
   */
  public LogoutPresenter(List<ClearedOnLogout> viewModelsToClear) {
    this.viewModelsToClear = List.copyOf(Objects.requireNonNull(viewModelsToClear));
  }

  /** Clears the user's data from every screen. */
  @Override
  public void prepareSuccessView() {
    for (ClearedOnLogout viewModel : viewModelsToClear) {
      viewModel.clearUserData();
    }
  }
}
