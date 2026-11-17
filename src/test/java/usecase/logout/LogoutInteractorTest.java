package usecase.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dataaccess.InMemorySessionHolder;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogoutInteractorTest {

  private InMemorySessionHolder session;
  private FakeLogoutPresenter presenter;
  private LogoutInteractor interactor;

  @BeforeEach
  void setUp() {
    session = new InMemorySessionHolder();
    presenter = new FakeLogoutPresenter();
    interactor = new LogoutInteractor(session, presenter);
  }

  @Test
  void execute_userLoggedIn_endsSessionAndPresentsSuccess() {
    // Inputs
    session.start(new User("user-1", "alice", "hash"));

    // Action
    interactor.execute();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
    assertEquals(1, presenter.successCalls);
  }

  @Test
  void execute_nobodyLoggedIn_stillPresentsSuccess() {
    // Action
    interactor.execute();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
    assertEquals(1, presenter.successCalls);
  }

  @Test
  void execute_twice_isHarmless() {
    // Inputs
    session.start(new User("user-1", "alice", "hash"));

    // Action
    interactor.execute();
    interactor.execute();

    // Assertion
    assertTrue(session.currentUser().isEmpty());
    assertEquals(2, presenter.successCalls);
  }

  private static class FakeLogoutPresenter implements LogoutOutputBoundary {
    private int successCalls;

    @Override
    public void prepareSuccessView() {
      successCalls++;
    }
  }
}
