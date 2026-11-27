package interfaceadapter;

import java.util.Objects;

/**
 * Holds the username of whoever is logged in, for every screen that shows it, such as the
 * sidebar. Its state is the username, or {@code ""} when nobody is logged in.
 *
 * <p>The login and sign-up presenters set it; the logout presenter clears it.
 */
public class LoggedInViewModel extends ViewModel<String> implements ClearedOnLogout {

  /** The name of the property change event fired whenever the username changes. */
  public static final String STATE_PROPERTY = "state";

  /** Creates the view model with nobody logged in. */
  public LoggedInViewModel() {
    super("logged in");
    super.setState("");
  }

  /**
   * Sets the logged-in username and notifies every listener with a {@value #STATE_PROPERTY}
   * event.
   *
   * @param username the logged-in username, or {@code ""} for nobody
   * @throws NullPointerException if {@code username} is null
   */
  @Override
  public void setState(String username) {
    super.setState(Objects.requireNonNull(username, "username"));
    firePropertyChanged(STATE_PROPERTY);
  }

  /** Forgets who was logged in. */
  @Override
  public void clearUserData() {
    setState("");
  }
}
