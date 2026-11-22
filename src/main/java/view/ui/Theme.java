package view.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import javax.swing.UIManager;

/** Colours, fonts and spacing shared by every screen. */
public final class Theme {
  public static final Color BACKGROUND = new Color(0x0E1013);
  public static final Color SURFACE = new Color(0x16191E);
  public static final Color TEXT = new Color(0xECEEF1);
  public static final Color MUTED = new Color(0x9AA1AB);
  public static final Color GAIN = new Color(0x3DD68C);
  public static final Color LOSS = new Color(0xFF6A4D);
  public static final Color PRIMARY = GAIN;
  public static final Color PRIMARY_TEXT = BACKGROUND;

  public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
  public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
  public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 14);

  public static final int SPACE_S = 4;
  public static final int SPACE_M = 8;
  public static final int SPACE_L = 16;
  public static final int SPACE_XL = 24;

  private Theme() {}

  /** Call once at startup, before creating any Swing component. */
  public static void install() {
    FlatLaf.setGlobalExtraDefaults(Map.of(
      "@background", "#0E1013",
      "@foreground", "#ECEEF1",
      "@accentColor", "#3DD68C"));
    FlatDarkLaf.setup();
    UIManager.put("Table.background", SURFACE);
    UIManager.put("TextField.background", SURFACE);
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("defaultFont", BODY);
  }
}
