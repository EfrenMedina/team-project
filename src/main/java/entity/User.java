package entity;

import java.util.Objects;

/**
 * A person who can log in to the app. Each user owns one trading account,
 * which points back to the user through its {@code userId}.
 *
 * <p>The password is never stored. Only {@code passwordHash} is kept: the value produced by a
 * {@code PasswordHasher}, which can check a password but can't be turned back into one.
 *
 * <p>The username format (3 to 20 letters, digits or underscores) is checked by the sign-up
 * use case before a user is created, not here.
 *
 * @param id unique id of this user
 * @param username the name the user logs in with
 * @param passwordHash the hashed password, including whatever the hasher needs to check it
 */
public record User(String id, String username, String passwordHash) {

  /**
   * Creates a user.
   *
   * @throws NullPointerException if any component is null
   * @throws IllegalArgumentException if {@code username} or {@code passwordHash} is blank
   */
  public User {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(passwordHash, "passwordHash");

    if (username.isBlank()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    if (passwordHash.isBlank()) {
      throw new IllegalArgumentException("passwordHash must not be blank");
    }
  }

  /**
   * Returns a description of this user for logs and debugging. The password hash is replaced
   * with {@code ***} so it never ends up in log files or error messages.
   *
   * @return the id and username, with the hash hidden
   */
  @Override
  public String toString() {
    return "User[id=" + id + ", username=" + username + ", passwordHash=***]";
  }
}
