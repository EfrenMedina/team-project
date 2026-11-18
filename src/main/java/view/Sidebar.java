package view;

import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The left-hand navigation shown on every screen after login (216px wide): logo, menu with the
 * current screen highlighted, and the signed-in user with "Log out" at the bottom.
 */
public class Sidebar extends JPanel {

  /** Shown until US-02 passes in the signed-in user. */
  static final String PLACEHOLDER_USER = "guest";

  /**
   * Builds the sidebar.
   *
   * @param selected the item to highlight
   * @param navigation used to switch screens when an item is clicked
   */
  public Sidebar(SidebarItem selected, NavigationController navigation) {
    super(new BorderLayout());
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
    add(userFooter(navigation), BorderLayout.SOUTH);
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

  private static JPanel userFooter(NavigationController navigation) {
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    footer.add(new Avatar(PLACEHOLDER_USER));
    footer.add(Box.createHorizontalStrut(10));

    JPanel nameAndLogout = column();
    nameAndLogout.add(Theme.bodyBold(PLACEHOLDER_USER));
    // US-02 replaces this with the logout use case.
    JButton logout = Theme.linkButton("Log out", 12, false, Theme.MUTED);
    logout.addActionListener(e -> navigation.navigateTo(ViewNames.LOGIN));
    nameAndLogout.add(logout);
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

    private final JLabel initials;

    Avatar(String username) {
      setPreferredSize(new Dimension(32, 32));
      setLayout(new BorderLayout());
      String letters = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
      initials = Theme.label(letters, 12, true, Theme.MUTED);
      initials.setHorizontalAlignment(SwingConstants.CENTER);
      add(initials, BorderLayout.CENTER);
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
