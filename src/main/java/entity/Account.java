package entity;

import java.util.*;


/**
 * A user's brokerage account and the positions it holds.
 *
 * <p>The account holds at most one {@link Position} per ticker. Buying a ticker the account
 * already holds adds the shares and their cost to that position, so its average cost blends
 * every purchase. Selling every share of a ticker removes its position.
 */
public class Account {
    private final String id;
    private final String userId;
    private final Map<String, Position> positions; // ticker -> position

    /**
     * Creates an account with no holdings.
     *
     * @param id unique identifier for this account
     * @param userId identifier of the user who owns this account
     */
    public Account(String id, String userId){
        this.id = id;
        this.userId = userId;
        this.positions = new HashMap<>();
    }

    /**
     * Returns this account's identifier.
     *
     * @return the account id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the identifier of the user who owns this account.
     *
     * @return the user id
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Adds the shares bought in a trade to this account's position in that ticker, creating
     * the position if the account doesn't hold the ticker yet.
     *
     * @param trade The trade record to add shares from.
     * @return A long representing the updated number of held shares from the traded ticker.
     * @throws TradeException If this account is not the trade's buyer.
     */
    public synchronized Long addPosition(Trade trade){
        if (!trade.buyerId().equals(id)) {
            throw new TradeException(TradeException.Reason.BUYER_ID_DOESNT_MATCH,
                    "Buyer's id differs from buyerId in trade");
        }

        Position pos = positions.get(trade.ticker());
        if (pos == null){
            positions.put(trade.ticker(), new Position(trade.ticker(), trade.quantity(), trade.total()));
        }
        else {
            pos.addShares(trade.quantity(), trade.total());
        }

        return getTotalTicketHeld(trade.ticker());
    }

    /**
     * Removes the shares sold in a trade from this account's position in that ticker. If no
     * shares are left, the position is removed.
     *
     * @param trade The trade record to remove shares by.
     * @return The updated number of held shares of the same ticker.
     * @throws TradeException If the account doesn't hold the ticker or doesn't hold enough shares.
     */
    public synchronized Long popPosition(Trade trade){
        Position pos = positions.get(trade.ticker());

        if (pos == null){
            throw new TradeException(TradeException.Reason.SELLER_DOES_NOT_HOLD_TICKER,
                    "Seller does not hold any positions with ticker.");
        }
        if (pos.getQuantity() < trade.quantity()){
            throw new TradeException(TradeException.Reason.SELL_LESS_THAN_HELD,
                    "Seller does not hold enough stocks");
        }

        pos.removeShares(trade.quantity());
        if (pos.isEmpty()){
            positions.remove(trade.ticker());
        }
        return getTotalTicketHeld(trade.ticker());
        // make sure account's money is updated somehow
    }

    /**
     * Returns this account's position in a ticker.
     *
     * @param ticker symbol of the security
     * @return the position, or null if the account holds none of that ticker
     */
    public synchronized Position getPosition(String ticker){
        return positions.get(ticker);
    }

    /**
     * Returns how many shares of a ticker this account holds.
     *
     * @param ticker symbol of the security
     * @return the quantity held, or 0 if the account holds none
     */
    public synchronized Long getTotalTicketHeld(String ticker){
        Position pos = positions.get(ticker);
        return pos == null ? 0L : pos.getQuantity();
    }

    /**
     * Returns the shares held for every ticker in this account.
     *
     * @return a new map from ticker to quantity held
     */
    public synchronized Map<String, Long> getAllPositions(){
        Map<String, Long> result = new HashMap<>();
        for (var pos : positions.values()){
            result.put(pos.getTicker(), pos.getQuantity());
        }
        return result;
    }
}
