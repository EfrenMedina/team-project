package view;

import interfaceadapter.ViewNames;
import interfaceadapter.login.LoginController;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * The Login and Sign-up screen (wireframe 01): a tagline on the left and a card with "Log in"
 * and "Sign up" tabs on the right. Each form sends what the user typed to its controller and
 * redraws whenever its view model changes.
 *
 * <p>Both use cases hash a password, which takes a moment, so they run in the background: the
 * window stays responsive and the form's button is disabled until the attempt finishes.
 * Passwords are wiped from the form as soon as an attempt starts. Errors show under the field
 * they are about. On success the presenters switch to the Dashboard; this screen never switches
 * screens itself.
 */
public class LoginView extends JPanel {

  private static final String LOG_IN = "Log in";
  private static final String SIGN_UP = "Sign up";
  private static final int CARD_WIDTH = 280;
  private static final int CARD_HEIGHT = 500;
  private static final int FIELD_WIDTH = 232;

  final JTextField logInUsernameField = Theme.textField();
  final JPasswordField logInPasswordField = Theme.passwordField();
  final JLabel logInErrorLabel = errorLabel();
  final JButton logInButton = Theme.primaryButton(LOG_IN);

  final JTextField signUpUsernameField = Theme.textField();
  final JPasswordField signUpPasswordField = Theme.passwordField();
  final JPasswordField signUpRepeatPasswordField = Theme.passwordField();
  final JLabel signUpUsernameErrorLabel = errorLabel();
  final JLabel signUpPasswordErrorLabel = errorLabel();
  final JLabel signUpRepeatPasswordErrorLabel = errorLabel();
  final JButton createAccountButton = Theme.primaryButton("Create account");

  final JButton logInTab = segment(LOG_IN);
  final JButton signUpTab = segment(SIGN_UP);

  /** The latest background attempt, so tests can wait for it. */
  SwingWorker<Void, Void> lastAttempt;

  private final CardLayout formLayout = new CardLayout();
  private final JPanel forms = new JPanel(formLayout);

  /** The Log-in form's state; the view records the typed username in it before each try. */
  private final LoginViewModel loginViewModel;

  /** The Sign-up form's state; the view records the typed username in it before each try. */
  private final SignupViewModel signupViewModel;

  /** Where "Log in" clicks are sent; set by the app after the use case is built. */
  private LoginController loginController;

  /** Where "Create account" clicks are sent; set by the app after the use case is built. */
  private SignupController signupController;

  /**
   * Builds the screen and starts listening to both forms' view models.
   *
   * @param loginViewModel the Log-in form's view model
   * @param signupViewModel the Sign-up form's view model
   * @throws NullPointerException if either argument is null
   */
  public LoginView(LoginViewModel loginViewModel, SignupViewModel signupViewModel) {
    super(new BorderLayout());
    this.loginViewModel = Objects.requireNonNull(loginViewModel, "loginViewModel");
    this.signupViewModel = Objects.requireNonNull(signupViewModel, "signupViewModel");
    setName(ViewNames.LOGIN);
    setBackground(Theme.BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder(96, 96, 96, 96));

    JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    logoRow.setOpaque(false);
    logoRow.add(new Logo());
    add(logoRow, BorderLayout.NORTH);

    JPanel body = new JPanel(new GridBagLayout());
    body.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.weightx = 1;
    c.anchor = GridBagConstraints.WEST;
    body.add(tagline(), c);
    c.weightx = 0;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(0, 0, 0, 216);
    body.add(card(), c);
    add(body, BorderLayout.CENTER);

    loginViewModel.addPropertyChangeListener(event -> {
      if (LoginViewModel.STATE_PROPERTY.equals(event.getPropertyName())) {
        onSwingThread(() -> showLoginState((LoginState) event.getNewValue()));
      }
    });
    signupViewModel.addPropertyChangeListener(event -> {
      if (SignupViewModel.STATE_PROPERTY.equals(event.getPropertyName())) {
        onSwingThread(() -> showSignupState((SignupState) event.getNewValue()));
      }
    });
    showLoginState(loginViewModel.getState());
    showSignupState(signupViewModel.getState());
  }

  /**
   * Sets where "Log in" clicks are sent. The app calls this once, after building the login use
   * case.
   *
   * @param loginController the controller to call
   */
  public void setLoginController(LoginController loginController) {
    this.loginController = loginController;
  }

  /**
   * Sets where "Create account" clicks are sent. The app calls this once, after building the
   * sign-up use case.
   *
   * @param signupController the controller to call
   */
  public void setSignupController(SignupController signupController) {
    this.signupController = signupController;
  }

  /**
   * Returns the name the app uses to switch to this screen.
   *
   * @return {@value ViewNames#LOGIN}
   */
  public String getViewName() {
    return ViewNames.LOGIN;
  }

