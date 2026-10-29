package usecase;

/**
 * Turns passwords into hashes that are safe to store, and checks a
 * password against a stored hash. The sign-up use case hashes new
 * passwords and the login use case checks them.
 *
 * <p>A hash can't be turned back into the password. Hashing the same
 * password twice gives different results, because each hash has its
 * own random salt, so hashes must be compared with {@link #matches},
 * never with {@code equals}.
 *
 * <p>This interface belongs to the use case layer; the algorithm that
 * implements it lives in the data access layer, so use cases never
 * depend on a specific hashing library.
 */
public interface PasswordHasher {

  /**
   * Hashes a password for storage. The result contains everything needed to check the
   * password later (such as the salt), so it can be stored as a single value.
   *
   * @param rawPassword the password the user typed
   * @return the hash to store; never equal to or containing {@code rawPassword}
   * @throws NullPointerException if {@code rawPassword} is null
   */
  String hash(String rawPassword);

  /**
   * Checks whether a password is the one that produced a stored hash.
   *
   * @param rawPassword the password the user typed
   * @param storedHash a value previously returned by {@link #hash}
   * @return true if {@code rawPassword} is the password that was hashed
   * @throws NullPointerException if either argument is null
   * @throws IllegalArgumentException if {@code storedHash} was not produced by this hasher
   */
  boolean matches(String rawPassword, String storedHash);
}
