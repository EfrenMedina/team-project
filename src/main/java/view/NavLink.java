package view;

/**
 * A button that moves to another screen.
 *
 * @param label the button text
 * @param target the card name of the screen to show, from {@link interfaceadapter.ViewNames}
 */
public record NavLink(String label, String target) {}
