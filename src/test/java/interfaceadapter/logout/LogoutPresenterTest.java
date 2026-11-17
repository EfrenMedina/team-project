package interfaceadapter.logout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import interfaceadapter.ClearedOnLogout;
import interfaceadapter.login.LoginState;
import interfaceadapter.login.LoginViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogoutPresenterTest {

  @Test
  void prepareSuccessView_clearsEveryViewModel() {
    // Inputs
    LoginViewModel loginViewModel = new LoginViewModel();
    loginViewModel.setState(new LoginState("alice", null, "alice"));
    RecordingViewModel otherScreen = new RecordingViewModel();
    LogoutPresenter presenter = new LogoutPresenter(List.of(loginViewModel, otherScreen));

    // Action
    presenter.prepareSuccessView();

    // Assertion
    assertEquals(LoginState.EMPTY, loginViewModel.getState());
    assertEquals(1, otherScreen.clears);
  }

  @Test
  void constructor_listChangedAfterwards_keepsOriginalList() {
    // Inputs
    RecordingViewModel added = new RecordingViewModel();
    List<ClearedOnLogout> viewModels = new ArrayList<>();
    LogoutPresenter presenter = new LogoutPresenter(viewModels);
    viewModels.add(added);

    // Action
    presenter.prepareSuccessView();

    // Assertion
    assertEquals(0, added.clears);
  }

  @Test
  void constructor_nullItem_throws() {
    // Inputs
    List<ClearedOnLogout> withNull = Collections.singletonList(null);

    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new LogoutPresenter(withNull));
  }

  private static class RecordingViewModel implements ClearedOnLogout {
    private int clears;

    @Override
    public void clearUserData() {
      clears++;
    }
  }
}
