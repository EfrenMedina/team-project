package usecase.logout;

/**
 * Receives the outcome of the logout use case. The presenter implements it to clear the
 * screens and go back to Login; tests implement it to record what happened.
 */
public interface LogoutOutputBoundary {

  /** Called once the session has ended, so nobody is logged in. Logout can't fail. */
  void prepareSuccessView();
}
