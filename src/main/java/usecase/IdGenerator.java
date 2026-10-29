package usecase;

/**
 * Creates unique ids for new things such as users, accounts and journal entries.
 *
 * <p>Use cases ask this interface for ids instead of creating them directly, so tests can
 * use a generator that returns predictable ids ("user-1", "account-1", ...) and check them
 * exactly. The app uses a generator that returns random UUIDs.
 */
public interface IdGenerator {

  /**
   * Returns a new id that has not been returned before by this generator.
   *
   * @return a new, non-blank id
   */
  String nextId();
}
