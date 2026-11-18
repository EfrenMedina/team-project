package view.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Demo screen showing every UI kit component.
 */
public class DemoScreen {
  /**
   * Starts the demo.
   *
   * @param args unused
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      Theme.install();
      JFrame f = new JFrame("PaperTrade UI kit demo");
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      JPanel root = new JPanel(new BorderLayout(Theme.SPACE_L, Theme.SPACE_L));
      root.setBorder(BorderFactory.createEmptyBorder(
          Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));

      JLabel title = new JLabel("Portfolio");
      title.setFont(Theme.TITLE);

      JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_M, 0));
      form.add(new LabeledField("Ticker", 8));
      form.add(new LabeledField("Quantity", 6));
      form.add(new PrimaryButton("Buy"));

      AppTable table = new AppTable(
          new String[]{"Ticker", "Qty", "Value", "P&L", "P&L %"}, 1, 2, 3, 4);
      table.useSignedColumns(3, 4);
      table.addRow("AAPL", 10, Formatters.money(231_400),
          Formatters.signedMoney(20_000), Formatters.percent(0.0125));
      table.addRow("MSFT", 5, Formatters.money(210_050),
          Formatters.signedMoney(-5_000), Formatters.percent(-0.005));

      JPanel top = new JPanel(new BorderLayout(0, Theme.SPACE_M));
      top.add(title, BorderLayout.NORTH);
      top.add(form, BorderLayout.CENTER);

      root.add(top, BorderLayout.NORTH);
      root.add(new JScrollPane(table), BorderLayout.CENTER);
      f.setContentPane(root);
      f.setSize(700, 400);
      f.setLocationRelativeTo(null);
      f.setVisible(true);
    });
  }
}