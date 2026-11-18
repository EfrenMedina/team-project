package view.ui;

import javax.swing.JButton;

/**
 * The app's main call-to-action button.
 */
public class PrimaryButton extends JButton {

  /**
   * Creates a primary button.
   *
   * @param text the button label
   */
  public PrimaryButton(String text) {
    super(text);
    setBackground(Theme.PRIMARY);
    setForeground(Theme.PRIMARY_TEXT);
    setFont(Theme.BODY.deriveFont(java.awt.Font.BOLD));
    setFocusPainted(false);
  }
}