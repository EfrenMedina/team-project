package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import interfaceadapter.login.LoginController;
import interfaceadapter.login.LoginPresenter;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupPresenter;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.login.LoginInputData;
import usecase.login.LoginInteractor;
import usecase.signup.SignupInputData;
import usecase.signup.SignupInteractor;

class LoginViewTest {

  private LoginViewModel loginViewModel;
  private SignupViewModel signupViewModel;
  private LoginView view;
  private List<LoginInputData> logins;
  private List<SignupInputData> signups;
  private List<Thread> threads;

  @BeforeEach
  void setUp() {
    loginViewModel = new LoginViewModel();
    signupViewModel = new SignupViewModel();
    view = new LoginView(loginViewModel, signupViewModel);
    logins = Collections.synchronizedList(new ArrayList<>());
    signups = Collections.synchronizedList(new ArrayList<>());
    threads = Collections.synchronizedList(new ArrayList<>());
    view.setLoginController(new LoginController(input -> {
      logins.add(input);
      threads.add(Thread.currentThread());
    }));
    view.setSignupController(new SignupController(input -> {
      signups.add(input);
      threads.add(Thread.currentThread());
    }));
  }

  // ---- Log in ----

  @Test
  void logIn_typedValues_sendsThemAndClearsPassword() throws Exception {
    // Inputs
    view.logInUsernameField.setText("alice");
    view.logInPasswordField.setText("hunter222");

    // Action
    onSwingThread(view.logInButton::doClick);
    waitForAttempt(view.logInButton);

    // Assertion
    assertEquals(List.of(new LoginInputData("alice", "hunter222")), logins);
    assertEquals(0, view.logInPasswordField.getPassword().length);
  }

  @Test
  void logIn_enterInPasswordField_sendsTypedValues() throws Exception {
    // Inputs
    view.logInUsernameField.setText("alice");
    view.logInPasswordField.setText("hunter222");

    // Action
    onSwingThread(view.logInPasswordField::postActionEvent);
    waitForAttempt(view.logInButton);

    // Assertion
    assertEquals(List.of(new LoginInputData("alice", "hunter222")), logins);
  }

  @Test
  void logIn_click_runsUseCaseOffSwingThreadAndDisablesButtonMeanwhile() throws Exception {
    // Inputs: a use case that waits until the test lets it finish
    CountDownLatch release = new CountDownLatch(1);
    view.setLoginController(new LoginController(input -> {
      threads.add(Thread.currentThread());
      awaitQuietly(release);
    }));

    // Action
    onSwingThread(view.logInButton::doClick);
    boolean disabledWhileRunning = !view.logInButton.isEnabled();
    release.countDown();
    waitForAttempt(view.logInButton);

    // Assertion
    assertTrue(disabledWhileRunning);
    assertTrue(view.logInButton.isEnabled());
    assertFalse(threads.get(0).getName().startsWith("AWT-EventQueue"));
  }

  @Test
  void logIn_fails_showsGenericErrorAndKeepsTypedUsername() throws Exception {
    // Inputs: a use case that always fails, reported through the real presenter
    LoginPresenter presenter = new LoginPresenter(
        loginViewModel, new LoggedInViewModel(), new ViewManagerModel());
    view.setLoginController(new LoginController(
        input -> presenter.prepareFailView(LoginInteractor.INCORRECT_CREDENTIALS)));
    view.logInUsernameField.setText("Bob");

    // Action
    onSwingThread(view.logInButton::doClick);
    waitForAttempt(view.logInButton);

    // Assertion
    assertEquals("Bob", view.logInUsernameField.getText());
    assertEquals("! Username or password is incorrect", view.logInErrorLabel.getText());
    assertNotNull(view.logInUsernameField.getClientProperty("JComponent.outline"));
    assertNotNull(view.logInPasswordField.getClientProperty("JComponent.outline"));
  }

  @Test
  void logInStateCleared_shown_emptiesFormAndRemovesError() throws Exception {
    // Inputs
    loginViewModel.setState(
        new LoginState("alice", LoginInteractor.INCORRECT_CREDENTIALS, null));
    flushSwingThread();

    // Action
    loginViewModel.clearUserData();
    flushSwingThread();

    // Assertion
    assertEquals("", view.logInUsernameField.getText());
    assertEquals(" ", view.logInErrorLabel.getText());
    assertNull(view.logInUsernameField.getClientProperty("JComponent.outline"));
  }

  // ---- Sign up ----

