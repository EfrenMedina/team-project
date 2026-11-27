package usecase.signup;

import entity.Account;
import entity.JournalEntry;
import entity.User;

/**
 * The storage the sign-up use case needs: checking whether a username is free and saving a
 * new user with their account and opening deposit. Implemented in the data access layer, in
 * memory for now and with SQLite later.
 *
 * <p>Usernames are compared ignoring case, so "Alice" and "alice" can't both exist.
 */
public interface SignupUserDataAccessInterface {

  /**
   * Returns whether a user with this username already exists, ignoring case.
   *
   * @param username the username to look for
   * @return true if the username is taken
   */
  boolean existsByUsername(String username);

  /**
   * Saves a new user together with their account and opening deposit. All three are saved,
   * or none is: in SQLite this is one transaction, so a user can never exist without the
   * cash they start with. The caller must check that the username is free first.
   *
   * @param user the user to save
   * @param account the user's trading account
   * @param openingDeposit the journal entry that puts the starting cash in the account
   * @throws IllegalStateException if a user with the same username (ignoring case) already
   *     exists; nothing is saved
   */
  void saveNewUser(User user, Account account, JournalEntry openingDeposit);
}
