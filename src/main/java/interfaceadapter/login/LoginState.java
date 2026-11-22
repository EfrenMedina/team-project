package interfaceadapter.login;

/**
 * What the Login screen shows. The password is deliberately not part of it: the screen hands
 * it straight to the controller, so it is never kept anywhere after login.
 *
 * @param username the text in the username field
 * @param error the message to show under the form, or {@code null} for none
 * @param loggedInUsername the username of the user who just logged in, or {@code null} if
 *     nobody is logged in
 */
public record LoginState(String username, String error, String loggedInUsername) {

  /** An empty form with no error and nobody logged in, as shown when the app opens. */
  public static final LoginState EMPTY = new LoginState("", null, null);
}
