package view;

import interfaceadapter.LoggedInViewModel;
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
   * @param loggedInViewModel holds the logged-in username, shown in the sidebar
   * @param logOut run when the sidebar's "Log out" is clicked
   * @param content the screen's own view
   */
  public ShellView(
      String viewName,
      SidebarItem selected,
      NavigationController navigation,
      LoggedInViewModel loggedInViewModel,
      Runnable logOut,
      JComponent content) {
    super(new BorderLayout());
    setName(viewName);
    setBackground(Theme.BACKGROUND);
    add(new Sidebar(selected, navigation, loggedInViewModel, logOut), BorderLayout.WEST);
    add(content, BorderLayout.CENTER);
  }
}
