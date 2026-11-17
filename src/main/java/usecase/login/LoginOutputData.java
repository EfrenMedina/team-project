package usecase.login;

/**
 * The result of a successful login, passed to the presenter.
 *
 * @param username the logged-in user's username, spelled the way it was saved at sign-up
 *     (which may differ in case from what they typed)
 */
public record LoginOutputData(String username) {}
