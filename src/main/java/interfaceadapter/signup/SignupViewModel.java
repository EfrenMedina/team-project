package interfaceadapter.signup;

import interfaceadapter.ClearedOnLogout;
import interfaceadapter.ViewModel;
import interfaceadapter.ViewNames;
import java.util.Objects;

/**
 * Holds the Sign-up form's {@link SignupState} and tells the screen when it changes. The
 * presenter sets the state; the screen listens and redraws.
 *
 * <p>Unlike the base {@link ViewModel}, every {@link #setState} notifies the listeners, so a
 * presenter can't forget to.
 */
public class SignupViewModel extends ViewModel<SignupState> implements ClearedOnLogout {

  /**
   * The name of the screen the Sign-up form is on, used to switch to it. Sign-up is a tab of
   * the Login screen, so it's the same screen.
   */
  public static final String VIEW_NAME = ViewNames.LOGIN;

  /** The name of the property change event fired whenever the state changes. */
  public static final String STATE_PROPERTY = "state";

  /** Creates the view model with an empty form. */
  public SignupViewModel() {
    super(VIEW_NAME);
    super.setState(SignupState.EMPTY);
  }

  /**
   * Replaces the state and notifies every listener with a {@value #STATE_PROPERTY} event.
   *
   * @param newState what the form should show
   * @throws NullPointerException if {@code newState} is null
   */
  @Override
  public void setState(SignupState newState) {
    super.setState(Objects.requireNonNull(newState, "newState"));
    firePropertyChanged(STATE_PROPERTY);
  }

  /** Empties the form and removes the welcome line of the user who signed up. */
  @Override
  public void clearUserData() {
    setState(SignupState.EMPTY);
  }
}
