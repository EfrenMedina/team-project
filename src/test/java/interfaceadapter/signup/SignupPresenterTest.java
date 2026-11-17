package interfaceadapter.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.Money;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.signup.SignupInteractor;
import usecase.signup.SignupOutputData;

class SignupPresenterTest {

  private SignupViewModel viewModel;
  private List<PropertyChangeEvent> events;
  private SignupPresenter presenter;

  @BeforeEach
  void setUp() {
    viewModel = new SignupViewModel();
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
    presenter = new SignupPresenter(viewModel);
  }

  @Test
  void prepareSuccessView_hundredThousand_showsUserAndFormattedCash() {
    // Inputs
    viewModel.setState(new SignupState("alice", SignupInteractor.USERNAME_TAKEN, null, null));
    events.clear();

    // Action
    presenter.prepareSuccessView(new SignupOutputData("alice", Money.of("100000.00")));

    // Assertion
    assertEquals(new SignupState("alice", null, "alice", "$100,000.00"), viewModel.getState());
    assertEquals(1, events.size());
    assertEquals(SignupViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
  }

  @Test
  void prepareFailView_usernameTaken_showsErrorAndKeepsTypedUsername() {
    // Inputs
    viewModel.setState(new SignupState("alice", null, null, null));
    events.clear();

    // Action
    presenter.prepareFailView(SignupInteractor.USERNAME_TAKEN);

    // Assertion
    assertEquals(new SignupState("alice", SignupInteractor.USERNAME_TAKEN, null, null),
        viewModel.getState());
    assertEquals(1, events.size());
  }

  @Test
  void newViewModel_startsWithEmptyForm() {
    // Action / Assertion
    assertEquals(SignupState.EMPTY, new SignupViewModel().getState());
  }

  @Test
  void setState_null_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> viewModel.setState(null));
  }

  @Test
  void constructor_nullViewModel_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> new SignupPresenter(null));
  }
}
