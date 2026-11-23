package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** A panel with a filled, rounded background, like the cards in the wireframes. */
public class RoundedPanel extends JPanel {

  private final Color fill;
  private final int arc;

  /**
   * Creates the panel.
   *
   * @param layout how children are laid out
   * @param fill the background colour, usually {@link Theme#SURFACE}
   * @param radius the corner radius in pixels (cards use 16, buttons 8)
   */
  public RoundedPanel(LayoutManager layout, Color fill, int radius) {
    super(layout);
    this.fill = fill;
    this.arc = radius * 2;
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(fill);
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
    g2.dispose();
    super.paintComponent(g);
  }
}
