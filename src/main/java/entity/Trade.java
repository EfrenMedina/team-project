package entity;

import java.time.Instant;
import java.util.Objects;

/**
 * A trade record containing profit
 * @param lotId Id of the position to be traded
 * @param quantity Number of shares to be traded
 * @param pricePerShare The price the shares were traded at
 * @param soldAt The time the trade was executed
 * @param buyerId The id of the account whose buy order was fulfilled
 * @param sellerId The id of the account whose sell order was fulfilled
 */
public record Trade(String lotId, String ticker, long quantity, Money pricePerShare, Instant soldAt, String buyerId, String sellerId) {
    public Trade {
        Objects.requireNonNull(lotId, "lotId");
        Objects.requireNonNull(ticker, "ticker");
        Objects.requireNonNull(pricePerShare, "pricePerShare");
        Objects.requireNonNull(buyerId, "buyerId");
        Objects.requireNonNull(sellerId, "sellerId");

        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (pricePerShare.isZeroOrLess()){
            throw new IllegalArgumentException("Price per share must be positive: " + pricePerShare);
        }
        if (buyerId.equals(sellerId)){
            throw new IllegalArgumentException("buyerId and sellerId cannot be the same");
        }

        // TODO: check that lotId is valid
    }

    public Money profit() {
        return pricePerShare.multiply(Math.toIntExact(quantity));
    }

}
