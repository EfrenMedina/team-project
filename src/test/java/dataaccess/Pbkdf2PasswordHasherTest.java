package dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Pbkdf2PasswordHasherTest {

  private static final String PASSWORD = "hunter222";

  // Few iterations so the tests run fast; the format and checks are the same as the default.
  private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1_000);

  @Test
  void hash_anyPassword_isNotAndDoesNotContainThePlainPassword() {
    // Inputs
    String password = PASSWORD;

    // Action
    String stored = hasher.hash(password);

    // Assertion
    assertNotEquals(password, stored);
    assertFalse(stored.contains(password));
  }

  @Test
  void hash_anyPassword_hasPrefixIterationsSaltAndHash() {
    // Inputs
    String password = PASSWORD;

    // Action
    String[] parts = hasher.hash(password).split("\\$");

    // Assertion
    assertEquals(4, parts.length);
    assertEquals("pbkdf2", parts[0]);
    assertEquals("1000", parts[1]);
    assertFalse(parts[2].isEmpty());
    assertFalse(parts[3].isEmpty());
  }

  @Test
  void hash_samePasswordTwice_givesDifferentHashes() {
    // Inputs
    String password = PASSWORD;

    // Action
    String first = hasher.hash(password);
    String second = hasher.hash(password);

    // Assertion
    assertNotEquals(first, second);
  }

  @Test
  void hash_defaultConstructor_uses600000Iterations() {
    // Inputs
    Pbkdf2PasswordHasher defaultHasher = new Pbkdf2PasswordHasher();

    // Action
    String stored = defaultHasher.hash(PASSWORD);

    // Assertion
    assertTrue(stored.startsWith("pbkdf2$600000$"));
  }

  @Test
  void matches_rightPassword_returnsTrue() {
    // Inputs
    String stored = hasher.hash(PASSWORD);

    // Action / Assertion
    assertTrue(hasher.matches(PASSWORD, stored));
  }

  @Test
  void matches_wrongPassword_returnsFalse() {
    // Inputs
    String stored = hasher.hash(PASSWORD);

    // Action / Assertion
    assertFalse(hasher.matches("hunter223", stored));
    assertFalse(hasher.matches("Hunter222", stored));
  }

  @Test
  void matches_hashMadeWithOtherIterationCount_usesStoredCount() {
    // Inputs
    String stored = new Pbkdf2PasswordHasher(2_000).hash(PASSWORD);

    // Action / Assertion
    assertTrue(hasher.matches(PASSWORD, stored));
  }

  @ParameterizedTest
  @ValueSource(strings = {"hunter222", "md5$1$AA==$AA==", "pbkdf2$1000$AA==",
      "pbkdf2$many$AA==$AA==", "pbkdf2$1000$not*base64$AA=="})
  void matches_storedValueNotInFormat_throws(String stored) {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> hasher.matches(PASSWORD, stored));
  }

  @Test
  void hash_nullPassword_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class, () -> hasher.hash(null));
  }

  @Test
  void matches_nullArgument_throws() {
    // Inputs
    String stored = hasher.hash(PASSWORD);

    // Action / Assertion
    assertThrows(NullPointerException.class, () -> hasher.matches(null, stored));
    assertThrows(NullPointerException.class, () -> hasher.matches(PASSWORD, null));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void constructor_iterationsBelowOne_throws(int iterations) {
    // Action / Assertion
    assertThrows(IllegalArgumentException.class, () -> new Pbkdf2PasswordHasher(iterations));
  }
}
