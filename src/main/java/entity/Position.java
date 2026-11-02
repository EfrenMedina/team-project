package entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A single lot: one purchase of a ticker, at one price per share, at one time.
 *
 * <p>Buying the same ticker twice creates two separate lots. A lot's ticker, cost per share
 * and acquisition time never change. Its quantity only goes down, when shares from this lot
 * are sold.
 *
 * <p>The adjusted cost is the total cost basis of the shares in this lot. It is set from
 * outside and is not yet updated automatically when the quantity changes.
 *
 * <p>Invariant: a lot's id is unique across all lots in the system, in every account. A lot
 * can't check this itself, so whoever creates it must supply an id that hasn't been used.
 */
public class Position {
    private final String id;
    private final String ticker;
    private final Money costPerShare;
    private final Instant acquiredAt;
    private Money costAdjusted;
    private long quantity;

    /**
     * Creates a lot for a purchase.
     *
     * @param id identifier for this lot, must not be used by any other lot
     * @param ticker symbol of the security bought
     * @param quantity how many shares were bought, must be greater than 0
     * @param costPerShare price paid per share, must be greater than 0
     * @param acquiredAt when the shares were bought
     * @throws IllegalArgumentException if quantity or costPerShare is 0 or less
     */
    public Position(String id, String ticker, long quantity, Money costPerShare,
            Instant acquiredAt){
        this.id = Objects.requireNonNull(id, "id");
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        this.costPerShare = Objects.requireNonNull(costPerShare, "costPerShare");
        this.acquiredAt = Objects.requireNonNull(acquiredAt, "acquiredAt");
        if (quantity <= 0){
            throw new IllegalArgumentException("A lot must have a quantity greater than 0.");
        }
        if (costPerShare.isZeroOrLess()){
            throw new IllegalArgumentException("A lot must have a cost per share greater than 0.");
        }
        this.quantity = quantity;
    }

    /**
     * Sets the adjusted cost basis of the shares in this lot.
     *
     * @param amount the total adjusted cost
     */
    public void setCostAdjusted(Money amount){
        this.costAdjusted = amount;
    }

    /**
     * Removes sold shares from this lot. Does not change the adjusted cost.
     *
     * @param amount how many shares were sold, must be greater than 0
     * @throws IllegalArgumentException if amount is 0 or less, or more than the shares left
     */
    // TODO: add cost base adjustment when quantity adjusts.
    public void reduceQuantity(long amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Amount sold must be greater than 0.");
        }
        if (amount > quantity){
            throw new IllegalArgumentException("Cannot sell " + amount + " shares from a lot of "
                    + quantity + ".");
        }
        this.quantity -= amount;
    }

    /**
     * Returns whether every share in this lot has been sold.
     *
     * @return true if the quantity is 0
     */
    public boolean isClosed(){
        return quantity == 0;
    }

    /**
     * Returns this lot's identifier.
     *
     * @return the lot id
     */
    public String getId(){
        return id;
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
     * Returns how many shares are left in this lot.
     *
     * @return the remaining quantity
     */
    public long getQuantity(){
        return quantity;
    }

    /**
     * Returns the price paid per share.
     *
     * @return the cost per share
     */
    public Money getCostPerShare(){
        return costPerShare;
    }

    /**
     * Returns when the shares in this lot were bought.
     *
     * @return the acquisition time
     */
    public Instant getAcquiredAt(){
        return acquiredAt;
    }

    /**
     * Returns the adjusted cost basis of the shares in this lot.
     *
     * @return the total adjusted cost, or null if it hasn't been set
     */
    public Money getCostAdjusted(){
        return costAdjusted;
    }

}
