package interfaceadapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.login.LoginOutputData;

class LoginPresenterTest {

  private LoginViewModel viewModel;
  private List<PropertyChangeEvent> events;
  private LoginPresenter presenter;

  @BeforeEach
  void setUp() {
    viewModel = new LoginViewModel();
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
    presenter = new LoginPresenter(viewModel);
  }

  @Test
  void prepareSuccessView_setsLoggedInUserAndClearsError() {
    // Inputs
    viewModel.setState(new LoginState("alice", "Username or password is incorrect.", null));
    events.clear();

    // Action
    presenter.prepareSuccessView(new LoginOutputData("Alice"));

    // Assertion
    assertEquals(new LoginState("Alice", null, "Alice"), viewModel.getState());
    assertEquals(1, events.size());
    assertEquals(LoginViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
  }

  @Test
  void prepareFailView_showsErrorAndKeepsTypedUsername() {
    // Inputs
    viewModel.setState(new LoginState("alice", null, null));
    events.clear();

    // Action
    presenter.prepareFailView("Username or password is incorrect.");

    // Assertion
    assertEquals(
        new LoginState("alice", "Username or password is incorrect.", null),
        viewModel.getState());
    assertEquals(1, events.size());
  }

  @Test
  void clearUserData_afterLogin_returnsToEmptyForm() {
    // Inputs
    presenter.prepareSuccessView(new LoginOutputData("Alice"));

    // Action
    viewModel.clearUserData();

    // Assertion
    assertEquals(LoginState.EMPTY, viewModel.getState());
    assertEquals(LoginState.EMPTY, events.get(events.size() - 1).getNewValue());
  }

  @Test
  void newViewModel_startsWithEmptyForm() {
    // Action / Assertion
    assertEquals(LoginState.EMPTY, new LoginViewModel().getState());
  }
}
