package view;

import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

/**
 * Content of the home screen after login (wireframe 02), laid out as designed but with no data:
 * total value, chart area, range toggles, buying power and shortcuts on the left, and the Stocks
 * and Watchlist card on the right. US-09 fills it in.
 */
public class DashboardView extends JPanel {

  private static final String[] RANGES = {"1D", "1W", "1M", "3M", "1Y", "ALL"};
  /** The selected range pill: the accent green at 16% over the background. */
  private static final Color SELECTED_RANGE = Color.decode("#163026");
  private static final String NO_DATA = "—";

  /**
   * Builds the dashboard content.
   *
   * @param navigation used to switch screens when a shortcut is clicked
   */
  public DashboardView(NavigationController navigation) {
    super(new BorderLayout());
    setBackground(Theme.BACKGROUND);
    add(mainColumn(navigation), BorderLayout.CENTER);
    add(sideCard(navigation), BorderLayout.EAST);
  }

  private static JComponent mainColumn(NavigationController navigation) {
    JPanel column = column();
    column.setBorder(BorderFactory.createEmptyBorder(48, 40, 24, 40));
    column.add(Theme.small("Investing"));
    column.add(Box.createVerticalStrut(4));
    column.add(Theme.hero("$" + NO_DATA));
    column.add(Box.createVerticalStrut(4));
    column.add(Theme.small("Today"));
    column.add(chartArea());
    column.add(Box.createVerticalStrut(16));
    column.add(rangeToggles());
    column.add(Box.createVerticalStrut(20));
    column.add(divider());
    column.add(row("Buying power", NO_DATA, null, navigation));
    column.add(divider());
    column.add(row("Open orders", "›", ViewNames.OPEN_ORDERS, navigation));
    column.add(divider());
    column.add(row("Last trade", "›", ViewNames.TRADE_HISTORY, navigation));
    return column;
  }

  private static JComponent chartArea() {
    JPanel chart = new JPanel(new GridBagLayout());
    chart.setOpaque(false);
    chart.setAlignmentX(LEFT_ALIGNMENT);
    chart.setPreferredSize(new Dimension(0, 300));
    chart.add(Theme.footnote("Portfolio value chart · US-09"));
    return chart;
  }

  private static JComponent rangeToggles() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    row.setOpaque(false);
    row.setAlignmentX(LEFT_ALIGNMENT);
    for (String range : RANGES) {
      boolean selected = "1D".equals(range);
      JLabel pill = Theme.label(range, 12, true, selected ? Theme.UP : Theme.MUTED);
      pill.setHorizontalAlignment(JLabel.CENTER);
      pill.setPreferredSize(new Dimension(48, 28));
      if (selected) {
        RoundedPanel background = new RoundedPanel(new BorderLayout(), SELECTED_RANGE, 14);
        background.add(pill);
        row.add(background);
      } else {
        row.add(pill);
      }
    }
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
    return row;
  }

  /** A full-width row; clickable when {@code target} is not null. */
  private static JComponent row(
      String label, String value, String target, NavigationController navigation) {
    JPanel content = new JPanel(new BorderLayout());
    content.setOpaque(false);
    content.add(Theme.body(label), BorderLayout.WEST);
    content.add(Theme.bodyBold(value), BorderLayout.EAST);

    JComponent row;
    if (target == null) {
      row = content;
      row.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
    } else {
      JButton button = Theme.flatButton("", false);
      button.setBackground(Theme.BACKGROUND);
      button.setLayout(new BorderLayout());
      button.setMargin(new Insets(18, 0, 18, 0));
      button.add(content);
      button.addActionListener(e -> navigation.navigateTo(target));
      row = button;
    }
    row.setAlignmentX(LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
    row.setPreferredSize(new Dimension(0, 56));
    return row;
  }

  private static JComponent sideCard(NavigationController navigation) {
    JPanel content = column();
    content.add(Theme.h2("Stocks"));
    content.add(Box.createVerticalStrut(16));
    content.add(Theme.small("No holdings yet."));
    content.add(Box.createVerticalStrut(24));
    content.add(divider());
    content.add(Box.createVerticalStrut(20));

    JPanel watchlistHeader = new JPanel(new BorderLayout());
    watchlistHeader.setOpaque(false);
    watchlistHeader.setAlignmentX(LEFT_ALIGNMENT);
    watchlistHeader.add(Theme.h2("Watchlist"), BorderLayout.WEST);
    JButton add = Theme.linkButton("+ Add", 12, true, Theme.MUTED);
    add.addActionListener(e -> navigation.navigateTo(ViewNames.SEARCH));
    watchlistHeader.add(add, BorderLayout.EAST);
    watchlistHeader.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, watchlistHeader.getPreferredSize().height));
    content.add(watchlistHeader);
    content.add(Box.createVerticalStrut(16));
    content.add(Theme.small("Your watchlist is empty."));
    content.add(Box.createVerticalStrut(24));

    JButton addSymbol = Theme.linkButton("+ Add a symbol", 12, false, Theme.MUTED);
    addSymbol.setHorizontalAlignment(JButton.CENTER);
    addSymbol.setBorderPainted(true);
    addSymbol.setBorder(new DashedBorder());
    addSymbol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    addSymbol.setPreferredSize(new Dimension(240, 40));
    addSymbol.addActionListener(e -> navigation.navigateTo(ViewNames.SEARCH));
    content.add(addSymbol);
    content.add(Box.createVerticalStrut(24));
    content.add(Theme.footnote("Click a row to open Stock detail."));
    content.add(Theme.footnote("Right-click a watchlist row to remove it."));

    RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.SURFACE, 16);
    card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    card.setPreferredSize(new Dimension(280, 0));
    card.add(content, BorderLayout.NORTH);

    JPanel margin = new JPanel(new BorderLayout());
    margin.setOpaque(false);
    margin.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 24));
    margin.add(card);
    return margin;
  }

  private static JComponent divider() {
    JPanel line = new JPanel();
    line.setBackground(Theme.RAISED);
    line.setAlignmentX(Component.LEFT_ALIGNMENT);
    line.setPreferredSize(new Dimension(0, 1));
    line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    return line;
  }

  private static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setAlignmentX(LEFT_ALIGNMENT);
    return panel;
  }

  /** Dashed rounded outline used by "+ Add a symbol". */
  private static final class DashedBorder extends AbstractBorder {

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Theme.BORDER);
      float[] dash = {4, 4};
      g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, dash, 0));
      g2.drawRoundRect(x, y, width - 1, height - 1, 16, 16);
      g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(10, 12, 10, 12);
    }
  }
}
