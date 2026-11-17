package interfaceadapter.login;

import java.util.Objects;
import usecase.login.LoginOutputBoundary;
import usecase.login.LoginOutputData;

/**
 * Shows the outcome of a login on the Login screen.
 *
 * <p>When the app shell from SCRUM-23 adds a {@code ViewManagerModel}, the success case should
 * also switch to the Dashboard there.
 */
public class LoginPresenter implements LoginOutputBoundary {

  /** The Login screen's view model. */
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
   * Records who logged in and removes any old error.
   *
   * @param outputData the logged-in user's details
   */
  @Override
  public void prepareSuccessView(LoginOutputData outputData) {
    String username = outputData.username();
    loginViewModel.setState(new LoginState(username, null, username));
  }

  /**
   * Shows the error under the form, keeping the username the user typed.
   *
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String error) {
    LoginState current = loginViewModel.getState();
    loginViewModel.setState(new LoginState(current.username(), error, null));
  }
}
