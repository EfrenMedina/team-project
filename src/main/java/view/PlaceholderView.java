package view;

import interface_adapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Stand-in for a screen that has not been built yet. Shows the screen's title, which user stories
 * it belongs to, and buttons for the moves the navigation map allows from it.
 *
 * <p>The feature owner replaces this with their real view by changing only their own {@code
 * addXView()} method in {@code AppBuilder}.
 */
public class PlaceholderView extends JPanel {

  /**
   * Builds the placeholder.
   *
   * @param viewName the card name of this screen
   * @param title the heading shown at the top
   * @param userStories which user stories the real screen covers, e.g. {@code "US-03"}
   * @param navigation used to switch screens when a link is clicked
   * @param links the screens reachable from this one
   */
  public PlaceholderView(
      String viewName,
      String title,
      String userStories,
      NavigationController navigation,
      List<NavLink> links) {
    setName(viewName);
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    JLabel heading = new JLabel(title);
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 28f));
    header.add(heading);
    header.add(new JLabel(userStories + " · placeholder, not built yet"));
    add(header, BorderLayout.NORTH);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    for (NavLink link : links) {
      JButton button = new JButton(link.label());
      button.addActionListener(e -> navigation.navigateTo(link.target()));
      buttons.add(button);
    }
    add(buttons, BorderLayout.CENTER);
  }
}
