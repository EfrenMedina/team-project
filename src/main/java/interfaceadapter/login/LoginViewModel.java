package interfaceadapter.login;

import interfaceadapter.ClearedOnLogout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * Holds the Login screen's {@link LoginState} and tells the screen when it changes. The
 * presenters set the state; the screen listens and redraws.
 *
 * <p>When the app shell from SCRUM-23 adds a shared {@code ViewModel} base class, this class
 * should extend it instead of managing its own listeners.
 */
public class LoginViewModel implements ClearedOnLogout {

  /** The name of the Login screen, used to switch to it. */
  public static final String VIEW_NAME = "log in";

  /** The name of the property change event fired whenever the state changes. */
  public static final String STATE_PROPERTY = "state";

  /** Notifies the listeners. */
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /** What the screen currently shows. */
  private LoginState state = LoginState.EMPTY;

  /**
   * Returns what the screen currently shows.
   *
   * @return the current state; never null
   */
  public LoginState getState() {
    return state;
  }

  /**
   * Replaces the state and notifies every listener with a {@value #STATE_PROPERTY} event.
   *
   * @param newState what the screen should show
   * @throws NullPointerException if {@code newState} is null
   */
  public void setState(LoginState newState) {
    LoginState oldState = state;
    state = Objects.requireNonNull(newState, "newState");
    support.firePropertyChange(STATE_PROPERTY, oldState, newState);
  }

  /**
   * Registers a listener, normally the Login screen, to hear about state changes.
   *
   * @param listener called with a {@value #STATE_PROPERTY} event after each change
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /** Empties the form and forgets who was logged in. */
  @Override
  public void clearUserData() {
    setState(LoginState.EMPTY);
  }
}
