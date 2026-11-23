package usecase.login;

import entity.User;
import java.util.Objects;
import java.util.Optional;
import usecase.shared.PasswordHasher;
import usecase.shared.SessionHolder;
import usecase.shared.StockDataUnavailableException;

/**
 * Logs a user in with the username and password they typed.
 *
 * <p>If the username exists (ignoring case) and the password matches its stored hash, the
 * user's session starts, the presenter is told who logged in, and then the
 * {@link AfterLoginStep} runs. Otherwise the presenter gets an error and no session starts.
 *
 * <p>The step runs last so that login is already complete whatever happens in it. If it can't
 * get prices it throws {@link StockDataUnavailableException}, which is caught here: the user
 * stays logged in and the step's work waits for the next price refresh.
 *
 * <p>A wrong password and an unknown username give the same message on purpose, so the login
 * form can't be used to find out which usernames exist.
 */
public class LoginInteractor implements LoginInputBoundary {

  /** Shown when either field is left empty. */
  public static final String MISSING_CREDENTIALS = "Enter your username and password";

  /** Shown for an unknown username and for a wrong password alike. */
  public static final String INCORRECT_CREDENTIALS = "Username or password is incorrect";

  /** Finds users by username. */
  private final LoginUserDataAccessInterface userDataAccess;

  /** Receives the outcome: who logged in, or why login failed. */
  private final LoginOutputBoundary presenter;

  /** Checks the typed password against the stored hash. */
  private final PasswordHasher passwordHasher;

  /** Remembers who is logged in once login succeeds. */
  private final SessionHolder sessionHolder;

  /** Work to do right after a successful login, such as filling open orders. */
  private final AfterLoginStep afterLoginStep;

  /**
   * Creates the interactor.
   *
   * @param userDataAccess where users are looked up
   * @param presenter receives the outcome
   * @param passwordHasher checks the password against the stored hash
   * @param sessionHolder starts the session of the user who logs in
   * @param afterLoginStep runs right after a successful login; {@link AfterLoginStep#NONE} if
   *     there's nothing to do
   * @throws NullPointerException if any argument is null
   */
  public LoginInteractor(
      LoginUserDataAccessInterface userDataAccess,
      LoginOutputBoundary presenter,
      PasswordHasher passwordHasher,
      SessionHolder sessionHolder,
      AfterLoginStep afterLoginStep) {
    this.userDataAccess = Objects.requireNonNull(userDataAccess, "userDataAccess");
    this.presenter = Objects.requireNonNull(presenter, "presenter");
    this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    this.sessionHolder = Objects.requireNonNull(sessionHolder, "sessionHolder");
    this.afterLoginStep = Objects.requireNonNull(afterLoginStep, "afterLoginStep");
  }

  /**
   * Checks the typed username and password and either starts the user's session and reports
   * success, or reports why login failed and starts no session.
   *
   * @param inputData what the user typed
   */
  @Override
  public void execute(LoginInputData inputData) {
    String username = inputData.username();
    String password = inputData.password();
    if (isBlank(username) || isBlank(password)) {
      presenter.prepareFailView(MISSING_CREDENTIALS);
      return;
    }

    Optional<User> found = userDataAccess.findByUsername(username);
    if (found.isEmpty() || !passwordHasher.matches(password, found.get().passwordHash())) {
      presenter.prepareFailView(INCORRECT_CREDENTIALS);
      return;
    }

    User user = found.get();
    sessionHolder.start(user);
    presenter.prepareSuccessView(new LoginOutputData(user.username()));
    try {
      afterLoginStep.afterLogin(user);
    } catch (StockDataUnavailableException e) {
      // Login has already succeeded. Open orders are checked again after the next price
      // refresh (SCRUM-41), so there's nothing to undo or report here.
    }
  }

  /**
   * Returns whether a typed value is missing or only spaces.
   *
   * @param value the value as typed, possibly null
   * @return true if {@code value} is null or blank
   */
  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
