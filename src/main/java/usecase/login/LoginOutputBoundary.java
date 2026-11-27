package usecase.login;

/**
 * Receives the outcome of the login use case. The presenter implements it to update the
 * screen; tests implement it to record what happened.
 */
public interface LoginOutputBoundary {

  /**
   * Called when the user was logged in.
   *
   * @param outputData the logged-in user's details
   */
  void prepareSuccessView(LoginOutputData outputData);

  /**
   * Called when the user could not be logged in. Nobody's session was started.
   *
   * @param error a message explaining what went wrong, ready to show to the user
   */
  void prepareFailView(String error);
}
