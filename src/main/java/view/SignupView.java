package view;

import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import view.ui.Theme;

/**
 * The Sign-up card from wireframe 01: username, password and confirm-password fields, a
 * "Create account" button and a line for errors. It sends what the user typed to the
 * {@link SignupController} and redraws whenever the {@link SignupViewModel} changes.
 *
 * <p>After a successful sign-up it shows a welcome line with the starting cash. Moving on to
 * the Dashboard is the app shell's job (SCRUM-23); this screen doesn't switch screens itself.
 * Colours come from {@link Theme}; the background and font come from the look and feel the
 * app installs at startup.
 */
public class SignupView extends JPanel implements PropertyChangeListener {

  final JTextField usernameField = new JTextField(20);
  final JPasswordField passwordField = new JPasswordField(20);
  final JPasswordField repeatPasswordField = new JPasswordField(20);
  final JButton createAccountButton = new JButton("Create account");
  final JLabel errorLabel = new JLabel(" ");
  final JLabel successLabel = new JLabel(" ");

  /** The screen's state; the view records the typed username in it before each attempt. */
  private final SignupViewModel viewModel;

  /** Where clicks are sent; set by the app shell after the use case is built. */
  private SignupController signupController;

  /**
   * Creates the screen and starts listening to its view model.
   *
   * @param viewModel the Sign-up screen's view model
   * @throws NullPointerException if {@code viewModel} is null
   */
  public SignupView(SignupViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    viewModel.addPropertyChangeListener(this);

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    JLabel title = new JLabel("Sign up");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    errorLabel.setForeground(Theme.LOSS);
    successLabel.setForeground(Theme.GAIN);
    JLabel hint = new JLabel("At least 8 characters.");
    hint.setFont(hint.getFont().deriveFont(11f));
    hint.setForeground(Theme.MUTED);
    JLabel startingCash = new JLabel("New accounts start with $100,000.00 cash.");
    startingCash.setForeground(Theme.MUTED);
    styleAsPrimary(createAccountButton);

    int row = 0;
    addRow(title, row++, 16);
    addRow(new JLabel("Username"), row++, 4);
    addRow(usernameField, row++, 4);
    addRow(errorLabel, row++, 12);
    addRow(new JLabel("Password"), row++, 4);
    addRow(passwordField, row++, 12);
    addRow(new JLabel("Confirm password"), row++, 4);
    addRow(repeatPasswordField, row++, 4);
    addRow(hint, row++, 16);
    addRow(createAccountButton, row++, 12);
    addRow(startingCash, row++, 4);
    addRow(successLabel, row, 0);

    createAccountButton.addActionListener(event -> submit());
    showState(viewModel.getState());
  }

  /**
   * Sets where "Create account" clicks are sent. The app shell calls this once, after
   * building the sign-up use case.
   *
   * @param signupController the controller to call
   */
  public void setSignupController(SignupController signupController) {
    this.signupController = signupController;
  }

  /**
   * Returns the name the app shell uses to switch to this screen.
   *
   * @return {@value SignupViewModel#VIEW_NAME}
   */
  public String getViewName() {
    return SignupViewModel.VIEW_NAME;
  }

  /**
   * Redraws the screen when the view model's state changes.
   *
   * @param event the change; its new value is the new {@link SignupState}
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (SignupViewModel.STATE_PROPERTY.equals(event.getPropertyName())) {
      showState((SignupState) event.getNewValue());
    }
  }

  /** Sends what the user typed to the controller, then wipes the passwords from the form. */
  private void submit() {
    if (signupController == null) {
      return;
    }
    String username = usernameField.getText();
    // Record what was typed, so a failed attempt redraws with this username, not an old one.
    viewModel.setState(new SignupState(username, null, null, null));
    char[] password = passwordField.getPassword();
    char[] repeatPassword = repeatPasswordField.getPassword();
    try {
      signupController.execute(username, new String(password), new String(repeatPassword));
    } finally {
      // Don't leave the passwords in memory or on screen after the attempt.
      Arrays.fill(password, '\0');
      Arrays.fill(repeatPassword, '\0');
      passwordField.setText("");
      repeatPasswordField.setText("");
    }
  }

  /**
   * Shows a state: the typed username, the error (if any) and the welcome line (if sign-up
   * succeeded).
   *
   * @param state what to show
   */
  private void showState(SignupState state) {
    if (!usernameField.getText().equals(state.username())) {
      usernameField.setText(state.username());
    }
    boolean hasError = state.error() != null;
    errorLabel.setText(hasError ? "! " + state.error() : " ");
    usernameField.putClientProperty("JComponent.outline", hasError ? Theme.LOSS : null);

    boolean signedUp = state.signedUpUsername() != null;
    successLabel.setText(signedUp
        ? "Welcome, " + state.signedUpUsername() + ". Your account starts with "
            + state.startingCash() + "."
        : " ");
  }

  /**
   * Makes a button the wireframe's main action: a filled, fully rounded {@link Theme#PRIMARY}
   * pill with bold dark text.
   *
   * @param button the button to style
   */
  private static void styleAsPrimary(JButton button) {
    button.setBackground(Theme.PRIMARY);
    button.setForeground(Theme.PRIMARY_TEXT);
    button.setFont(button.getFont().deriveFont(Font.BOLD));
    // FlatLaf client properties; other look and feels ignore them.
    button.putClientProperty("JButton.buttonType", "roundRect");
    button.putClientProperty("JButton.focusedBackground", Theme.PRIMARY);
    button.putClientProperty("JButton.hoverBackground", Theme.PRIMARY.brighter());
  }

  /**
   * Adds a component as the next full-width row of the form.
   *
   * @param component what to add
   * @param row the row index
   * @param spaceBelow the gap under the row, in pixels
   */
  private void addRow(JComponent component, int row, int spaceBelow) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;
    constraints.insets = new Insets(0, 0, spaceBelow, 0);
    add(component, constraints);
  }
}
