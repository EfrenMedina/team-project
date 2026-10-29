package entity;

/**
 * A buy or sell order for a single ticker.
 *
 * <p>An order starts out {@link OrderStatus#PENDING}, and its details (side, amount, type,
 * limit price) can only be changed while it is still pending. Once the order has been sent,
 * the setters leave it unchanged.
 */
public class Order {
    private final String id;
    private final String ticker;
    private OrderType type;
    private OrderSide side;
    private long amount;
    private long limitPrice;
    private OrderStatus status;

    /**
     * Creates a new pending order.
     *
     * @param id unique identifier for this order
     * @param ticker symbol of the security being traded
     */
    public Order(String id, String ticker){
        this.id = id;
        this.ticker = ticker;
        this.status = OrderStatus.PENDING; // just created order

    }

    /**
     * Sets whether this order buys or sells.
     *
     * @param side the order side
     * @return true if the side was set, false if the order is no longer pending
     */
    public boolean setSide(OrderSide side){
        if (status != OrderStatus.PENDING){
            return false;
        }
        this.side = side;
        return true;
    }

    /**
     * Sets how many units this order is for.
     *
     * @param amount the quantity, must be greater than 0
     * @return true if the amount was set, false if the order is no longer pending
     * @throws OrderException if amount is 0 or less
     */
    public boolean setAmount(long amount) throws OrderException{
        if (status != OrderStatus.PENDING){
            return false;
        }
        if (amount <= 0){
            throw new OrderException(OrderException.Reason.INVALID_QUANTITY, "Order " + id +
                    " cannot have a quantity assigned to 0 or less.");
        }
        this.amount = amount;
        return true;
    }

    /**
     * Sets whether this is a market or limit order.
     *
     * @param type the order type
     * @return true if the type was set, false if the order is no longer pending
     */
    public boolean setOrderType(OrderType type){
        if (status != OrderStatus.PENDING){
            return false;
        }
        this.type = type;
        return true;
    }

    /**
     * Sets the price limit for a limit order. Does nothing if the order is no longer pending.
     *
     * @param limitPrice the limit price, must be greater than 0
     * @throws OrderException if the order type hasn't been set, the order is a MARKET order,
     *     or limitPrice is 0 or less
     */
    public void setLimitPrice(long limitPrice) throws OrderException{
        if (status != OrderStatus.PENDING){
            return; // already sent, leave the order unchanged
        }
        if (type == null){
            throw new OrderException(OrderException.Reason.MISSING_ORDER_TYPE, "Order " + id +
                    " is missing a order type.");
        }
        else if (type == OrderType.MARKET){
            throw new OrderException(OrderException.Reason.LIMIT_PRICE_NOT_ALLOWED, "Order " + id +
                    " with type MARKET cannot be assigned a limit price.");
        }
        else if (limitPrice <= 0){
            throw new OrderException(OrderException.Reason.INVALID_LIMIT_PRICE, "Order " + id +
                    " cannot have a limit price of 0 or less.");
        }
        else {
            this.limitPrice = limitPrice;
        }
    }

    /**
     * Accepts this order, moving it from PENDING to OPEN.
     *
     * @throws OrderException if the order isn't pending, or is missing its side, amount,
     *     type, or (for a LIMIT order) limit price
     */
    public void acceptOrder() throws OrderException{
        if (status != OrderStatus.PENDING){
            throw illegalTransition("accept");
        }
        if (side == null){
            throw new OrderException(OrderException.Reason.MISSING_SIDE, "Order " + id +
                    " is missing a side.");
        }
        if (amount <= 0){
            throw new OrderException(OrderException.Reason.INVALID_QUANTITY, "Order " + id +
                    " is missing an amount.");
        }
        if (type == null){
            throw new OrderException(OrderException.Reason.MISSING_ORDER_TYPE, "Order " + id +
                    " is missing a order type.");
        }
        if (type == OrderType.LIMIT && limitPrice <= 0){
            throw new OrderException(OrderException.Reason.MISSING_LIMIT_PRICE, "Order " + id +
                    " with type LIMIT is missing a limit price.");
        }
        this.status = OrderStatus.OPEN;
    }

    /**
     * Rejects this order, moving it from PENDING to REJECTED.
     *
     * @throws OrderException if the order isn't pending
     */
    public void rejectOrder() throws OrderException{
        if (status != OrderStatus.PENDING){
            throw illegalTransition("reject");
        }
        this.status = OrderStatus.REJECTED;
    }

    /**
     * Cancels this order. Both pending and open orders can be canceled.
     *
     * @throws OrderException if the order is already filled, canceled, or rejected
     */
    public void cancelOrder() throws OrderException{
        if (status != OrderStatus.PENDING && status != OrderStatus.OPEN){
            throw illegalTransition("cancel");
        }
        this.status = OrderStatus.CANCELED;
    }

    /**
     * Fills this order, moving it from OPEN to FILLED. Partial fills are currently undefined.
     *
     * @throws OrderException if the order isn't open
     */
    public void fillOrder() throws OrderException{
        if (status != OrderStatus.OPEN){
            throw illegalTransition("fill");
        }

        this.status = OrderStatus.FILLED;
    }

    /**
     * Builds the exception for an action that isn't allowed from the current status.
     *
     * @param action the attempted action, used in the message
     * @return the exception to throw
     */
    private OrderException illegalTransition(String action){
        OrderException.Reason reason = switch (status){
            case PENDING -> OrderException.Reason.NOT_OPEN;
            case OPEN -> OrderException.Reason.ALREADY_OPEN;
            case FILLED -> OrderException.Reason.ALREADY_FILLED;
            case CANCELED -> OrderException.Reason.ALREADY_CANCELED;
            case REJECTED -> OrderException.Reason.ALREADY_REJECTED;
        };
        return new OrderException(reason, "Cannot " + action + " order " + id +
                ": status is " + status + ".");
    }

    /**
     * Returns this order's identifier.
     *
     * @return the order id
     */
    public String getId(){
        return this.id;
    }

    /**
     * Returns the symbol of the security being traded.
     *
     * @return the ticker
     */
    public String getTicker(){
        return this.ticker;
    }

    /**
     * Returns whether this is a market or limit order.
     *
     * @return the order type, or null if it hasn't been set
     */
    public OrderType getType(){
        return this.type;
    }

    /**
     * Returns whether this order buys or sells.
     *
     * @return the order side, or null if it hasn't been set
     */
    public OrderSide getSide(){
        return this.side;
    }

    /**
     * Returns how many units this order is for.
     *
     * @return the amount, or 0 if it hasn't been set
     */
    public long getAmount(){
        return this.amount;
    }

    /**
     * Returns the price limit for a limit order.
     *
     * @return the limit price, or 0 if it hasn't been set
     */
    public long getLimitPrice(){
        return this.limitPrice;
    }

    /**
     * Returns where this order is in its lifecycle.
     *
     * @return the order status
     */
    public OrderStatus getStatus(){
        return this.status;
    }
}
