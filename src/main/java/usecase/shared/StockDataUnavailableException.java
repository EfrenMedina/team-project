package usecase.shared;

/**
 * Thrown when stock data can't be fetched: the data service is down or out of requests, the
 * network failed, or the ticker isn't known.
 *
 * <p>Checked, because this is expected to happen while the app runs and every caller should
 * decide what to show instead (for example, saved data or a "data service offline" message).
 */
public class StockDataUnavailableException extends Exception {

  /**
   * Creates the exception.
   *
   * @param message what couldn't be fetched and why
   */
  public StockDataUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates the exception for a lower-level failure, such as a network error.
   *
   * @param message what couldn't be fetched and why
   * @param cause the failure that caused it
   */
  public StockDataUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
