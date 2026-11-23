package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupPresenter;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.signup.SignupInputData;
import usecase.signup.SignupInteractor;

class SignupViewTest {

  private SignupViewModel viewModel;
  private SignupView view;
  private List<SignupInputData> received;
  private List<Thread> threads;

  @BeforeEach
  void setUp() {
    viewModel = new SignupViewModel();
    view = new SignupView(viewModel);
    received = Collections.synchronizedList(new ArrayList<>());
    threads = Collections.synchronizedList(new ArrayList<>());
    view.setSignupController(new SignupController(input -> {
      received.add(input);
      threads.add(Thread.currentThread());
    }));
  }

  @Test
  void createAccount_typedValues_sendsThemAndClearsPasswords() throws Exception {
    // Inputs
    view.usernameField.setText("alice");
    view.passwordField.setText("hunter222");
    view.repeatPasswordField.setText("hunter222");

    // Action
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt();

    // Assertion
    assertEquals(List.of(new SignupInputData("alice", "hunter222", "hunter222")), received);
    assertEquals(0, view.passwordField.getPassword().length);
    assertEquals(0, view.repeatPasswordField.getPassword().length);
  }

  @Test
  void createAccount_click_runsUseCaseOffSwingThreadAndDisablesButtonMeanwhile()
      throws Exception {
    // Inputs: a use case that waits until the test lets it finish
    CountDownLatch release = new CountDownLatch(1);
    view.setSignupController(new SignupController(input -> {
      threads.add(Thread.currentThread());
      awaitQuietly(release);
    }));
    view.usernameField.setText("alice");

    // Action
    onSwingThread(view.createAccountButton::doClick);
    boolean disabledWhileRunning = !view.createAccountButton.isEnabled();
    release.countDown();
    waitForAttempt();

    // Assertion
    assertTrue(disabledWhileRunning);
    assertTrue(view.createAccountButton.isEnabled());
    assertFalse(threads.isEmpty());
    assertFalse(threads.get(0).getName().startsWith("AWT-EventQueue"));
  }

  @Test
  void createAccount_failsAfterEarlierAttempt_keepsNewlyTypedUsername() throws Exception {
    // Inputs: a use case that always fails, reported through the real presenter
    SignupPresenter presenter = new SignupPresenter(viewModel);
    view.setSignupController(new SignupController(
        input -> presenter.prepareFailView(SignupInteractor.USERNAME_TAKEN)));
    view.usernameField.setText("bob");
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt();
    view.usernameField.setText("Bob");

    // Action
    onSwingThread(view.createAccountButton::doClick);
    waitForAttempt();

    // Assertion
    assertEquals("Bob", view.usernameField.getText());
    assertEquals("! " + SignupInteractor.USERNAME_TAKEN, view.usernameErrorLabel.getText());
  }

  @Test
  void stateWithUsernameError_shown_showsItUnderUsernameOnly() throws Exception {
    // Action
    viewModel.setState(new SignupState(
        "alice", SignupInteractor.USERNAME_TAKEN, SignupState.Field.USERNAME, null, null));
    flushSwingThread();

    // Assertion
    assertEquals("! " + SignupInteractor.USERNAME_TAKEN, view.usernameErrorLabel.getText());
    assertEquals(" ", view.passwordErrorLabel.getText());
    assertEquals(" ", view.repeatPasswordErrorLabel.getText());
    assertEquals("alice", view.usernameField.getText());
  }

  @Test
  void stateWithPasswordError_shown_showsItUnderPasswordAndOutlinesOnlyPassword()
      throws Exception {
    // Action
    viewModel.setState(new SignupState("alice", SignupInteractor.INVALID_PASSWORD_LENGTH,
        SignupState.Field.PASSWORD, null, null));
    flushSwingThread();

    // Assertion
    assertEquals(" ", view.usernameErrorLabel.getText());
    assertEquals("! " + SignupInteractor.INVALID_PASSWORD_LENGTH,
        view.passwordErrorLabel.getText());
    assertNull(view.usernameField.getClientProperty("JComponent.outline"));
    assertNotEquals(null, view.passwordField.getClientProperty("JComponent.outline"));
  }

  @Test
  void stateWithMismatch_shown_showsItUnderConfirmPassword() throws Exception {
    // Action
    viewModel.setState(new SignupState("alice", SignupInteractor.PASSWORDS_DONT_MATCH,
        SignupState.Field.REPEAT_PASSWORD, null, null));
    flushSwingThread();

    // Assertion
    assertEquals(" ", view.passwordErrorLabel.getText());
    assertEquals("! " + SignupInteractor.PASSWORDS_DONT_MATCH,
        view.repeatPasswordErrorLabel.getText());
  }

  @Test
  void stateAfterSignup_shown_displaysWelcomeWithCash() throws Exception {
    // Action
    viewModel.setState(new SignupState("alice", null, null, "alice", "$100,000.00"));
    flushSwingThread();

    // Assertion
    assertTrue(view.successLabel.getText().contains("$100,000.00"));
    assertEquals(" ", view.usernameErrorLabel.getText());
  }

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
  private void waitForAttempt() throws Exception {
    view.lastAttempt.get(5, TimeUnit.SECONDS);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!view.createAccountButton.isEnabled() && System.nanoTime() < deadline) {
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
