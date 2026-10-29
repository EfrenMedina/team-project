package entity;

/**
 * One of the five accounts in the ledger. Each account knows its normal side: the side
 * that makes its balance grow (see docs/ledger.md).
 */
public enum LedgerAccount {
  CASH(EntrySide.DEBIT),
  INVESTMENTS_AT_COST(EntrySide.DEBIT),
  COMMISSION_EXPENSE(EntrySide.DEBIT),
  CONTRIBUTED_CAPITAL(EntrySide.CREDIT),
  REALIZED_GAINS(EntrySide.CREDIT);

  private final EntrySide normalSide;

  LedgerAccount(EntrySide normalSide) {
    this.normalSide = normalSide;
  }

  /**
   * Returns the side (debit or credit) that increases this account's balance.
   */
  public EntrySide getNormalSide() {
    return this.normalSide;
  }
}
