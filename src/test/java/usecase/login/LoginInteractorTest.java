package usecase.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.InMemorySessionHolder;
import dataaccess.InMemoryUserDataAccessObject;
import entity.Account;
import entity.JournalEntryFactory;
import entity.Money;
import entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import usecase.shared.PasswordHasher;
import usecase.shared.StockDataUnavailableException;

class LoginInteractorTest {

  private static final String PASSWORD = "hunter222";
  private static final Instant NOW = Instant.parse("2026-11-17T14:30:00Z");

  private InMemoryUserDataAccessObject dataAccess;
  private InMemorySessionHolder session;
  private FakeLoginPresenter presenter;
  private RecordingAfterLoginStep afterLoginStep;
  private FakePasswordHasher hasher;
  private LoginInteractor interactor;
  private User alice;

  @BeforeEach
  void setUp() {
    dataAccess = new InMemoryUserDataAccessObject();
    session = new InMemorySessionHolder();
    presenter = new FakeLoginPresenter();
    afterLoginStep = new RecordingAfterLoginStep();
    hasher = new FakePasswordHasher();
    interactor = new LoginInteractor(dataAccess, presenter, hasher, session, afterLoginStep);

    alice = new User("user-1", "Alice", hasher.hash(PASSWORD));
    save(alice);
    save(new User("user-2", "bob", hasher.hash("bobs-password")));
  }

  // ---- Success ----

  @Test
  void execute_correctCredentials_startsSessionAndPresentsUsername() {
    // Action
    interactor.execute(new LoginInputData("Alice", PASSWORD));

    // Assertion
    assertEquals(new LoginOutputData("Alice"), presenter.success);
    assertNull(presenter.error);
    assertEquals(Optional.of(alice), session.currentUser());
  }

  @Test
  void execute_correctCredentials_runsAfterLoginStepOnceAfterLoginSucceeds() {
    // Action
    interactor.execute(new LoginInputData("Alice", PASSWORD));

    // Assertion
    assertEquals(List.of(alice), afterLoginStep.users);
    assertTrue(afterLoginStep.sessionWasStartedFirst);
    assertTrue(afterLoginStep.successWasPresentedFirst);
  }

  @Test
  void execute_afterLoginStepCantGetPrices_stillLogsIn() {
    // Inputs
    afterLoginStep.failure = new StockDataUnavailableException("quotes are down");

    // Action
    interactor.execute(new LoginInputData("Alice", PASSWORD));

    // Assertion
    assertEquals(new LoginOutputData("Alice"), presenter.success);
    assertNull(presenter.error);
    assertEquals(Optional.of(alice), session.currentUser());
    assertEquals(List.of(alice), afterLoginStep.users);
  }

  @Test
  void execute_afterLoginStepHasBug_throwsButLoginIsAlreadyComplete() {
    // Inputs
    afterLoginStep.failure = new IllegalStateException("bug in the step");
    LoginInputData input = new LoginInputData("Alice", PASSWORD);

    // Action
    assertThrows(IllegalStateException.class, () -> interactor.execute(input));

    // Assertion: the bug isn't hidden, but the user isn't left half logged in either
    assertEquals(new LoginOutputData("Alice"), presenter.success);
    assertEquals(Optional.of(alice), session.currentUser());
  }

  @Test
  void execute_usernameInOtherCase_logsInWithSavedSpelling() {
    // Action
    interactor.execute(new LoginInputData("aLICE", PASSWORD));

    // Assertion
    assertEquals(new LoginOutputData("Alice"), presenter.success);
    assertEquals(Optional.of(alice), session.currentUser());
  }

  @Test
  void execute_withNoAfterLoginStep_stillLogsIn() {
    // Inputs
    LoginInteractor withoutStep =
        new LoginInteractor(dataAccess, presenter, hasher, session, AfterLoginStep.NONE);

    // Action
    withoutStep.execute(new LoginInputData("Alice", PASSWORD));

    // Assertion
    assertEquals(new LoginOutputData("Alice"), presenter.success);
    assertEquals(Optional.of(alice), session.currentUser());
  }

  // ---- Wrong password and unknown username ----

  @Test
  void execute_wrongPassword_presentsGenericErrorAndStartsNoSession() {
    // Action
    interactor.execute(new LoginInputData("Alice", "not-the-password"));

    // Assertion
    assertFailedWith(LoginInteractor.INCORRECT_CREDENTIALS);
  }

  @Test
  void execute_otherUsersPassword_presentsGenericErrorAndStartsNoSession() {
    // Action
    interactor.execute(new LoginInputData("Alice", "bobs-password"));

    // Assertion
    assertFailedWith(LoginInteractor.INCORRECT_CREDENTIALS);
  }

