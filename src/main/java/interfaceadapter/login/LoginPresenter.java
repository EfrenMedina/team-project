package interfaceadapter.login;

import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import java.util.Objects;
import usecase.login.LoginOutputBoundary;
import usecase.login.LoginOutputData;

/**
 * Turns the login use case's result into what the screens show: the error message on the
 * Log-in form on failure, or the Dashboard with the user's name in the sidebar on success.
 */
public class LoginPresenter implements LoginOutputBoundary {

  /** The Log-in form's state, which this presenter updates. */
  private final LoginViewModel loginViewModel;

  /** Who is logged in, shown in the sidebar. */
  private final LoggedInViewModel loggedInViewModel;

  /** Which screen is showing. */
  private final ViewManagerModel viewManagerModel;

  /**
   * Creates the presenter.
   *
   * @param loginViewModel the Log-in form's view model
   * @param loggedInViewModel holds who is logged in
   * @param viewManagerModel switches screens
   * @throws NullPointerException if any argument is null
   */
  public LoginPresenter(
      LoginViewModel loginViewModel,
      LoggedInViewModel loggedInViewModel,
      ViewManagerModel viewManagerModel) {
    this.loginViewModel = Objects.requireNonNull(loginViewModel, "loginViewModel");
    this.loggedInViewModel = Objects.requireNonNull(loggedInViewModel, "loggedInViewModel");
    this.viewManagerModel = Objects.requireNonNull(viewManagerModel, "viewManagerModel");
  }

  /**
   * Records who logged in, clears any earlier error and shows the Dashboard.
   *
   * @param outputData the logged-in user's details
   */
  @Override
  public void prepareSuccessView(LoginOutputData outputData) {
    String username = outputData.username();
    loginViewModel.setState(new LoginState(username, null, username));
    loggedInViewModel.setState(username);
    viewManagerModel.setState(ViewNames.DASHBOARD);
    viewManagerModel.firePropertyChanged();
  }

  /**
   * Shows the error and keeps the username the user typed, so they can correct it. Stays on
   * the Log-in form.
   *
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String error) {
    LoginState current = loginViewModel.getState();
    loginViewModel.setState(new LoginState(current.username(), error, null));
  }
}
