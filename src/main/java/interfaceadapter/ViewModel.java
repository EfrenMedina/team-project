package interfaceadapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Holds the state shown by one view and tells listeners when it changes.
 *
 * <p>Presenters update the state and call {@link #firePropertyChanged()}; the view listens and
 * redraws itself.
 *
 * @param <T> the type of state this view model holds
 */
public class ViewModel<T> {

  private final String viewName;
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);
  private T state;

  /**
   * Creates a view model for the view with the given name.
   *
   * @param viewName the name of the view, used as its card name in the {@code ViewManager}
   */
  public ViewModel(String viewName) {
    this.viewName = viewName;
  }

  public String getViewName() {
    return viewName;
  }

  public T getState() {
    return state;
  }

  public void setState(T state) {
    this.state = state;
  }

  /** Tells listeners that the state changed, under the property name {@code "state"}. */
  public void firePropertyChanged() {
    support.firePropertyChange("state", null, state);
  }

  /**
   * Tells listeners that the state changed, under a custom property name.
   *
   * @param propertyName the name listeners filter on
   */
  public void firePropertyChanged(String propertyName) {
    support.firePropertyChange(propertyName, null, state);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }
}
