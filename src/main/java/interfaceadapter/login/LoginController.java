package interfaceadapter.login;

import java.util.Objects;
import usecase.login.LoginInputBoundary;
import usecase.login.LoginInputData;

/**
 * Turns the Login screen's "Log in" click into a call to the login use case.
 */
public class LoginController {

  /** The login use case. */
  private final LoginInputBoundary loginInteractor;

  /**
   * Creates the controller.
   *
   * @param loginInteractor the login use case to call
   * @throws NullPointerException if {@code loginInteractor} is null
   */
  public LoginController(LoginInputBoundary loginInteractor) {
    this.loginInteractor = Objects.requireNonNull(loginInteractor, "loginInteractor");
  }

  /**
   * Tries to log in with what the user typed.
   *
   * @param username the text in the username field
   * @param password the text in the password field
   */
  public void execute(String username, String password) {
    loginInteractor.execute(new LoginInputData(username, password));
  }
}
