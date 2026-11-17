package usecase.login;

/**
 * What the user typed into the login form. The values are passed exactly as typed; the login
 * use case decides whether they match a user.
 *
 * @param username the username the user typed
 * @param password the password the user typed
 */
public record LoginInputData(String username, String password) {

  /**
   * Returns a description for logs and debugging, with the password hidden.
   *
   * @return the username, with the password replaced by {@code ***}
   */
  @Override
  public String toString() {
    return "LoginInputData[username=" + username + ", password=***]";
  }
}
