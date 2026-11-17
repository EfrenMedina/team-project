package usecase.signup;

/**
 * Receives the outcome of the sign-up use case. The presenter implements it to update the
 * screen; tests implement it to record what happened.
 */
public interface SignupOutputBoundary {

  /**
   * Called when the user was created.
   *
   * @param outputData the new user's details
   */
  void prepareSuccessView(SignupOutputData outputData);

  /**
   * Called when the user could not be created. Nothing was saved.
   *
   * @param error a message explaining what to fix, ready to show to the user
   */
  void prepareFailView(String error);

}