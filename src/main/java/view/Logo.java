package view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** The app's mark (a green rising line in a dark tile) followed by "Paper Trading App". */
public class Logo extends JPanel {

  /** Creates the logo. */
  public Logo() {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(false);
    setAlignmentX(LEFT_ALIGNMENT);
    add(new Mark());
    add(Box.createHorizontalStrut(10));
    add(Theme.bodyBold("Paper Trading App"));
    setMaximumSize(getPreferredSize());
  }

  /** The 28×28 tile with the line, drawn as in the wireframe SVGs. */
  private static final class Mark extends JComponent {

    Mark() {
      setPreferredSize(new Dimension(28, 28));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Theme.RAISED);
      g2.fillRoundRect(0, 0, 28, 28, 14, 14);
      Path2D line = new Path2D.Float();
      line.moveTo(6, 20);
      line.lineTo(12, 13);
      line.lineTo(17, 17);
      line.lineTo(23, 8);
      g2.setColor(Theme.UP);
      g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.draw(line);
      g2.dispose();
    }
  }
}
