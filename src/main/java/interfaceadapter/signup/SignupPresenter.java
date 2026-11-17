package interfaceadapter.signup;

import entity.Money;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import usecase.signup.SignupOutputBoundary;
import usecase.signup.SignupOutputData;

/**
 * Turns the sign-up use case's result into what the Sign-up screen shows: the error message
 * on failure, or the new username and their formatted starting cash on success.
 */
public class SignupPresenter implements SignupOutputBoundary {

  /** The Sign-up screen's state, which this presenter updates. */
  private final SignupViewModel signupViewModel;

  /**
   * Creates the presenter.
   *
   * @param signupViewModel the Sign-up screen's view model
   * @throws NullPointerException if {@code signupViewModel} is null
   */
  public SignupPresenter(SignupViewModel signupViewModel) {
    this.signupViewModel = Objects.requireNonNull(signupViewModel, "signupViewModel");
  }

  /**
   * Shows the new user and their starting cash, and clears any earlier error.
   *
   * @param outputData the new user's details
   */
  @Override
  public void prepareSuccessView(SignupOutputData outputData) {
    String username = outputData.username();
    signupViewModel.setState(
        new SignupState(username, null, username, format(outputData.startingCash())));
  }

  /**
   * Shows the error and keeps the username the user typed, so they can correct it.
   *
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String error) {
    SignupState current = signupViewModel.getState();
    signupViewModel.setState(new SignupState(current.username(), error, null, null));
  }

  /**
   * Formats an amount as US dollars, for example {@code "$100,000.00"}.
   *
   * @param amount the amount
   * @return the amount with a dollar sign, thousands separators and two decimals
   */
  private static String format(Money amount) {
    return NumberFormat.getCurrencyInstance(Locale.US)
        .format(BigDecimal.valueOf(amount.toCents(), 2));
  }
}
