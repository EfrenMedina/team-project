package view.ui;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Colours, fonts and spacing shared by every screen.
 */
public final class Theme {
  public static final Color PRIMARY = new Color(0x2563EB);
  public static final Color PRIMARY_TEXT = Color.WHITE;
  public static final Color GAIN = new Color(0x1B8A3E);
  public static final Color LOSS = new Color(0xC62828);
  public static final Color MUTED = new Color(0x6B7280);

  public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
  public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
  public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 14);

  public static final int SPACE_S = 4;
  public static final int SPACE_M = 8;
  public static final int SPACE_L = 16;
  public static final int SPACE_XL = 24;

  private Theme() {
  }

  /**
   * Call once at startup, before creating any Swing component.
   */
  public static void install() {
    FlatLightLaf.setup();
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("defaultFont", BODY);
  }

  public static Color colorFor(long signedValue) {
    return signedValue > 0 ? GAIN : signedValue < 0 ? LOSS : MUTED;
  }
}
