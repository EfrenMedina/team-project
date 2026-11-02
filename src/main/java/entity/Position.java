package entity;

/**
 * A holding of a single ticker: how many units are held and what they cost.
 *
 * <p>The adjusted cost is the total cost basis of the whole position, not the cost per unit.
 * It is not yet updated automatically when the quantity changes.
 */
public class Position {
    private final String ticker;
    private Money costAdjusted;
    private long quantity;

    /**
     * Creates an empty position with a quantity of 0 and no cost set.
     *
     * @param ticker symbol of the security held
     */
    public Position(String ticker){
        this.ticker = ticker;
    }

    /**
     * Creates a position with the given quantity and no cost set.
     *
     * @param ticker symbol of the security held
     * @param quantity how many units are held
     */
    public Position(String ticker, long quantity){
        this.ticker = ticker;
        this.quantity = quantity;
    }

    /**
     * Sets the adjusted cost basis of the whole position.
     *
     * @param amount the total adjusted cost
     */
    public void setCostAdjusted(Money amount){
        this.costAdjusted = amount;
    }

    /**
     * Sets how many units are held. Does not change the adjusted cost.
     *
     * @param quantity the new quantity
     */
    // TODO: add cost base adjustment when quantity adjusts.
    public void setQuantity(long quantity){
        this.quantity = quantity;
    }

    /**
     * Returns how many units are held.
     *
     * @return the quantity, or 0 if it hasn't been set
     */
    public long getQuantity(){
        return quantity;
    }

    /**
     * Returns the symbol of the security held.
     *
     * @return the ticker
     */
    public String getTicker(){
        return ticker;
    }

    /**
     * Returns the adjusted cost basis of the whole position.
     *
     * @return the total adjusted cost, or null if it hasn't been set
     */
    public Money getCostAdjusted(){
        return costAdjusted;
    }

}
