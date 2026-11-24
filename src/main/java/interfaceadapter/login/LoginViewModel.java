package interfaceadapter.login;

import interfaceadapter.ClearedOnLogout;
import interfaceadapter.ViewModel;
import interfaceadapter.ViewNames;
import java.util.Objects;

/**
 * Holds the Log-in form's {@link LoginState} and tells the screen when it changes. The
 * presenters set the state; the screen listens and redraws.
 *
 * <p>Unlike the base {@link ViewModel}, every {@link #setState} notifies the listeners, so a
 * presenter can't forget to.
 */
public class LoginViewModel extends ViewModel<LoginState> implements ClearedOnLogout {

  /** The name of the screen the Log-in form is on, used to switch to it. */
  public static final String VIEW_NAME = ViewNames.LOGIN;

  /** The name of the property change event fired whenever the state changes. */
  public static final String STATE_PROPERTY = "state";

  /** Creates the view model with an empty form. */
  public LoginViewModel() {
    super(VIEW_NAME);
    super.setState(LoginState.EMPTY);
  }

  /**
   * Replaces the state and notifies every listener with a {@value #STATE_PROPERTY} event.
   *
   * @param newState what the form should show
   * @throws NullPointerException if {@code newState} is null
   */
  @Override
  public void setState(LoginState newState) {
    super.setState(Objects.requireNonNull(newState, "newState"));
    firePropertyChanged(STATE_PROPERTY);
  }

  /** Empties the form and forgets who was logged in. */
  @Override
  public void clearUserData() {
    setState(LoginState.EMPTY);
  }
}
