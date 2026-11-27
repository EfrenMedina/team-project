package interfaceadapter.viewstock;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * Holds the Stock detail screen's {@link ViewStockState} and tells the screen when it changes.
 * The presenter sets the state; the screen listens and redraws.
 *
 * <p>The presenter runs on a background thread, so listeners may be called off the Swing event
 * thread and must move their work onto it.
 *
 * <p>When the app shell from SCRUM-23 adds a shared {@code ViewModel} base class, this class
 * should extend it instead of managing its own listeners.
 */
public class ViewStockViewModel {

  /** The name of the Stock detail screen, used to switch to it. Matches the app shell's. */
  public static final String VIEW_NAME = "stock detail";

  /** The name of the property change event fired whenever the state changes. */
  public static final String STATE_PROPERTY = "state";

  /** Notifies the listeners. */
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /** What the screen currently shows. Volatile because the presenter sets it off the UI thread. */
  private volatile ViewStockState state = ViewStockState.EMPTY;

  /**
   * Returns what the screen currently shows.
   *
   * @return the current state; never null
   */
  public ViewStockState getState() {
    return state;
  }

  /**
   * Replaces the state and notifies every listener with a {@value #STATE_PROPERTY} event. The
   * event fires even when the new state equals the old one, for example when the same error
   * comes back after a retry, so the screen always hears that a request finished.
   *
   * @param newState what the screen should show
   * @throws NullPointerException if {@code newState} is null
   */
  public void setState(ViewStockState newState) {
    state = Objects.requireNonNull(newState, "newState");
    support.firePropertyChange(STATE_PROPERTY, null, newState);
  }

  /**
   * Registers a listener, normally the Stock detail screen, to hear about state changes.
   *
   * @param listener called with a {@value #STATE_PROPERTY} event after each change
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }
}
