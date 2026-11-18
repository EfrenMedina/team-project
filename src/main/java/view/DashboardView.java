package view;

import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The home screen after logging in (wireframe 02). For now it is an empty content area plus the
 * sidebar that reaches every other screen. US-09 fills in the content.
 */
public class DashboardView extends JPanel {

  /** Sidebar entries, in the order shown on the navigation map. */
  private static final List<NavLink> SIDEBAR =
      List.of(
          new NavLink("Dashboard", ViewNames.DASHBOARD),
          new NavLink("Search", ViewNames.SEARCH),
          new NavLink("Portfolio", ViewNames.PORTFOLIO),
          new NavLink("Open orders", ViewNames.OPEN_ORDERS),
          new NavLink("Trade history", ViewNames.TRADE_HISTORY),
          new NavLink("Account statement", ViewNames.ACCOUNT_STATEMENT),
          new NavLink("Realized gains", ViewNames.REALIZED_GAINS));

  /**
   * Builds the dashboard.
   *
   * @param navigation used to switch screens when a sidebar entry is clicked
   */
  public DashboardView(NavigationController navigation) {
    setName(ViewNames.DASHBOARD);
    setLayout(new BorderLayout());

    JPanel sidebar = new JPanel();
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
    for (NavLink link : SIDEBAR) {
      sidebar.add(sidebarButton(link, navigation));
      sidebar.add(Box.createVerticalStrut(8));
    }
    sidebar.add(Box.createVerticalGlue());
    // Log out lives under Account in the sidebar. US-02 replaces this with the logout use case.
    sidebar.add(sidebarButton(new NavLink("Log out", ViewNames.LOGIN), navigation));
    add(sidebar, BorderLayout.WEST);

    JPanel content = new JPanel(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
    JLabel heading = new JLabel("Dashboard");
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 28f));
    content.add(heading, BorderLayout.NORTH);
    JPanel body = new JPanel();
    JButton watchlistRow = new JButton("Watchlist row (placeholder)");
    watchlistRow.addActionListener(e -> navigation.navigateTo(ViewNames.STOCK_DETAIL));
    body.add(watchlistRow);
    content.add(body, BorderLayout.CENTER);
    add(content, BorderLayout.CENTER);
  }

  private static JButton sidebarButton(NavLink link, NavigationController navigation) {
    JButton button = new JButton(link.label());
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
    button.addActionListener(e -> navigation.navigateTo(link.target()));
    return button;
  }
}
