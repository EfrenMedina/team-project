package view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.ViewManagerModel;
import interface_adapter.navigation.NavigationController;
import java.awt.CardLayout;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ViewManagerTest {

  @Test
  void navigateTo_otherCard_showsOnlyThatCard() {
    CardLayout cardLayout = new CardLayout();
    JPanel views = new JPanel(cardLayout);
    JPanel first = new JPanel();
    JPanel second = new JPanel();
    views.add(first, "first");
    views.add(second, "second");
    ViewManagerModel model = new ViewManagerModel();
    new ViewManager(views, cardLayout, model);

    new NavigationController(model).navigateTo("second");

    assertTrue(second.isVisible());
    assertFalse(first.isVisible());
  }
}
