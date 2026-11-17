package interfaceadapter;

/**
 * A view model that holds data belonging to the logged-in user, such as their username, cash
 * or holdings. When the user logs out, the logout presenter calls {@link #clearUserData} on
 * every one of them, so the next person at the computer sees none of it.
 *
 * <p>Each feature's view model that shows user data should implement this, and the app should
 * pass it to the logout presenter when it's wired up.
 */
public interface ClearedOnLogout {

  /** Removes every piece of the logged-in user's data and tells the screen to redraw. */
  void clearUserData();
}
