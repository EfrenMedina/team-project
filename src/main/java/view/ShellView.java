package view;

import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Frame for every screen after login: the {@link Sidebar} on the left and the screen's own
 * content on the right. Feature views only build the content; {@code AppBuilder} wraps it.
 */
public class ShellView extends JPanel {

  /**
   * Wraps a screen's content.
   *
   * @param viewName the card name of this screen
   * @param selected the sidebar item to highlight
   * @param navigation used by the sidebar to switch screens
   * @param content the screen's own view
   */
  public ShellView(
      String viewName,
      SidebarItem selected,
      NavigationController navigation,
      JComponent content) {
    super(new BorderLayout());
    setName(viewName);
    setBackground(Theme.BACKGROUND);
    add(new Sidebar(selected, navigation), BorderLayout.WEST);
    add(content, BorderLayout.CENTER);
  }
}
