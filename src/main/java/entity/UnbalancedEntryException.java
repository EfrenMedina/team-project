package entity;

/**
 * Thrown when a journal entry has fewer than two lines or its debits don't equal its credits.
 * Unchecked, because an unbalanced entry is a programming bug, not something to recover from.
 */
public class UnbalancedEntryException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param message what was wrong with the entry
   */
  public UnbalancedEntryException(String message) {
    super(message);
  }
}
