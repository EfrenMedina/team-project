package view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Colours, fonts and ready-styled components taken from {@code docs/wireframes/}. Use these instead
 * of picking colours by hand, so every screen looks the same.
 */
public final class Theme {

  /** Window background. */
  public static final Color BACKGROUND = Color.decode("#0E1013");
  /** Sidebar and card background. */
  public static final Color SURFACE = Color.decode("#16191E");
  /** Text fields and segmented-control track. */
  public static final Color INPUT = Color.decode("#1A1D22");
  /** Selected sidebar item, selected segment, divider lines. */
  public static final Color RAISED = Color.decode("#1F2329");
  /** Text field border. */
  public static final Color BORDER = Color.decode("#3A4049");
  /** Main text. */
  public static final Color TEXT = Color.decode("#ECEEF1");
  /** Secondary text. */
  public static final Color MUTED = Color.decode("#9AA1AB");
  /** Footnotes and section captions. */
  public static final Color FAINT = Color.decode("#646B75");
  /** Gains, the accent colour and primary buttons. */
  public static final Color UP = Color.decode("#3DD68C");
  /** Losses and errors. */
  public static final Color DOWN = Color.decode("#FF6A4D");

  private static final String STYLE = "FlatLaf.style";
  private static final String NO_FOCUS_RING = "focusWidth: 0; innerFocusWidth: 0; borderWidth: 0;";

  private Theme() {}

  /** Installs the Inter font and the dark FlatLaf look. Call once, before building any view. */
  public static void install() {
    FlatInterFont.install();
    FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
    FlatLaf.setGlobalExtraDefaults(
        Map.of(
            "@background", "#0E1013",
            "@foreground", "#ECEEF1",
            "@accentColor", "#3DD68C",
            "Component.arc", "16",
            "Button.arc", "16",
            "TextComponent.arc", "16"));
    FlatDarkLaf.setup();
  }

  // ---- Text ----

  /** Font at the given pixel size from the wireframes. */
  public static Font font(float size, boolean bold) {
    Font base = UIManager.getFont("Label.font");
    if (base == null) {
      base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }
    return base.deriveFont(bold ? Font.BOLD : Font.PLAIN, size);
  }

  /** Big number at the top of a screen, e.g. total value (44px bold). */
  public static JLabel hero(String text) {
    return label(text, 44, true, TEXT);
  }

  /** Page title, e.g. "Statements" (28px bold). */
  public static JLabel h1(String text) {
    return label(text, 28, true, TEXT);
  }

  /** Section title, e.g. "Watchlist" (18px semibold). */
  public static JLabel h2(String text) {
    return label(text, 18, true, TEXT);
  }

  /** Body text (14px). */
  public static JLabel body(String text) {
    return label(text, 14, false, TEXT);
  }

  /** Bold body text (14px). */
  public static JLabel bodyBold(String text) {
    return label(text, 14, true, TEXT);
  }

  /** Secondary text (12px grey). */
  public static JLabel small(String text) {
    return label(text, 12, false, MUTED);
  }

  /** Footnote (11px dim grey). */
  public static JLabel footnote(String text) {
    return label(text, 11, false, FAINT);
  }

  /** A label with an exact size, weight and colour. */
  public static JLabel label(String text, float size, boolean bold, Color color) {
    JLabel label = new JLabel(text);
    label.setFont(font(size, bold));
    label.setForeground(color);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  // ---- Buttons ----

  /** Green pill button for the main action, e.g. "Log in", "Buy". */
  public static JButton primaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(font(14, true));
    button.setBackground(UP);
    button.setForeground(BACKGROUND);
    button.putClientProperty(
        STYLE,
        NO_FOCUS_RING
            + "arc: 999; margin: 11,24,11,24;"
            + "hoverBackground: #52DE9C; pressedBackground: #2FBF78;");
    return button;
  }

  /** Dark pill button for other actions. */
  public static JButton secondaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(font(14, true));
    button.setBackground(RAISED);
    button.setForeground(TEXT);
    button.putClientProperty(
        STYLE,
        NO_FOCUS_RING
            + "arc: 999; margin: 11,24,11,24;"
            + "hoverBackground: #2A2F36; pressedBackground: #30363E;");
    return button;
  }

  /** Text-only button, e.g. "Log out", "+ Add". */
  public static JButton linkButton(String text, float size, boolean bold, Color color) {
    JButton button = new JButton(text);
    button.setFont(font(size, bold));
    button.setForeground(color);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.putClientProperty("JButton.buttonType", "borderless");
    button.putClientProperty(STYLE, NO_FOCUS_RING + "margin: 0,0,0,0;");
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    return button;
  }

  /** Flat full-width row or menu item, highlighted when {@code selected}. */
  public static JButton flatButton(String text, boolean selected) {
    JButton button = new JButton(text);
    button.setFont(font(14, selected));
    button.setForeground(TEXT);
    button.setBackground(selected ? RAISED : SURFACE);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.putClientProperty(
        STYLE,
        NO_FOCUS_RING
            + "arc: 16; margin: 9,16,9,16;"
            + "hoverBackground: #1F2329; pressedBackground: #262B32;"
            + "disabledBackground: #16191E; disabledText: #646B75;");
    return button;
  }

  // ---- Inputs ----

  /** Dark rounded text field. */
  public static JTextField textField() {
    JTextField field = new JTextField();
    styleInput(field);
    return field;
  }

  /** Dark rounded password field. */
  public static JPasswordField passwordField() {
    JPasswordField field = new JPasswordField();
    styleInput(field);
    return field;
  }

  private static void styleInput(JTextField field) {
    field.setFont(font(14, false));
    field.setBackground(INPUT);
    field.setForeground(TEXT);
    field.setCaretColor(TEXT);
    field.putClientProperty(
        STYLE,
        "arc: 16; margin: 9,12,9,12; focusWidth: 0; innerFocusWidth: 0;"
            + "borderColor: #3A4049; focusedBorderColor: #3DD68C;");
  }
}
