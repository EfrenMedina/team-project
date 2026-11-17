package usecase.signup;

/**
 * What the user typed into the sign-up form. The values are passed exactly as typed; the
 * sign-up use case decides whether they are valid.
 *
 * @param username the requested username
 * @param password the chosen password
 * @param repeatPassword the password typed a second time, to catch typos
 */
public record SignupInputData(String username, String password, String repeatPassword) {

  /**
   * Returns a description for logs and debugging, with both passwords hidden.
   *
   * @return the username, with the passwords replaced by {@code ***}
   */
  @Override
  public String toString() {
    return "SignupInputData[username=" + username + ", password=***, repeatPassword=***]";
  }
}
