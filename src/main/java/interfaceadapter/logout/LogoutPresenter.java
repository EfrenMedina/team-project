package interfaceadapter.logout;

import interfaceadapter.ClearedOnLogout;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import java.util.List;
import java.util.Objects;
import usecase.logout.LogoutOutputBoundary;

/**
 * Turns the logout use case's result into what the screens show: the logged-out user's data
 * is wiped from every screen, and the Login screen is shown.
 */
public class LogoutPresenter implements LogoutOutputBoundary {

  /** Every screen's state that holds user data, which this presenter clears. */
  private final List<ClearedOnLogout> viewModelsToClear;

  /** Which screen is showing. */
  private final ViewManagerModel viewManagerModel;

  /**
   * Creates the presenter.
   *
   * @param viewModelsToClear every view model that holds the user's data, including the
   *     logged-in user and the Log-in and Sign-up forms
   * @param viewManagerModel switches screens
   * @throws NullPointerException if any argument, or any item in the list, is null
   */
  public LogoutPresenter(
      List<ClearedOnLogout> viewModelsToClear, ViewManagerModel viewManagerModel) {
    this.viewModelsToClear = List.copyOf(Objects.requireNonNull(viewModelsToClear));
    this.viewManagerModel = Objects.requireNonNull(viewManagerModel, "viewManagerModel");
  }

  /** Clears the user's data from every screen, then shows the Login screen. */
  @Override
  public void prepareSuccessView() {
    for (ClearedOnLogout viewModel : viewModelsToClear) {
      viewModel.clearUserData();
    }
    viewManagerModel.setState(ViewNames.LOGIN);
    viewManagerModel.firePropertyChanged();
  }
}
