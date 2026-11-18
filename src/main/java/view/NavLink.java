package view;

/**
 * A button that moves to another screen.
 *
 * @param label the button text
 * @param target the card name of the screen to show, from {@link interfaceadapter.ViewNames}
 * @param primary true for the screen's main action (green pill), false for others
 */
public record NavLink(String label, String target, boolean primary) {

  /** A secondary link. */
  public NavLink(String label, String target) {
    this(label, target, false);
  }
}
