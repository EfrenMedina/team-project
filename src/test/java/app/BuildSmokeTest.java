package app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Confirms JUnit 5 runs and the build targets Java 17 or newer. */
class BuildSmokeTest {

  @Test
  void runsOnJava17OrNewer() {
    assertTrue(Runtime.version().feature() >= 17);
  }
}
