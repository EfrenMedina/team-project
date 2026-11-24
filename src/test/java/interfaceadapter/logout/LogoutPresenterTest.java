package interfaceadapter.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import interfaceadapter.ClearedOnLogout;
import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogoutPresenterTest {

  private ViewManagerModel viewManager;

  @BeforeEach
  void setUp() {
    viewManager = new ViewManagerModel();
    viewManager.setState(ViewNames.DASHBOARD);
  }

  @Test
  void prepareSuccessView_clearsEveryViewModel() {
    // Inputs
    LoginViewModel loginViewModel = new LoginViewModel();
    loginViewModel.setState(new LoginState("alice", null, "alice"));
    LoggedInViewModel loggedIn = new LoggedInViewModel();
    loggedIn.setState("alice");
    RecordingViewModel otherScreen = new RecordingViewModel();
    LogoutPresenter presenter =
        new LogoutPresenter(List.of(loginViewModel, loggedIn, otherScreen), viewManager);

    // Action
    presenter.prepareSuccessView();

    // Assertion
    assertEquals(LoginState.EMPTY, loginViewModel.getState());
    assertEquals("", loggedIn.getState());
    assertEquals(1, otherScreen.clears);
  }

  @Test
  void prepareSuccessView_any_showsLoginScreen() {
    // Inputs
    List<String> shown = new ArrayList<>();
    viewManager.addPropertyChangeListener(event -> shown.add((String) event.getNewValue()));
    LogoutPresenter presenter = new LogoutPresenter(List.of(), viewManager);

    // Action
    presenter.prepareSuccessView();

    // Assertion
    assertEquals(ViewNames.LOGIN, viewManager.getState());
    assertEquals(List.of(ViewNames.LOGIN), shown);
  }

  @Test
  void constructor_listChangedAfterwards_keepsOriginalList() {
    // Inputs
    RecordingViewModel added = new RecordingViewModel();
    List<ClearedOnLogout> viewModels = new ArrayList<>();
    LogoutPresenter presenter = new LogoutPresenter(viewModels, viewManager);
    viewModels.add(added);

    // Action
    presenter.prepareSuccessView();

    // Assertion
    assertEquals(0, added.clears);
  }

  @Test
  void constructor_nullArgument_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LogoutPresenter(null, viewManager));
    assertThrows(NullPointerException.class, () -> new LogoutPresenter(List.of(), null));
  }

  @Test
  void constructor_nullItem_throws() {
    // Inputs
    List<ClearedOnLogout> withNull = Collections.singletonList(null);

    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LogoutPresenter(withNull, viewManager));
  }

  @Test
  void loggedInViewModel_setNull_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LoggedInViewModel().setState(null));
  }

  private static class RecordingViewModel implements ClearedOnLogout {
    private int clears;

    @Override
    public void clearUserData() {
      clears++;
    }
  }
}
