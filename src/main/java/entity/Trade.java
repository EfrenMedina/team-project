package entity;

import java.time.Instant;
import java.util.Objects;

/**
 * A trade record containing total
 * @param ticker Symbol of the security traded
 * @param quantity Number of shares to be traded
 * @param pricePerShare The price the shares were traded at
 * @param soldAt The time the trade was executed
 * @param buyerId The id of the account whose buy order was fulfilled
 * @param id Unique identifier of this trade
 */
public record Trade(String ticker, long quantity, Money pricePerShare, Instant soldAt, String buyerId, String id) {
    public Trade {
        Objects.requireNonNull(ticker, "ticker");
        Objects.requireNonNull(pricePerShare, "pricePerShare");
        Objects.requireNonNull(buyerId, "buyerId");
        Objects.requireNonNull(id, "id");

        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (pricePerShare.isZeroOrLess()){
            throw new IllegalArgumentException("Price per share must be positive: " + pricePerShare);
        }
    }

    public Money total() {
        return pricePerShare.multiply(Math.toIntExact(quantity));
    }

}
