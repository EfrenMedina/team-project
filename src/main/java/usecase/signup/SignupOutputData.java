package usecase.signup;

import entity.Money;

/**
 * The result of a successful sign-up, passed to the presenter.
 *
 * @param username the username of the new user
 * @param startingCash the cash the new account starts with
 */
public record SignupOutputData(String username, Money startingCash) {}
