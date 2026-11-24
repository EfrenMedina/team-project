package view;

import interfaceadapter.LoggedInViewModel;
import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * The left-hand navigation shown on every screen after login (216px wide): logo, menu with the
 * current screen highlighted, and the signed-in user with "Log out" at the bottom.
 */
public class Sidebar extends JPanel {

  /** The logged-in user's name. */
  final JLabel usernameLabel = Theme.bodyBold(" ");

  /** The "Log out" link. */
  final JButton logoutButton = Theme.linkButton("Log out", 12, false, Theme.MUTED);

  /** The round badge with the user's initial. */
  private final Avatar avatar = new Avatar();

  /**
   * Builds the sidebar and starts showing whoever is logged in.
   *
   * @param selected the item to highlight
   * @param navigation used to switch screens when an item is clicked
   * @param loggedInViewModel holds the logged-in username, shown at the bottom
   * @param logOut run when "Log out" is clicked, normally the logout controller
   * @throws NullPointerException if {@code loggedInViewModel} or {@code logOut} is null
   */
  public Sidebar(
      SidebarItem selected,
      NavigationController navigation,
      LoggedInViewModel loggedInViewModel,
      Runnable logOut) {
    super(new BorderLayout());
    Objects.requireNonNull(loggedInViewModel, "loggedInViewModel");
    Objects.requireNonNull(logOut, "logOut");
    setBackground(Theme.SURFACE);
    setPreferredSize(new Dimension(216, 800));
    setBorder(BorderFactory.createEmptyBorder(28, 16, 24, 16));

    JPanel top = column();
    JPanel logoRow = column();
    logoRow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    logoRow.add(new Logo());
    top.add(logoRow);
    top.add(Box.createVerticalStrut(35));
    for (SidebarItem item : SidebarItem.values()) {
      top.add(menuButton(item, item == selected, navigation));
      top.add(Box.createVerticalStrut(6));
    }
    add(top, BorderLayout.NORTH);
    add(userFooter(), BorderLayout.SOUTH);

    logoutButton.addActionListener(e -> logOut.run());
    showUser(loggedInViewModel.getState());
    loggedInViewModel.addPropertyChangeListener(event -> {
      String username = (String) event.getNewValue();
      if (SwingUtilities.isEventDispatchThread()) {
        showUser(username);
      } else {
        SwingUtilities.invokeLater(() -> showUser(username));
      }
    });
  }

  /**
   * Shows a user's name and initial at the bottom of the sidebar.
   *
   * @param username the logged-in username, or {@code ""} for nobody
   */
  private void showUser(String username) {
    usernameLabel.setText(username.isEmpty() ? " " : username);
    avatar.setUsername(username);
  }

  private static JButton menuButton(
      SidebarItem item, boolean selected, NavigationController navigation) {
    JButton button = Theme.flatButton(item.label(), selected);
    button.setAlignmentX(LEFT_ALIGNMENT);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    if (item.target() == null) {
      button.setEnabled(false);
      button.setToolTipText("Not designed yet");
    } else {
      button.addActionListener(e -> navigation.navigateTo(item.target()));
    }
    return button;
  }

  private JPanel userFooter() {
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    footer.add(avatar);
    footer.add(Box.createHorizontalStrut(10));

    JPanel nameAndLogout = column();
    nameAndLogout.add(usernameLabel);
    nameAndLogout.add(logoutButton);
    footer.add(nameAndLogout);
    return footer;
  }

  private static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setAlignmentX(LEFT_ALIGNMENT);
    return panel;
  }

  /** Round badge with the user's initials. */
  private static final class Avatar extends JComponent {

    private final JLabel initials = Theme.label("?", 12, true, Theme.MUTED);

    Avatar() {
      setPreferredSize(new Dimension(32, 32));
      setLayout(new BorderLayout());
      initials.setHorizontalAlignment(SwingConstants.CENTER);
      add(initials, BorderLayout.CENTER);
    }

    /** Shows the first letter of a username, or "?" for nobody. */
    void setUsername(String username) {
      initials.setText(
          username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase(Locale.ROOT));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Theme.RAISED);
      g2.fillOval(0, 0, getWidth(), getHeight());
      g2.dispose();
    }
  }
}
