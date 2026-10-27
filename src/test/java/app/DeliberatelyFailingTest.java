package app;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/** Throwaway: proves CI turns red on a failing test (SCRUM-14). Never merge. */
class DeliberatelyFailingTest {

  @Test
  void alwaysFails() {
    fail("CI should go red");
  }
}
