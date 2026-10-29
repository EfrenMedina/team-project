package usecase.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Money;
import entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import usecase.IdGenerator;
import usecase.PasswordHasher;

class SignupInteractorTest {

  private static final String PASSWORD = "hunter222";
  private static final String LONGEST_PASSWORD =
      "abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz" + "abcdefghijkl";
  private static final String TOO_LONG_PASSWORD = LONGEST_PASSWORD + "m";

  private FakeUserDataAccess dataAccess;
  private FakeSignupPresenter presenter;
  private FakePasswordHasher hasher;
  private SignupInteractor interactor;

  @BeforeEach
  void setUp() {
    dataAccess = new FakeUserDataAccess();
    presenter = new FakeSignupPresenter();
    hasher = new FakePasswordHasher();
    interactor = new SignupInteractor(dataAccess, presenter, hasher, new SequentialIds());
  }

  // ---- Success ----

  @Test
  void execute_validInput_savesUserAndPresentsStartingCash() {
    // Inputs
    SignupInputData input = new SignupInputData("alice", PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    assertEquals(1, dataAccess.saved.size());
    User saved = dataAccess.saved.get(0);
    assertEquals("user-1", saved.id());
    assertEquals("alice", saved.username());
    assertEquals(new SignupOutputData("alice", Money.of("100000.00")), presenter.success);
    assertNull(presenter.error);
  }

  @Test
  void execute_validInput_storesHashFromHasherNotPlainPassword() {
    // Inputs
    SignupInputData input = new SignupInputData("alice", PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    String storedHash = dataAccess.saved.get(0).passwordHash();
    assertEquals(PASSWORD, hasher.lastRawPassword);
    assertEquals(FakePasswordHasher.HASH, storedHash);
    assertNotEquals(PASSWORD, storedHash);
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "abcdefghij0123456789", "Jordan_Lee_99", "___"})
  void execute_usernameAtLimitsOrWithUnderscores_succeeds(String username) {
    // Inputs
    SignupInputData input = new SignupInputData(username, PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    assertEquals(username, presenter.success.username());
    assertEquals(1, dataAccess.saved.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345678", LONGEST_PASSWORD})
  void execute_passwordAtLengthLimits_succeeds(String password) {
    // Inputs
    SignupInputData input = new SignupInputData("alice", password, password);

    // Action
    interactor.execute(input);

    // Assertion
    assertEquals("alice", presenter.success.username());
  }

  // ---- Username rule ----

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"ab", "abcdefghij0123456789x", "jordan.lee", "al ice", "alice!", "élise"})
  void execute_invalidUsername_presentsErrorAndSavesNothing(String username) {
    // Inputs
    SignupInputData input = new SignupInputData(username, PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.INVALID_USERNAME);
  }

  // ---- Password length rule ----

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1234567", TOO_LONG_PASSWORD})
  void execute_passwordWrongLength_presentsErrorAndSavesNothing(String password) {
    // Inputs
    SignupInputData input = new SignupInputData("alice", password, password);

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.INVALID_PASSWORD_LENGTH);
  }

  // ---- Passwords match rule ----

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"hunter223", "Hunter222", ""})
  void execute_passwordsDontMatch_presentsErrorAndSavesNothing(String repeatPassword) {
    // Inputs
    SignupInputData input = new SignupInputData("alice", PASSWORD, repeatPassword);

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.PASSWORDS_DONT_MATCH);
  }

  // ---- Username taken rule ----

  @Test
  void execute_usernameTaken_presentsErrorAndSavesNothing() {
    // Inputs
    dataAccess.takenUsernames.add("alice");
    SignupInputData input = new SignupInputData("alice", PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.USERNAME_TAKEN);
  }

  @Test
  void execute_usernameTakenInOtherCase_presentsErrorAndSavesNothing() {
    // Inputs
    dataAccess.takenUsernames.add("alice");
    SignupInputData input = new SignupInputData("ALICE", PASSWORD, PASSWORD);

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.USERNAME_TAKEN);
  }

  // ---- Rule order ----

  @Test
  void execute_badUsernameAndMismatchedPasswords_reportsUsernameFirst() {
    // Inputs
    SignupInputData input = new SignupInputData("ab", "short", "different");

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.INVALID_USERNAME);
  }

  @Test
  void execute_takenUsernameAndMismatchedPasswords_reportsMismatchFirst() {
    // Inputs
    dataAccess.takenUsernames.add("alice");
    SignupInputData input = new SignupInputData("alice", PASSWORD, "hunter223");

    // Action
    interactor.execute(input);

    // Assertion
    assertFailedWith(SignupInteractor.PASSWORDS_DONT_MATCH);
  }

  // ---- Constructor ----

  @Test
  void constructor_nullDependency_throws() {
    // Inputs
    SequentialIds ids = new SequentialIds();

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new SignupInteractor(null, presenter, hasher, ids));
    assertThrows(NullPointerException.class,
        () -> new SignupInteractor(dataAccess, null, hasher, ids));
    assertThrows(NullPointerException.class,
        () -> new SignupInteractor(dataAccess, presenter, null, ids));
    assertThrows(NullPointerException.class,
        () -> new SignupInteractor(dataAccess, presenter, hasher, null));
  }

  /** Checks that sign-up failed with this message, saved nothing and never hashed. */
  private void assertFailedWith(String expectedError) {
    assertEquals(expectedError, presenter.error);
    assertNull(presenter.success);
    assertTrue(dataAccess.saved.isEmpty());
    assertNull(hasher.lastRawPassword);
  }

  // ---- Test doubles ----

  /** Records saved users; a username counts as taken if it is in takenUsernames. */
  private static class FakeUserDataAccess implements SignupUserDataAccessInterface {
    final List<String> takenUsernames = new ArrayList<>();
    final List<User> saved = new ArrayList<>();

    @Override
    public boolean existsByUsername(String username) {
      return takenUsernames.contains(username.toLowerCase(Locale.ROOT));
    }

    @Override
    public void saveNewUser(User user) {
      saved.add(user);
    }
  }

  /** Records the last thing the interactor presented. */
  private static class FakeSignupPresenter implements SignupOutputBoundary {
    SignupOutputData success;
    String error;

    @Override
    public void prepareSuccessView(SignupOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String error) {
      this.error = error;
    }
  }

  /** Returns a fixed hash instantly and remembers which password it was given. */
  private static class FakePasswordHasher implements PasswordHasher {
    static final String HASH = "fake-hash";
    String lastRawPassword;

    @Override
    public String hash(String rawPassword) {
      lastRawPassword = rawPassword;
      return HASH;
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
      throw new UnsupportedOperationException("Sign-up never checks passwords");
    }
  }

  /** Returns "user-1", "user-2", ... so tests can predict ids. */
  private static class SequentialIds implements IdGenerator {
    private int next = 1;

    @Override
    public String nextId() {
      return "user-" + next++;
    }
  }
}
