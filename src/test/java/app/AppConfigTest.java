package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.AppConfig.DataMode;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class AppConfigTest {

  @Test
  void from_nothingSet_usesFakeData() {
    assertEquals(DataMode.FAKE, AppConfig.from(new Properties(), null).dataMode());
  }

  @Test
  void from_fileSaysReal_usesRealData() {
    Properties file = new Properties();
    file.setProperty("data.mode", "real");

    assertEquals(DataMode.REAL, AppConfig.from(file, null).dataMode());
  }

  @Test
  void from_overrideGiven_overrideWinsOverFile() {
    Properties file = new Properties();
    file.setProperty("data.mode", "real");

    assertEquals(DataMode.FAKE, AppConfig.from(file, " FAKE ").dataMode());
  }

  @Test
  void from_unknownMode_throwsWithClearMessage() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> AppConfig.from(new Properties(), "sql"));

    assertEquals("data.mode must be 'fake' or 'real', but was 'sql'", e.getMessage());
  }
}