  @Test
  void execute_unknownUsername_presentsGenericErrorAndStartsNoSession() {
    // Action
    interactor.execute(new LoginInputData("carol", PASSWORD));

    // Assertion
    assertFailedWith(LoginInteractor.INCORRECT_CREDENTIALS);
  }

  @Test
  void execute_wrongPasswordAndUnknownUsername_showTheSameMessage() {
    // Inputs
    interactor.execute(new LoginInputData("Alice", "not-the-password"));
    String wrongPasswordError = presenter.error;

    // Action
    interactor.execute(new LoginInputData("carol", "not-the-password"));

    // Assertion
    assertEquals(wrongPasswordError, presenter.error);
  }

  @Test
  void execute_wrongPasswordWhileAnotherUserIsLoggedIn_keepsTheirSession() {
    // Inputs
    User bob = dataAccess.findByUsername("bob").orElseThrow();
    session.start(bob);

    // Action
    interactor.execute(new LoginInputData("Alice", "not-the-password"));

    // Assertion
    assertEquals(LoginInteractor.INCORRECT_CREDENTIALS, presenter.error);
    assertEquals(Optional.of(bob), session.currentUser());
  }

  // ---- Missing fields ----

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void execute_missingUsername_asksForBothFields(String username) {
    // Action
    interactor.execute(new LoginInputData(username, PASSWORD));

    // Assertion
    assertFailedWith(LoginInteractor.MISSING_CREDENTIALS);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void execute_missingPassword_asksForBothFields(String password) {
    // Action
    interactor.execute(new LoginInputData("Alice", password));

    // Assertion
    assertFailedWith(LoginInteractor.MISSING_CREDENTIALS);
  }

  // ---- Construction ----

  @Test
  void constructor_nullDependency_throws() {
    // Inputs
    AfterLoginStep step = AfterLoginStep.NONE;

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new LoginInteractor(null, presenter, hasher, session, step));
    assertThrows(NullPointerException.class,
        () -> new LoginInteractor(dataAccess, null, hasher, session, step));
    assertThrows(NullPointerException.class,
        () -> new LoginInteractor(dataAccess, presenter, null, session, step));
    assertThrows(NullPointerException.class,
        () -> new LoginInteractor(dataAccess, presenter, hasher, null, step));
    assertThrows(NullPointerException.class,
        () -> new LoginInteractor(dataAccess, presenter, hasher, session, null));
  }

  // ---- Helpers ----

  /** Saves a user with an account and an opening deposit, the way sign-up does. */
  private void save(User user) {
    String accountId = "acc-" + user.id();
    dataAccess.saveNewUser(
        user,
        new Account(accountId, user.id()),
        JournalEntryFactory.forOpeningDeposit(
            "entry-" + user.id(), accountId, NOW, Money.of("100000.00")));
  }

  /** Checks that login failed with this message, started no session and ran no step. */
  private void assertFailedWith(String expectedError) {
    assertEquals(expectedError, presenter.error);
    assertNull(presenter.success);
    assertTrue(session.currentUser().isEmpty());
    assertTrue(afterLoginStep.users.isEmpty());
  }

  private static class FakeLoginPresenter implements LoginOutputBoundary {
    private LoginOutputData success;
    private String error;

    @Override
    public void prepareSuccessView(LoginOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String error) {
      this.error = error;
    }
  }

  /**
   * Records who it ran for, and whether login had already fully succeeded at that point. Can be
   * told to fail afterwards.
   */
  private class RecordingAfterLoginStep implements AfterLoginStep {
    private final List<User> users = new ArrayList<>();
    private boolean sessionWasStartedFirst;
    private boolean successWasPresentedFirst;
    private Exception failure;

    @Override
    public void afterLogin(User user) throws StockDataUnavailableException {
      users.add(user);
      sessionWasStartedFirst = session.currentUser().equals(Optional.of(user));
      successWasPresentedFirst = presenter.success != null;
      if (failure instanceof StockDataUnavailableException unavailable) {
        throw unavailable;
      }
      if (failure instanceof RuntimeException bug) {
        throw bug;
      }
    }
  }

  /** A fast stand-in for PBKDF2: the "hash" is the password with a prefix. */
  private static class FakePasswordHasher implements PasswordHasher {
    private static final String PREFIX = "hashed:";

    @Override
    public String hash(String rawPassword) {
      return PREFIX + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
      return storedHash.equals(PREFIX + rawPassword);
    }
  }
}
