package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.InMemorySessionHolder;
import dataaccess.InMemoryUserDataAccessObject;
import dataaccess.Pbkdf2PasswordHasher;
import entity.IdGenerator;
import interfaceadapter.ClearedOnLogout;
import interfaceadapter.login.LoginController;
import interfaceadapter.login.LoginPresenter;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import interfaceadapter.logout.LogoutController;
import interfaceadapter.logout.LogoutPresenter;
import interfaceadapter.signup.SignupController;
import interfaceadapter.signup.SignupPresenter;
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
 * Signs a user up, then logs in and out through the real controllers, interactors, presenters
 * and in-memory storage, the way the app will wire them. Only the screens are missing.
 */
class LoginLogoutFlowTest {

  private static final String PASSWORD = "hunter222";

  private InMemorySessionHolder session;
  private LoginViewModel loginViewModel;
  private LoginController loginController;
  private LogoutController logoutController;

  @BeforeEach
  void setUp() {
    InMemoryUserDataAccessObject users = new InMemoryUserDataAccessObject();
    PasswordHasher hasher = new Pbkdf2PasswordHasher(1);
    session = new InMemorySessionHolder();
    loginViewModel = new LoginViewModel();

    loginController =
        new LoginController(
            new LoginInteractor(
                users, new LoginPresenter(loginViewModel), hasher, session, AfterLoginStep.NONE));
    List<ClearedOnLogout> screens = List.of(loginViewModel);
    logoutController =
        new LogoutController(new LogoutInteractor(session, new LogoutPresenter(screens)));

    AtomicInteger lastId = new AtomicInteger();
    IdGenerator ids = () -> "id-" + lastId.incrementAndGet();
    Clock clock = Clock.fixed(Instant.parse("2026-11-17T14:30:00Z"), ZoneOffset.UTC);
    SignupPresenter signupPresenter = new SignupPresenter(new SignupViewModel());
    new SignupController(new SignupInteractor(users, signupPresenter, hasher, ids, clock))
        .execute("Alice", PASSWORD, PASSWORD);
  }

  @Test
  void logIn_correctCredentials_marksUserLoggedIn() {
    // Action
    loginController.execute("alice", PASSWORD);

    // Assertion
    assertEquals(new LoginState("Alice", null, "Alice"), loginViewModel.getState());
    assertEquals("id-1", session.currentUser().orElseThrow().id());
  }

  @Test
  void logIn_wrongPasswordOrUnknownUser_showsSameMessage() {
    // Action
    loginController.execute("alice", "wrong-password");
    String wrongPasswordError = loginViewModel.getState().error();
    loginController.execute("nobody", PASSWORD);

    // Assertion
    assertEquals(LoginInteractor.INCORRECT_CREDENTIALS, wrongPasswordError);
    assertEquals(wrongPasswordError, loginViewModel.getState().error());
    assertTrue(session.currentUser().isEmpty());
  }

  @Test
  void logOut_afterLogin_endsSessionAndClearsScreens() {
    // Inputs
    loginController.execute("alice", PASSWORD);

    // Action
    logoutController.execute();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
    assertEquals(LoginState.EMPTY, loginViewModel.getState());
  }
}
