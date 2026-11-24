package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.InMemorySessionHolder;
import dataaccess.InMemoryUserDataAccessObject;
import dataaccess.Pbkdf2PasswordHasher;
import entity.IdGenerator;
import interfaceadapter.ClearedOnLogout;
import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import interfaceadapter.login.LoginController;
import interfaceadapter.login.LoginPresenter;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import interfaceadapter.logout.LogoutController;
import interfaceadapter.logout.LogoutPresenter;
import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupPresenter;
import interfaceadapter.signup.SignupState;
import interfaceadapter.signup.SignupViewModel;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.login.AfterLoginStep;
import usecase.login.LoginInteractor;
import usecase.logout.LogoutInteractor;
import usecase.shared.PasswordHasher;
import usecase.signup.SignupInteractor;

/**
 * Signs up, logs out and logs back in through the real controllers, interactors, presenters
 * and in-memory storage, wired the way {@link AppBuilder} wires them, but without the screens.
 */
class LoginLogoutFlowTest {

  private static final String PASSWORD = "hunter222";

  private InMemorySessionHolder session;
  private ViewManagerModel viewManager;
  private LoggedInViewModel loggedIn;
  private LoginViewModel loginViewModel;
  private SignupViewModel signupViewModel;
  private SignupController signupController;
  private LoginController loginController;
  private LogoutController logoutController;

  @BeforeEach
  void setUp() {
    session = new InMemorySessionHolder();
    viewManager = new ViewManagerModel();
    viewManager.setState(ViewNames.LOGIN);
    loggedIn = new LoggedInViewModel();
    loginViewModel = new LoginViewModel();
    signupViewModel = new SignupViewModel();
    InMemoryUserDataAccessObject users = new InMemoryUserDataAccessObject();
    PasswordHasher hasher = new Pbkdf2PasswordHasher(1);

    AtomicInteger lastId = new AtomicInteger();
    IdGenerator ids = () -> "id-" + lastId.incrementAndGet();
    Clock clock = Clock.fixed(Instant.parse("2026-11-17T14:30:00Z"), ZoneOffset.UTC);
    signupController = new SignupController(new SignupInteractor(
        users, new SignupPresenter(signupViewModel, loggedIn, viewManager), hasher, ids, clock,
        session));
    loginController = new LoginController(new LoginInteractor(
        users, new LoginPresenter(loginViewModel, loggedIn, viewManager), hasher, session,
        AfterLoginStep.NONE));
    List<ClearedOnLogout> screens = List.of(loggedIn, loginViewModel, signupViewModel);
    logoutController = new LogoutController(
        new LogoutInteractor(session, new LogoutPresenter(screens, viewManager)));
  }

  @Test
  void signUp_valid_logsInAndShowsDashboard() {
    // Action
    signupController.execute("Alice", PASSWORD, PASSWORD);

    // Assertion
    assertEquals(ViewNames.DASHBOARD, viewManager.getState());
    assertEquals("Alice", loggedIn.getState());
    assertEquals("id-1", session.currentUser().orElseThrow().id());
  }

  @Test
  void logOut_afterSignup_clearsEveryScreenAndShowsLogin() {
    // Inputs
    signupController.execute("Alice", PASSWORD, PASSWORD);

    // Action
    logoutController.execute();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
    assertEquals(ViewNames.LOGIN, viewManager.getState());
    assertEquals("", loggedIn.getState());
    assertEquals(LoginState.EMPTY, loginViewModel.getState());
    assertEquals(SignupState.EMPTY, signupViewModel.getState());
  }

  @Test
  void logIn_afterLogout_showsDashboardForThatUser() {
    // Inputs
    signupController.execute("Alice", PASSWORD, PASSWORD);
    logoutController.execute();

    // Action
    loginController.execute("alice", PASSWORD);

    // Assertion
    assertEquals(ViewNames.DASHBOARD, viewManager.getState());
    assertEquals("Alice", loggedIn.getState());
    assertEquals("id-1", session.currentUser().orElseThrow().id());
  }

  @Test
  void logIn_wrongPasswordOrUnknownUser_showsSameMessageAndStaysOnLogin() {
    // Inputs
    signupController.execute("Alice", PASSWORD, PASSWORD);
    logoutController.execute();

    // Action
    loginController.execute("alice", "wrong-password");
    String wrongPasswordError = loginViewModel.getState().error();
    loginController.execute("nobody", PASSWORD);

    // Assertion
    assertEquals(LoginInteractor.INCORRECT_CREDENTIALS, wrongPasswordError);
    assertEquals(wrongPasswordError, loginViewModel.getState().error());
    assertTrue(session.currentUser().isEmpty());
    assertEquals(ViewNames.LOGIN, viewManager.getState());
  }
}
