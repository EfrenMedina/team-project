package entity;

/**
 * Thrown when trying to set an illegal value to Order, or to move an Order into a status
 * it can't reach from its current one.
 */
public class OrderException extends RuntimeException{

    /** Cases that could cause a failure with order **/
    public enum Reason {
        ALREADY_FILLED,
        ALREADY_CANCELED,
        ALREADY_REJECTED,
        ALREADY_OPEN,
        NOT_OPEN,
        MISSING_LIMIT_PRICE,
        INVALID_QUANTITY,
        MISSING_ORDER_TYPE,
        MISSING_SIDE,
        LIMIT_PRICE_NOT_ALLOWED,
        INVALID_LIMIT_PRICE
    }

    private final Reason reason;

    /**
     * Creates the exception.
     *
     * @param message what was wrong with the entry
     */
    public OrderException(Reason reason, String message){
        super(message);
        this.reason = reason;

    }

    public Reason getReason() {
        return reason;
    }
}
