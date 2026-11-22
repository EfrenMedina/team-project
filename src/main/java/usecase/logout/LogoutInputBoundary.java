package usecase.logout;

/**
 * The logout use case, as seen by the controller. The controller calls {@link #execute}
 * without knowing which class does the work.
 */
public interface LogoutInputBoundary {

  /** Logs out whoever is logged in. The outcome is reported to the {@link LogoutOutputBoundary}. */
  void execute();
}
