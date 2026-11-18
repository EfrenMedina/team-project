package view;

import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Underlined tabs that switch between sibling screens, e.g. "Account statement" and "Realized
 * gains" under Statements.
 */
public class TabBar extends JPanel {

  /**
   * Builds the tabs.
   *
   * @param tabs one link per tab, in order
   * @param selectedTarget the card name of the tab to underline
   * @param navigation used to switch screens when a tab is clicked
   */
  public TabBar(List<NavLink> tabs, String selectedTarget, NavigationController navigation) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(false);
    setAlignmentX(LEFT_ALIGNMENT);
    for (NavLink tab : tabs) {
      boolean selected = tab.target().equals(selectedTarget);
      JButton button = Theme.linkButton(tab.label(), 14, selected, Theme.TEXT);
      button.addActionListener(e -> navigation.navigateTo(tab.target()));

      JPanel underline = new JPanel();
      underline.setBackground(selected ? Theme.TEXT : Theme.BACKGROUND);
      underline.setPreferredSize(new Dimension(0, 2));

      JPanel cell = new JPanel(new BorderLayout(0, 8));
      cell.setOpaque(false);
      cell.add(button, BorderLayout.CENTER);
      cell.add(underline, BorderLayout.SOUTH);
      add(cell);
      add(Box.createHorizontalStrut(48));
    }
    setMaximumSize(getPreferredSize());
  }
}
