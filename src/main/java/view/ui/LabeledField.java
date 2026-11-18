package view.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A text field with a label above it.
 */
public class LabeledField extends JPanel {
  private final JTextField field;

  /**
   * Creates a labeled field.
   *
   * @param label   text shown above the field
   * @param columns width of the field in characters
   */
  public LabeledField(String label, int columns) {
    super(new BorderLayout(0, Theme.SPACE_S));
    JLabel l = new JLabel(label);
    l.setForeground(Theme.MUTED);
    field = new JTextField(columns);
    add(l, BorderLayout.NORTH);
    add(field, BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(
        Theme.SPACE_S, 0, Theme.SPACE_S, 0));
  }

  public String getText() {
    return field.getText().trim();
  }

  public void setText(String t) {
    field.setText(t);
  }

  public JTextField getField() {
    return field;
  }
}