package interfaceadapter.signup;

import interfaceadapter.ClearedOnLogout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * Holds the Sign-up screen's {@link SignupState} and tells the screen when it changes. The
 * presenter sets the state; the screen listens and redraws.
 *
 * <p>When the app shell from SCRUM-23 adds a shared {@code ViewModel} base class, this class
 * should extend it instead of managing its own listeners. Switching to the Dashboard after a
 * successful sign-up also belongs to the app shell: it can listen for a state whose
 * {@code signedUpUsername} is set.
 */
public class SignupViewModel implements ClearedOnLogout {

  /** The name of the Sign-up screen, used to switch to it. */
  public static final String VIEW_NAME = "sign up";

  /** The name of the property change event fired whenever the state changes. */
  public static final String STATE_PROPERTY = "state";

  /** Notifies the listeners. */
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /** What the screen currently shows. */
  private SignupState state = SignupState.EMPTY;

  /**
   * Returns what the screen currently shows.
   *
   * @return the current state; never null
   */
  public SignupState getState() {
    return state;
  }

  /**
   * Replaces the state and notifies every listener with a {@value #STATE_PROPERTY} event.
   *
   * @param newState what the screen should show
   * @throws NullPointerException if {@code newState} is null
   */
  public void setState(SignupState newState) {
    SignupState oldState = state;
    state = Objects.requireNonNull(newState, "newState");
    support.firePropertyChange(STATE_PROPERTY, oldState, newState);
  }

  /**
   * Registers a listener, normally the Sign-up screen, to hear about state changes.
   *
   * @param listener called with a {@value #STATE_PROPERTY} event after each change
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /** Empties the form and removes the welcome line of the user who signed up. */
  @Override
  public void clearUserData() {
    setState(SignupState.EMPTY);
  }
}
