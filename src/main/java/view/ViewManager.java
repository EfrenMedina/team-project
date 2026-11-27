package view;

import interfaceadapter.ViewManagerModel;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Shows whichever screen the {@link ViewManagerModel} names, using a {@link CardLayout}.
 *
 * <p>Presenters may switch screens from a background thread (login and sign-up run there), so
 * the switch itself is always done on the Swing thread. Callers on another thread wait for it,
 * so the new screen is showing as soon as the call returns.
 */
public class ViewManager implements PropertyChangeListener {

  private final JPanel views;
  private final CardLayout cardLayout;

  /**
   * Starts listening to the model.
   *
   * @param views the panel holding one card per screen
   * @param cardLayout the layout of {@code views}
   * @param viewManagerModel the model naming the screen to show
   */
  public ViewManager(JPanel views, CardLayout cardLayout, ViewManagerModel viewManagerModel) {
    this.views = views;
    this.cardLayout = cardLayout;
    viewManagerModel.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!"state".equals(evt.getPropertyName())) {
      return;
    }
    String viewName = (String) evt.getNewValue();
    if (SwingUtilities.isEventDispatchThread()) {
      cardLayout.show(views, viewName);
      return;
    }
    try {
      SwingUtilities.invokeAndWait(() -> cardLayout.show(views, viewName));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Could not show screen " + viewName, e.getCause());
    }
  }
}