  // ---- Log in ----

  /** Sends what the user typed to the login controller in the background. */
  private void logIn() {
    if (loginController == null) {
      return;
    }
    String username = logInUsernameField.getText();
    // Record what was typed, so a failed attempt redraws with this username, not an old one.
    loginViewModel.setState(new LoginState(username, null, null));
    String password = takePassword(logInPasswordField);
    LoginController controller = loginController;
    runInBackground(logInButton, () -> controller.execute(username, password));
  }

  /**
   * Shows the Log-in form's state: the typed username and the error, if any. Both fields are
   * outlined while there's an error, because the message doesn't say which one is wrong.
   *
   * @param state what to show
   */
  private void showLoginState(LoginState state) {
    setTextIfChanged(logInUsernameField, state.username());
    boolean hasError = state.error() != null;
    logInErrorLabel.setText(hasError ? "! " + state.error() : " ");
    outline(logInUsernameField, hasError);
    outline(logInPasswordField, hasError);
  }

  // ---- Sign up ----

  /** Sends what the user typed to the sign-up controller in the background. */
  private void signUp() {
    if (signupController == null) {
      return;
    }
    String username = signUpUsernameField.getText();
    // Record what was typed, so a failed attempt redraws with this username, not an old one.
    signupViewModel.setState(new SignupState(username, null, null, null, null));
    String password = takePassword(signUpPasswordField);
    String repeatPassword = takePassword(signUpRepeatPasswordField);
    SignupController controller = signupController;
    runInBackground(
        createAccountButton, () -> controller.execute(username, password, repeatPassword));
  }

  /**
   * Shows the Sign-up form's state: the typed username, and the error under the field it is
   * about, with only that field outlined.
   *
   * @param state what to show
   */
  private void showSignupState(SignupState state) {
    setTextIfChanged(signUpUsernameField, state.username());
    showFieldError(state, SignupState.Field.USERNAME, signUpUsernameField,
        signUpUsernameErrorLabel);
    showFieldError(state, SignupState.Field.PASSWORD, signUpPasswordField,
        signUpPasswordErrorLabel);
    showFieldError(state, SignupState.Field.REPEAT_PASSWORD, signUpRepeatPasswordField,
        signUpRepeatPasswordErrorLabel);
  }

  /**
   * Shows a sign-up error under one field if it is about that field, and clears it otherwise.
   *
   * @param state the Sign-up form's state
   * @param field which field this is
   * @param input the field itself, outlined when the error is about it
   * @param errorLabel the line under the field
   */
  private static void showFieldError(
      SignupState state, SignupState.Field field, JTextField input, JLabel errorLabel) {
    boolean here = state.error() != null && state.errorField() == field;
    errorLabel.setText(here ? "! " + state.error() : " ");
    outline(input, here);
  }

  // ---- Shared behaviour ----

  /**
   * Runs a use case off the Swing thread, with the form's button disabled until it finishes.
   *
   * @param button the button that started the attempt
   * @param attempt the controller call
   */
  private void runInBackground(JButton button, Runnable attempt) {
    button.setEnabled(false);
    lastAttempt = new SwingWorker<>() {
      @Override
      protected Void doInBackground() {
        attempt.run();
        return null;
      }

      @Override
      protected void done() {
        button.setEnabled(true);
      }
    };
    lastAttempt.execute();
  }

  /**
   * Reads a password field and empties it, wiping the copy Swing handed out.
   *
   * @param field the password field
   * @return what was typed
   */
  private static String takePassword(JPasswordField field) {
    char[] chars = field.getPassword();
    String password = new String(chars);
    Arrays.fill(chars, '\0');
    field.setText("");
    return password;
  }

  /**
   * Runs an action on the Swing thread: now if this is the Swing thread, otherwise soon. The
   * presenters update view models from the background attempt, so redraws must be moved here.
   *
   * @param action what to run
   */
  private static void onSwingThread(Runnable action) {
    if (SwingUtilities.isEventDispatchThread()) {
      action.run();
    } else {
      SwingUtilities.invokeLater(action);
    }
  }

  private static void setTextIfChanged(JTextField field, String text) {
    if (!field.getText().equals(text)) {
      field.setText(text);
    }
  }

  private static void outline(JComponent field, boolean error) {
    // FlatLaf client property; other look and feels ignore it.
    field.putClientProperty("JComponent.outline", error ? Theme.DOWN : null);
  }

  // ---- Layout ----

  private static JComponent tagline() {
    JPanel panel = column();
    panel.add(Theme.label("Practise investing", 40, true, Theme.TEXT));
    panel.add(Box.createVerticalStrut(16));
    panel.add(Theme.h2("with $100,000.00 of simulated money."));
    panel.add(Box.createVerticalStrut(16));
    panel.add(Theme.small("Real prices. No real risk."));
    return panel;
  }

