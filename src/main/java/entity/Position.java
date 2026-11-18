package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * All the shares an account holds in one ticker, with what was paid for them in total.
 *
 * <p>An account has at most one position per ticker. Buying more of the same ticker adds to
 * the existing position, so the average cost per share blends every purchase. For example,
 * buying 10 shares at $100 and 10 at $120 gives 20 shares that cost $2,200, or $110 each.
 *
 * <p>Selling removes shares at the average cost: the total cost goes down in proportion, so
 * the average cost per share of the shares left does not change.
 */
public class Position {
    private final String ticker;
    private long quantity;
    private Money totalCost;

    /**
     * Creates a position from a first purchase.
     *
     * @param ticker symbol of the security bought
     * @param quantity how many shares were bought, must be greater than 0
     * @param cost total amount paid for those shares, must be greater than 0
     * @throws IllegalArgumentException if quantity or cost is 0 or less
     */
    public Position(String ticker, long quantity, Money cost){
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        Objects.requireNonNull(cost, "cost");
        validatePurchase(quantity, cost);
        this.quantity = quantity;
        this.totalCost = cost;
    }

    /**
     * Adds bought shares to this position.
     *
     * @param amount how many shares were bought, must be greater than 0
     * @param cost total amount paid for those shares, must be greater than 0
     * @throws IllegalArgumentException if amount or cost is 0 or less
     */
    public void addShares(long amount, Money cost){
        Objects.requireNonNull(cost, "cost");
        validatePurchase(amount, cost);
        this.quantity += amount;
        this.totalCost = totalCost.add(cost);
    }

    /**
     * Removes sold shares from this position. The total cost goes down in proportion, so the
     * average cost per share stays the same (up to rounding to the cent).
     *
     * @param amount how many shares were sold, must be greater than 0
     * @throws IllegalArgumentException if amount is 0 or less, or more than the shares held
     */
    public void removeShares(long amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Amount sold must be greater than 0.");
        }
        if (amount > quantity){
            throw new IllegalArgumentException("Cannot sell " + amount + " shares from a position of "
                    + quantity + ".");
        }
        long remaining = quantity - amount;
        this.totalCost = Money.ofCents(scaleCents(totalCost.toCents(), remaining, quantity));
        this.quantity = remaining;
    }

    /**
     * Returns whether every share in this position has been sold.
     *
     * @return true if the quantity is 0
     */
    public boolean isEmpty(){
        return quantity == 0;
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
     * Returns how many shares are held.
     *
     * @return the quantity
     */
    public long getQuantity(){
        return quantity;
    }

    /**
     * Returns the total amount paid for the shares still held.
     *
     * @return the total cost
     */
    public Money getTotalCost(){
        return totalCost;
    }

    /**
     * Returns the average amount paid per share, rounded to the cent.
     *
     * @return the average cost per share, or zero if no shares are held
     */
    public Money getAverageCost(){
        if (quantity == 0){
            return Money.ZERO;
        }
        return Money.ofCents(scaleCents(totalCost.toCents(), 1, quantity));
    }

    private static void validatePurchase(long amount, Money cost){
        if (amount <= 0){
            throw new IllegalArgumentException("Amount bought must be greater than 0.");
        }
        if (cost.isZeroOrLess()){
            throw new IllegalArgumentException("Cost must be greater than 0.");
        }
    }

    /** Returns cents * numerator / denominator, rounded to a whole cent. */
    private static long scaleCents(long cents, long numerator, long denominator){
        return BigDecimal.valueOf(cents)
                .multiply(BigDecimal.valueOf(numerator))
                .divide(BigDecimal.valueOf(denominator), 0, RoundingMode.HALF_EVEN)
                .longValueExact();
    }
}
