package interfaceadapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.login.LoginInteractor;
import usecase.login.LoginOutputData;

class LoginPresenterTest {

  private LoginViewModel viewModel;
  private LoggedInViewModel loggedIn;
  private ViewManagerModel viewManager;
  private List<PropertyChangeEvent> events;
  private LoginPresenter presenter;

  @BeforeEach
  void setUp() {
    viewModel = new LoginViewModel();
    loggedIn = new LoggedInViewModel();
    viewManager = new ViewManagerModel();
    viewManager.setState(ViewNames.LOGIN);
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
    presenter = new LoginPresenter(viewModel, loggedIn, viewManager);
  }

  @Test
  void prepareSuccessView_afterFailedTry_showsUserAndClearsError() {
    // Inputs
    viewModel.setState(new LoginState("alice", LoginInteractor.INCORRECT_CREDENTIALS, null));
    events.clear();

    // Action
    presenter.prepareSuccessView(new LoginOutputData("Alice"));

    // Assertion
    assertEquals(new LoginState("Alice", null, "Alice"), viewModel.getState());
    assertEquals(1, events.size());
    assertEquals(LoginViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
  }

  @Test
  void prepareSuccessView_any_recordsUserAndShowsDashboard() {
    // Inputs
    List<PropertyChangeEvent> screenChanges = new ArrayList<>();
    viewManager.addPropertyChangeListener(screenChanges::add);

    // Action
    presenter.prepareSuccessView(new LoginOutputData("Alice"));

    // Assertion
    assertEquals("Alice", loggedIn.getState());
    assertEquals(ViewNames.DASHBOARD, viewManager.getState());
    assertEquals(1, screenChanges.size());
  }

  @Test
  void prepareFailView_incorrectCredentials_showsErrorAndStaysOnLogin() {
    // Inputs
    viewModel.setState(new LoginState("alice", null, null));
    events.clear();

    // Action
    presenter.prepareFailView(LoginInteractor.INCORRECT_CREDENTIALS);

    // Assertion
    assertEquals(new LoginState("alice", LoginInteractor.INCORRECT_CREDENTIALS, null),
        viewModel.getState());
    assertEquals(1, events.size());
    assertEquals("", loggedIn.getState());
    assertEquals(ViewNames.LOGIN, viewManager.getState());
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
  void newViewModel_startsWithEmptyFormOnLoginScreen() {
    // Inputs
    LoginViewModel fresh = new LoginViewModel();

    // Action / Assertion
    assertEquals(LoginState.EMPTY, fresh.getState());
    assertEquals(ViewNames.LOGIN, fresh.getViewName());
  }

  @Test
  void setState_null_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> viewModel.setState(null));
  }

  @Test
  void constructor_nullArgument_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new LoginPresenter(null, loggedIn, viewManager));
    assertThrows(NullPointerException.class,
        () -> new LoginPresenter(viewModel, null, viewManager));
    assertThrows(NullPointerException.class,
        () -> new LoginPresenter(viewModel, loggedIn, null));
  }
}
