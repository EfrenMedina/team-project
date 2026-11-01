package entity;

/**
 * Thrown when an illegal trade operation is attempted.
 */
public class TradeException extends RuntimeException {

    /** Cases that could cause a failure with trade **/
    public enum Reason {
        SELL_LESS_THAN_HELD,                    // A class should handle splitting large buy order into smaller ones.
                                                // This simply handles race condition.
        BUYER_ID_DOESNT_MATCH,
        SELLER_ID_DOESNT_MATCH,
        SELLER_DOES_NOT_HOLD_TICKER,
        LOTID_INCONSISTENCY

    }

    private final Reason reason;

    public TradeException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {return reason;}
}
