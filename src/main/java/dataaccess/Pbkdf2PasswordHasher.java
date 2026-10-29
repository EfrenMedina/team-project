package dataaccess;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import usecase.PasswordHasher;

/**
 * Hashes passwords with PBKDF2 (HMAC-SHA256) and a random 16-byte salt per password.
 *
 * <p>A hash is stored as one string: {@code pbkdf2$<iterations>$<salt>$<hash>}, with the
 * salt and hash in Base64. Because the iteration count is stored with each hash, it can be
 * raised later without breaking passwords that were hashed with the old count.
 */
public final class Pbkdf2PasswordHasher implements PasswordHasher {

  /** The JCA name of PBKDF2 with HMAC-SHA256. */
  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

  /** The first part of every stored hash, so other formats are recognised and rejected. */
  private static final String PREFIX = "pbkdf2";

  /** The iteration count OWASP recommends for PBKDF2 with HMAC-SHA256. */
  private static final int DEFAULT_ITERATIONS = 600_000;

  /** The length of each random salt, in bytes. */
  private static final int SALT_BYTES = 16;

  /** The length of each hash, in bits. */
  private static final int KEY_BITS = 256;

  /** How many times the hash function is applied for new hashes. */
  private final int iterations;

  /** Source of the salts; unpredictable, unlike {@code java.util.Random}. */
  private final SecureRandom random = new SecureRandom();

  /**
   * Creates a hasher with 600,000 iterations, the count OWASP recommends for PBKDF2 with
   * HMAC-SHA256. Each hash takes a noticeable fraction of a second, which is the point: it
   * makes guessing passwords slow.
   */
  public Pbkdf2PasswordHasher() {
    this(DEFAULT_ITERATIONS);
  }

  /**
   * Creates a hasher with a chosen number of iterations. Tests use a small number so they
   * run fast; the app uses {@link #Pbkdf2PasswordHasher()}.
   *
   * @param iterations how many times the hash function is applied
   * @throws IllegalArgumentException if {@code iterations} is less than 1
   */
  public Pbkdf2PasswordHasher(int iterations) {
    if (iterations < 1) {
      throw new IllegalArgumentException("iterations must be at least 1, got " + iterations);
    }
    this.iterations = iterations;
  }

  /**
   * Hashes a password with a new random salt.
   *
   * @param rawPassword the password the user typed
   * @return the hash as {@code pbkdf2$<iterations>$<salt>$<hash>}
   * @throws NullPointerException if {@code rawPassword} is null
   */
  @Override
  public String hash(String rawPassword) {
    Objects.requireNonNull(rawPassword, "rawPassword");
    byte[] salt = new byte[SALT_BYTES];
    random.nextBytes(salt);
    byte[] hash = derive(rawPassword, salt, iterations);

    Base64.Encoder encoder = Base64.getEncoder();
    return String.join("$", PREFIX, String.valueOf(iterations),
        encoder.encodeToString(salt), encoder.encodeToString(hash));
  }

  /**
   * Hashes the password again with the salt and iteration count from the stored hash, and
   * compares the results in constant time.
   *
   * @param rawPassword the password the user typed
   * @param storedHash a value returned by {@link #hash}
   * @return true if {@code rawPassword} is the password that was hashed
   * @throws NullPointerException if either argument is null
   * @throws IllegalArgumentException if {@code storedHash} is not in this hasher's format
   */
  @Override
  public boolean matches(String rawPassword, String storedHash) {
    Objects.requireNonNull(rawPassword, "rawPassword");
    Objects.requireNonNull(storedHash, "storedHash");

    // "$" alone means "end of line" in a regex, so it must be escaped.
    String[] parts = storedHash.split("\\$");
    if (parts.length != 4 || !parts[0].equals(PREFIX)) {
      throw new IllegalArgumentException("Not a PBKDF2 hash");
    }
    // Both throw a subclass of IllegalArgumentException on bad input.
    int storedIterations = Integer.parseInt(parts[1]);
    Base64.Decoder decoder = Base64.getDecoder();
    byte[] salt = decoder.decode(parts[2]);
    byte[] expected = decoder.decode(parts[3]);

    // Use the stored count, not this.iterations, so old hashes still match after a change.
    byte[] actual = derive(rawPassword, salt, storedIterations);
    // Takes the same time however many bytes match, so timing reveals nothing.
    return MessageDigest.isEqual(expected, actual);
  }

  /**
   * Runs PBKDF2 on a password.
   *
   * @param rawPassword the password
   * @param salt the salt
   * @param iterations how many times the hash function is applied
   * @return the 256-bit hash
   */
  private static byte[] derive(String rawPassword, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_BITS);
    try {
      return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      // Every Java 17 runtime has PBKDF2, so this only happens if the JDK is broken.
      throw new IllegalStateException("PBKDF2 is not available", e);
    } finally {
      spec.clearPassword();
    }
  }
}
