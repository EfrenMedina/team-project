package interfaceadapter.login;

import java.util.Objects;
import usecase.login.LoginOutputBoundary;
import usecase.login.LoginOutputData;

/**
 * Turns the login use case's result into what the Login screen shows: the error message on
 * failure, or the logged-in username on success.
 */
public class LoginPresenter implements LoginOutputBoundary {

  /** The Login screen's state, which this presenter updates. */
  private final LoginViewModel loginViewModel;

  /**
   * Creates the presenter.
   *
   * @param loginViewModel the Login screen's view model
   * @throws NullPointerException if {@code loginViewModel} is null
   */
  public LoginPresenter(LoginViewModel loginViewModel) {
    this.loginViewModel = Objects.requireNonNull(loginViewModel, "loginViewModel");
  }

  /**
   * Shows who logged in, and clears any earlier error.
   *
   * @param outputData the logged-in user's details
   */
  @Override
  public void prepareSuccessView(LoginOutputData outputData) {
    String username = outputData.username();
    loginViewModel.setState(new LoginState(username, null, username));
  }

  /**
   * Shows the error and keeps the username the user typed, so they can correct it.
   *
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String error) {
    LoginState current = loginViewModel.getState();
    loginViewModel.setState(new LoginState(current.username(), error, null));
  }
}
