package interfaceadapter.signup;

import java.util.Objects;
import usecase.signup.SignupInputBoundary;
import usecase.signup.SignupInputData;

/**
 * Turns the Sign-up screen's "Create account" click into a call to the sign-up use case.
 */
public class SignupController {

  /** The sign-up use case. */
  private final SignupInputBoundary signupInteractor;

  /**
   * Creates the controller.
   *
   * @param signupInteractor the sign-up use case to call
   * @throws NullPointerException if {@code signupInteractor} is null
   */
  public SignupController(SignupInputBoundary signupInteractor) {
    this.signupInteractor = Objects.requireNonNull(signupInteractor, "signupInteractor");
  }

  /**
   * Tries to create an account with what the user typed.
   *
   * @param username the text in the username field
   * @param password the text in the password field
   * @param repeatPassword the text in the confirm-password field
   */
  public void execute(String username, String password, String repeatPassword) {
    signupInteractor.execute(new SignupInputData(username, password, repeatPassword));
  }
}
