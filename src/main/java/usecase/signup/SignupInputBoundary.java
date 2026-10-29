package usecase.signup;

/**
 * The sign-up use case, as seen by the controller. The controller calls
 * {@link #execute} without knowing which class does the work.
 */
public interface SignupInputBoundary {

  /**
   * Tries to create a new user from the form input. The outcome is reported to the
   * {@link SignupOutputBoundary}, not returned.
   *
   * @param inputData what the user typed
   */
  void execute(SignupInputData inputData);
}
