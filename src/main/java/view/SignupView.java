package view;

import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.awt.Color;
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

/**
 * The Sign-up card from wireframe 01: username, password and confirm-password fields, a
 * "Create account" button and a line for errors. It sends what the user typed to the
 * {@link SignupController} and redraws whenever the {@link SignupViewModel} changes.
 *
 * <p>After a successful sign-up it shows a welcome line with the starting cash. Moving on to
 * the Dashboard is the app shell's job (SCRUM-23); this screen doesn't switch screens itself.
 * Colours and fonts come from the look and feel until the shared UI kit (SCRUM-22) is merged.
 */
public class SignupView extends JPanel implements PropertyChangeListener {

  /** Colour of error messages, matching the wireframe's red. */
  private static final Color ERROR_COLOUR = new Color(0xE5, 0x48, 0x4D);

  /** Colour of the success line, matching the wireframe's green. */
  private static final Color SUCCESS_COLOUR = new Color(0x3D, 0xD6, 0x8C);

  final JTextField usernameField = new JTextField(20);
  final JPasswordField passwordField = new JPasswordField(20);
  final JPasswordField repeatPasswordField = new JPasswordField(20);
  final JButton createAccountButton = new JButton("Create account");
  final JLabel errorLabel = new JLabel(" ");
  final JLabel successLabel = new JLabel(" ");

  /** Where clicks are sent; set by the app shell after the use case is built. */
  private SignupController signupController;

  /**
   * Creates the screen and starts listening to its view model.
   *
   * @param viewModel the Sign-up screen's view model
   * @throws NullPointerException if {@code viewModel} is null
   */
  public SignupView(SignupViewModel viewModel) {
    Objects.requireNonNull(viewModel, "viewModel");
    viewModel.addPropertyChangeListener(this);

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    JLabel title = new JLabel("Sign up");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    errorLabel.setForeground(ERROR_COLOUR);
    successLabel.setForeground(SUCCESS_COLOUR);
    JLabel hint = new JLabel("At least 8 characters.");
    hint.setFont(hint.getFont().deriveFont(11f));

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
    addRow(new JLabel("New accounts start with $100,000.00 cash."), row++, 4);
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
    char[] password = passwordField.getPassword();
    char[] repeatPassword = repeatPasswordField.getPassword();
    try {
      signupController.execute(
          usernameField.getText(), new String(password), new String(repeatPassword));
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
    usernameField.putClientProperty("JComponent.outline", hasError ? "error" : null);

    boolean signedUp = state.signedUpUsername() != null;
    successLabel.setText(signedUp
        ? "Welcome, " + state.signedUpUsername() + ". Your account starts with "
            + state.startingCash() + "."
        : " ");
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