  private JComponent card() {
    forms.setOpaque(false);
    forms.setAlignmentX(LEFT_ALIGNMENT);
    forms.add(logInForm(), LOG_IN);
    forms.add(signUpForm(), SIGN_UP);

    logInTab.addActionListener(e -> showForm(LOG_IN));
    signUpTab.addActionListener(e -> showForm(SIGN_UP));
    RoundedPanel tabs = new RoundedPanel(new GridLayout(1, 2, 4, 0), Theme.INPUT, 8);
    tabs.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    tabs.add(logInTab);
    tabs.add(signUpTab);
    tabs.setAlignmentX(LEFT_ALIGNMENT);
    tabs.setPreferredSize(new Dimension(FIELD_WIDTH, 36));
    tabs.setMaximumSize(new Dimension(FIELD_WIDTH, 36));
    showForm(LOG_IN);

    JPanel content = column();
    content.add(tabs);
    content.add(Box.createVerticalStrut(24));
    content.add(forms);

    RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.SURFACE, 16);
    card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
    card.add(content, BorderLayout.NORTH);
    return card;
  }

  private void showForm(String name) {
    formLayout.show(forms, name);
    styleSegment(logInTab, LOG_IN.equals(name));
    styleSegment(signUpTab, SIGN_UP.equals(name));
  }

  private JComponent logInForm() {
    JPanel form = column();
    addField(form, "Username", logInUsernameField, null);
    addField(form, "Password", logInPasswordField, logInErrorLabel);
    form.add(Box.createVerticalStrut(8));
    logInButton.addActionListener(e -> logIn());
    // Pressing Enter in either field logs in, like clicking the button.
    logInUsernameField.addActionListener(e -> logIn());
    logInPasswordField.addActionListener(e -> logIn());
    form.add(fullWidth(logInButton, 44));
    form.add(Box.createVerticalStrut(20));
    JButton switchToSignUp = Theme.linkButton("New here? Sign up", 12, true, Theme.MUTED);
    switchToSignUp.addActionListener(e -> showForm(SIGN_UP));
    form.add(switchToSignUp);
    return form;
  }

  private JComponent signUpForm() {
    JPanel form = column();
    addField(form, "Username", signUpUsernameField, signUpUsernameErrorLabel);
    addField(form, "Password", signUpPasswordField, signUpPasswordErrorLabel);
    addField(form, "Confirm password", signUpRepeatPasswordField,
        signUpRepeatPasswordErrorLabel);
    form.add(Theme.footnote("At least 8 characters."));
    form.add(Box.createVerticalStrut(16));
    createAccountButton.addActionListener(e -> signUp());
    signUpRepeatPasswordField.addActionListener(e -> signUp());
    form.add(fullWidth(createAccountButton, 44));
    form.add(Box.createVerticalStrut(20));
    form.add(Theme.small("You start with $100,000.00 cash."));
    return form;
  }

  /**
   * Adds a labelled field to a form, with an optional error line under it.
   *
   * @param form the form
   * @param label the text above the field
   * @param field the field
   * @param errorLabel the line under the field for its errors, or {@code null} for none
   */
  private static void addField(
      JPanel form, String label, JComponent field, JLabel errorLabel) {
    form.add(Theme.small(label));
    form.add(Box.createVerticalStrut(6));
    form.add(fullWidth(field, 40));
    if (errorLabel != null) {
      form.add(Box.createVerticalStrut(4));
      form.add(errorLabel);
    }
    form.add(Box.createVerticalStrut(errorLabel != null ? 8 : 14));
  }

  private static JLabel errorLabel() {
    JLabel label = Theme.label(" ", 12, false, Theme.DOWN);
    label.setAlignmentX(LEFT_ALIGNMENT);
    return label;
  }

  private static JComponent fullWidth(JComponent component, int height) {
    component.setAlignmentX(LEFT_ALIGNMENT);
    Dimension size = new Dimension(FIELD_WIDTH, height);
    component.setPreferredSize(size);
    component.setMaximumSize(size);
    return component;
  }

  private static JButton segment(String text) {
    JButton button = Theme.flatButton(text, false);
    button.setHorizontalAlignment(JButton.CENTER);
    return button;
  }

  private static void styleSegment(JButton button, boolean selected) {
    button.setFont(Theme.font(14, selected));
    button.setForeground(selected ? Theme.TEXT : Theme.MUTED);
    button.setBackground(selected ? Theme.RAISED : Theme.INPUT);
  }

  private static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setAlignmentX(LEFT_ALIGNMENT);
    return panel;
  }
}