  @Test
  void createAccount_typedValues_sendsThemAndClearsPasswords() throws Exception {
    // Inputs
    view.signUpUsernameField.setText("alice");
    view.signUpPasswordField.setText("hunter222");
    view.signUpRepeatPasswordField.setText("hunter222");

    // Action
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt(view.createAccountButton);

    // Assertion
    assertEquals(List.of(new SignupInputData("alice", "hunter222", "hunter222")), signups);
    assertEquals(0, view.signUpPasswordField.getPassword().length);
    assertEquals(0, view.signUpRepeatPasswordField.getPassword().length);
    assertFalse(threads.get(0).getName().startsWith("AWT-EventQueue"));
  }

  @Test
  void createAccount_failsAfterEarlierAttempt_keepsNewlyTypedUsername() throws Exception {
    // Inputs: a use case that always fails, reported through the real presenter
    SignupPresenter presenter = new SignupPresenter(
        signupViewModel, new LoggedInViewModel(), new ViewManagerModel());
    view.setSignupController(new SignupController(
        input -> presenter.prepareFailView(SignupInteractor.USERNAME_TAKEN)));
    view.signUpUsernameField.setText("bob");
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt(view.createAccountButton);
    view.signUpUsernameField.setText("Bob");

    // Action
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt(view.createAccountButton);

    // Assertion
    assertEquals("Bob", view.signUpUsernameField.getText());
    assertEquals("! " + SignupInteractor.USERNAME_TAKEN,
        view.signUpUsernameErrorLabel.getText());
  }

  @Test
  void signUpStateWithPasswordError_shown_showsItUnderPasswordOnly() throws Exception {
    // Action
    signupViewModel.setState(new SignupState("alice", SignupInteractor.INVALID_PASSWORD_LENGTH,
        SignupState.Field.PASSWORD, null, null));
    flushSwingThread();

    // Assertion
    assertEquals(" ", view.signUpUsernameErrorLabel.getText());
    assertEquals("! " + SignupInteractor.INVALID_PASSWORD_LENGTH,
        view.signUpPasswordErrorLabel.getText());
    assertEquals(" ", view.signUpRepeatPasswordErrorLabel.getText());
    assertNull(view.signUpUsernameField.getClientProperty("JComponent.outline"));
    assertNotNull(view.signUpPasswordField.getClientProperty("JComponent.outline"));
  }

  @Test
  void signUpStateWithMismatch_shown_showsItUnderConfirmPassword() throws Exception {
    // Action
    signupViewModel.setState(new SignupState("alice", SignupInteractor.PASSWORDS_DONT_MATCH,
        SignupState.Field.REPEAT_PASSWORD, null, null));
    flushSwingThread();

    // Assertion
    assertEquals(" ", view.signUpPasswordErrorLabel.getText());
    assertEquals("! " + SignupInteractor.PASSWORDS_DONT_MATCH,
        view.signUpRepeatPasswordErrorLabel.getText());
  }

  // ---- Wiring ----

  @Test
  void buttons_noControllersYet_doNothing() throws Exception {
    // Inputs
    LoginView unwired = new LoginView(new LoginViewModel(), new SignupViewModel());
    unwired.logInPasswordField.setText("hunter222");

    // Action
    onSwingThread(() -> {
      unwired.logInButton.doClick();
      unwired.createAccountButton.doClick();
    });

    // Assertion
    assertNull(unwired.lastAttempt);
    assertEquals("hunter222", new String(unwired.logInPasswordField.getPassword()));
  }

  @Test
  void getViewName_isLoginScreen() {
    // Action / Assertion
    assertEquals(ViewNames.LOGIN, view.getViewName());
    assertEquals(ViewNames.LOGIN, view.getName());
  }

  @Test
  void constructor_nullViewModel_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LoginView(null, signupViewModel));
    assertThrows(NullPointerException.class, () -> new LoginView(loginViewModel, null));
  }

  // ---- Helpers ----

  /** Runs an action on the Swing thread and waits for it, as a real click would. */
  private static void onSwingThread(Runnable action) throws Exception {
    SwingUtilities.invokeAndWait(action);
  }

  /** Lets every redraw already queued on the Swing thread run. */
  private static void flushSwingThread() throws Exception {
    SwingUtilities.invokeAndWait(() -> { });
  }

  /**
   * Waits for the background attempt, then for the redraws and the button to come back.
   * SwingWorker calls done() through a Swing timer, so this waits on time, not on the queue.
   */
  private void waitForAttempt(JButton button) throws Exception {
    view.lastAttempt.get(5, TimeUnit.SECONDS);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!button.isEnabled() && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    flushSwingThread();
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
