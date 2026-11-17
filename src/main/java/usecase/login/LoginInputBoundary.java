package usecase.login;

/**
 * The login use case, as seen by the controller. The controller calls {@link #execute}
 * without knowing which class does the work.
 */
public interface LoginInputBoundary {

  /**
   * Tries to log a user in with what they typed. The outcome is reported to the {@link
   * LoginOutputBoundary}, not returned.
   *
   * @param inputData what the user typed
   */
  void execute(LoginInputData inputData);
}
