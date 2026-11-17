package interfaceadapter.signup;

/**
 * What the Sign-up screen shows. The passwords are deliberately not part of it: the screen
 * hands them straight to the controller, so they are never kept anywhere after sign-up.
 *
 * @param username the text in the username field
 * @param error the message to show under the form, or {@code null} for none
 * @param signedUpUsername the username of the user who just signed up, or {@code null} if
 *     sign-up hasn't succeeded
 * @param startingCash the new account's cash, formatted for display (for example
 *     {@code "$100,000.00"}), or {@code null} if sign-up hasn't succeeded
 */
public record SignupState(
    String username, String error, String signedUpUsername, String startingCash) {

  /** An empty form with no error and no new user, as shown when the screen opens. */
  public static final SignupState EMPTY = new SignupState("", null, null, null);
}
