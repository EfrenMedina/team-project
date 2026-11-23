package view;

import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Content for a screen that has not been built yet: its title (and tabs, if it has any), then an
 * empty card saying which user story builds it, which wireframe to follow, and buttons for the
 * moves the navigation map allows from it.
 *
 * <p>The feature owner replaces this with their real view by changing only their own {@code
 * addXView()} method in {@code AppBuilder}.
 */
public class PlaceholderView extends JPanel {

  /**
   * Builds the placeholder.
   *
   * @param title the page title
   * @param tabs tabs under the title, or null if the screen has none
   * @param userStories which user stories the real screen covers, e.g. {@code "US-03"}
   * @param wireframe the wireframe file to follow, e.g. {@code "03-search.svg"}
   * @param navigation used to switch screens when a link is clicked
   * @param links the screens reachable from this one
   */
  public PlaceholderView(
      String title,
      TabBar tabs,
      String userStories,
      String wireframe,
      NavigationController navigation,
      List<NavLink> links) {
    super(new BorderLayout(0, 28));
    setBackground(Theme.BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder(48, 40, 40, 40));

    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.setOpaque(false);
    header.add(Theme.h1(title));
    if (tabs != null) {
      header.add(Box.createVerticalStrut(24));
      header.add(tabs);
    }
    add(header, BorderLayout.NORTH);
    add(emptyCard(userStories, wireframe, navigation, links), BorderLayout.CENTER);
  }

  private static JComponent emptyCard(
      String userStories, String wireframe, NavigationController navigation, List<NavLink> links) {
    JPanel message = new JPanel();
    message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
    message.setOpaque(false);
    message.add(centered(Theme.h2("Coming in " + userStories)));
    message.add(Box.createVerticalStrut(8));
    message.add(centered(Theme.small("Design: docs/wireframes/" + wireframe)));

    if (!links.isEmpty()) {
      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
      buttons.setOpaque(false);
      for (NavLink link : links) {
        JButton button =
            link.primary()
                ? Theme.primaryButton(link.label())
                : Theme.secondaryButton(link.label());
        button.addActionListener(e -> navigation.navigateTo(link.target()));
        buttons.add(button);
      }
      message.add(Box.createVerticalStrut(28));
      message.add(centered(buttons));
    }

    RoundedPanel card = new RoundedPanel(new GridBagLayout(), Theme.SURFACE, 16);
    card.add(message);
    return card;
  }

  private static JComponent centered(JComponent component) {
    component.setAlignmentX(Component.CENTER_ALIGNMENT);
    component.setMaximumSize(component.getPreferredSize());
    return component;
  }
}
