package view.ui;

import java.awt.Color;

/**
 * The app's shared colours, so every screen looks like the same app.
 *
 * <p>Placeholder written for SCRUM-26 with only the colours the Sign-up screen uses, taken
 * from the wireframes. SCRUM-22 (PR #17) owns this class: its version has the same constant
 * names plus fonts, spacing and the look-and-feel setup, and replaces this one when merged.
 */
public final class Theme {

  /** The main action colour, for buttons such as "Create account". */
  public static final Color PRIMARY = new Color(0x3DD68C);

  /** Text on a {@link #PRIMARY} background. */
  public static final Color PRIMARY_TEXT = new Color(0x0E1013);

  /** Gains and success messages. */
  public static final Color GAIN = new Color(0x3DD68C);

  /** Losses and error messages. */
  public static final Color LOSS = new Color(0xFF6A4D);

  /** Secondary text, such as hints. */
  public static final Color MUTED = new Color(0x9AA1AB);

  private Theme() {
  }
}
