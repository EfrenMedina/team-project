package interfaceadapter.logout;

import interfaceadapter.ClearedOnLogout;
import java.util.List;
import java.util.Objects;
import usecase.logout.LogoutOutputBoundary;

/**
 * Wipes the logged-out user's data from every screen.
 *
 * <p>When the app shell from SCRUM-23 adds a {@code ViewManagerModel}, this should also switch
 * back to the Login screen there.
 */
public class LogoutPresenter implements LogoutOutputBoundary {

  /** Every view model that holds user data, cleared on logout. */
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
