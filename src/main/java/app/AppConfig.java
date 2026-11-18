package app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Start-up settings for the app.
 *
 * <p>{@code data.mode} picks the data sources {@link AppBuilder} wires in:
 *
 * <ul>
 *   <li>{@code fake} (default): fake stock gateway and in-memory DAOs. Needs no API keys and keeps
 *       nothing between runs.
 *   <li>{@code real}: the Finnhub / Twelve Data gateways and the SQLite DAOs.
 * </ul>
 *
 * <p>Set it in {@code config.properties} ({@code data.mode=real}) or on the command line ({@code
 * -Ddata.mode=real}). The command line wins.
 *
 * @param dataMode which data sources to use
 */
public record AppConfig(DataMode dataMode) {

  /** The setting's key, in both {@code config.properties} and system properties. */
  public static final String DATA_MODE_KEY = "data.mode";

  private static final Path CONFIG_FILE = Path.of("config.properties");

  /** Which data sources the app uses. */
  public enum DataMode {
    FAKE,
    REAL
  }

  /**
   * Reads {@code config.properties} from the working directory, if present, then applies any
   * {@code -Ddata.mode} override.
   *
   * @return the settings to start the app with
   */
  public static AppConfig load() {
    Properties file = new Properties();
    if (Files.exists(CONFIG_FILE)) {
      try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
        file.load(in);
      } catch (IOException e) {
        throw new UncheckedIOException("Could not read " + CONFIG_FILE.toAbsolutePath(), e);
      }
    }
    return from(file, System.getProperty(DATA_MODE_KEY));
  }

  /**
   * Builds settings from the config file's properties and an optional override.
   *
   * @param file properties read from {@code config.properties} (may be empty)
   * @param override the {@code -Ddata.mode} value, or null if not given
   * @return the settings
   * @throws IllegalArgumentException if the mode is not {@code fake} or {@code real}
   */
  static AppConfig from(Properties file, String override) {
    String value = override != null ? override : file.getProperty(DATA_MODE_KEY, "fake");
    try {
      return new AppConfig(DataMode.valueOf(value.trim().toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          DATA_MODE_KEY + " must be 'fake' or 'real', but was '" + value + "'", e);
    }
  }
}
