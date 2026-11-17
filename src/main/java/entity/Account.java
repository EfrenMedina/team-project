package entity;

/**
 * A user's trading account. It has no cash field: cash is calculated from the ledger.
 *
 * <p>Placeholder written for SCRUM-26 so sign-up compiles. SCRUM-20 owns this class: its
 * version (PR #14) has the same constructor and getters plus positions, and replaces this
 * one when it is merged.
 */
public class Account {

  private final String id;
  private final String userId;

  /**
   * Creates an account with no holdings.
   *
   * @param id unique identifier for this account
   * @param userId identifier of the user who owns this account
   */
  public Account(String id, String userId) {
    this.id = id;
    this.userId = userId;
  }

  /**
   * Returns this account's identifier.
   *
   * @return the account id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the identifier of the user who owns this account.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }
}
