package interfaceadapter.signup;

import entity.Money;
import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.ViewNames;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import usecase.signup.SignupInteractor;
import usecase.signup.SignupOutputBoundary;
import usecase.signup.SignupOutputData;

/**
 * Turns the sign-up use case's result into what the screens show: the error message under the
 * field it is about on failure, or the Dashboard with the new user's name in the sidebar on
 * success. The new user is already logged in by then.
 */
public class SignupPresenter implements SignupOutputBoundary {

  /** The Sign-up form's state, which this presenter updates. */
  private final SignupViewModel signupViewModel;

  /** Who is logged in, shown in the sidebar. */
  private final LoggedInViewModel loggedInViewModel;

  /** Which screen is showing. */
  private final ViewManagerModel viewManagerModel;

  /**
   * Creates the presenter.
   *
   * @param signupViewModel the Sign-up form's view model
   * @param loggedInViewModel holds who is logged in
   * @param viewManagerModel switches screens
   * @throws NullPointerException if any argument is null
   */
  public SignupPresenter(
      SignupViewModel signupViewModel,
      LoggedInViewModel loggedInViewModel,
      ViewManagerModel viewManagerModel) {
    this.signupViewModel = Objects.requireNonNull(signupViewModel, "signupViewModel");
    this.loggedInViewModel = Objects.requireNonNull(loggedInViewModel, "loggedInViewModel");
    this.viewManagerModel = Objects.requireNonNull(viewManagerModel, "viewManagerModel");
  }

  /**
   * Records the new user and their starting cash, clears any earlier error and shows the
   * Dashboard.
   *
   * @param outputData the new user's details
   */
  @Override
  public void prepareSuccessView(SignupOutputData outputData) {
    String username = outputData.username();
    signupViewModel.setState(
        new SignupState(username, null, null, username, format(outputData.startingCash())));
    loggedInViewModel.setState(username);
    viewManagerModel.setState(ViewNames.DASHBOARD);
    viewManagerModel.firePropertyChanged();
  }

  /**
   * Shows the error under the field it is about, and keeps the username the user typed so
   * they can correct it.
   *
   * @param error the message to show
   */
  @Override
  public void prepareFailView(String error) {
    SignupState current = signupViewModel.getState();
    signupViewModel.setState(
        new SignupState(current.username(), error, fieldFor(error), null, null));
  }

  /**
   * Returns the field an error message is about: password errors go under the password
   * boxes, and everything else (an invalid or taken username) under the username box.
   *
   * @param error a message from {@link SignupInteractor}
   * @return the field to show it under
   */
  private static SignupState.Field fieldFor(String error) {
    if (SignupInteractor.INVALID_PASSWORD_LENGTH.equals(error)) {
      return SignupState.Field.PASSWORD;
    }
    if (SignupInteractor.PASSWORDS_DONT_MATCH.equals(error)) {
      return SignupState.Field.REPEAT_PASSWORD;
    }
    return SignupState.Field.USERNAME;
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
