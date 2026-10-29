package usecase.signup;

import entity.Money;
import entity.User;
import java.util.Objects;
import java.util.regex.Pattern;
import usecase.IdGenerator;
import usecase.PasswordHasher;

/**
 * Creates a new user from the sign-up form. The rules are checked in this order, and the
 * first one that fails is reported:
 *
 * <ol>
 *   <li>the username is 3 to 20 letters, digits or underscores;
 *   <li>the password is 8 to 64 characters;
 *   <li>the two passwords match;
 *   <li>the username is not taken (ignoring case).
 * </ol>
 *
 * <p>If every rule passes, the password is hashed, the user is saved and the presenter is
 * told the account starts with $100,000.00. If any rule fails, nothing is saved.
 */
public class SignupInteractor implements SignupInputBoundary {

  /** Shown when the username has the wrong length or characters. */
  public static final String INVALID_USERNAME =
      "Username must be 3 to 20 letters, digits or underscores.";

  /** Shown when the password is shorter than 8 or longer than 64 characters. */
  public static final String INVALID_PASSWORD_LENGTH = "Password must be 8 to 64 characters.";

  /** Shown when the password and the repeated password differ. */
  public static final String PASSWORDS_DONT_MATCH = "Passwords don't match.";

  /** Shown when another user already has the username, ignoring case. */
  public static final String USERNAME_TAKEN = "That username is already taken.";

  /** 3 to 20 letters, digits or underscores, and nothing else. */
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

  /** The shortest password allowed, in characters. */
  private static final int MIN_PASSWORD_LENGTH = 8;

  /** The longest password allowed, in characters. */
  private static final int MAX_PASSWORD_LENGTH = 64;

  /** The cash every new account starts with: $100,000.00. */
  private static final Money OPENING_DEPOSIT = Money.ofCents(10_000_000);

  /** Checks whether usernames are taken and saves new users. */
  private final SignupUserDataAccessInterface userDataAccess;

  /** Receives the outcome: the new user, or the reason sign-up failed. */
  private final SignupOutputBoundary presenter;

  /** Hashes passwords so the plain text is never saved. */
  private final PasswordHasher passwordHasher;

  /** Gives each new user a unique id. */
  private final IdGenerator idGenerator;

  /**
   * Creates the interactor.
   *
   * @param userDataAccess where users are checked and saved
   * @param presenter receives the outcome
   * @param passwordHasher hashes the password before it is saved
   * @param idGenerator gives the new user's id
   */
  public SignupInteractor(
      SignupUserDataAccessInterface userDataAccess,
      SignupOutputBoundary presenter,
      PasswordHasher passwordHasher,
      IdGenerator idGenerator) {
    this.userDataAccess = Objects.requireNonNull(userDataAccess, "userDataAccess");
    this.presenter = Objects.requireNonNull(presenter, "presenter");
    this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
  }

  /**
   * Checks the input against the sign-up rules and either saves the new user and reports
   * success, or reports the first rule that failed and saves nothing.
   *
   * @param inputData what the user typed
   */
  @Override
  public void execute(SignupInputData inputData) {
    String error = validate(inputData);
    if (error != null) {
      presenter.prepareFailView(error);
      return;
    }

    String username = inputData.username();
    if (userDataAccess.existsByUsername(username)) {
      presenter.prepareFailView(USERNAME_TAKEN);
      return;
    }

    String passwordHash = passwordHasher.hash(inputData.password());
    User user = new User(idGenerator.nextId(), username, passwordHash);
    // SCRUM-25: build the Account and opening deposit here and save them with the user.
    userDataAccess.saveNewUser(user);
    presenter.prepareSuccessView(new SignupOutputData(username, OPENING_DEPOSIT));
  }

  /**
   * Checks the format rules: the username, the password length and that both passwords
   * match. Doesn't check whether the username is taken.
   *
   * @param inputData what the user typed
   * @return the message for the first rule that fails, or {@code null} if all of them pass
   */
  private static String validate(SignupInputData inputData) {
    String username = inputData.username();
    String password = inputData.password();

    if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
      return INVALID_USERNAME;
    }
    if (password == null
        || password.length() < MIN_PASSWORD_LENGTH
        || password.length() > MAX_PASSWORD_LENGTH) {
      return INVALID_PASSWORD_LENGTH;
    }
    if (!password.equals(inputData.repeatPassword())) {
      return PASSWORDS_DONT_MATCH;
    }
    return null;
  }
}
