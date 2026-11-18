package view;

import interface_adapter.ViewManagerModel;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/** Shows whichever screen the {@link ViewManagerModel} names, using a {@link CardLayout}. */
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
    if ("state".equals(evt.getPropertyName())) {
      cardLayout.show(views, (String) evt.getNewValue());
    }
  }
}
