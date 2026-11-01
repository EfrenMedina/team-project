package entity;

import java.util.*;


/**
 * A user's brokerage account and the lots it holds.
 *
 * <p>Holdings are stored as individual {@link Position} lots, grouped by ticker. Buying the
 * same ticker more than once adds another lot rather than merging into an existing one, so
 * each purchase keeps its own cost and acquisition time.
 */
public class Account {
    private final String id;
    private final String userId;
    private Map<String, Map<String, Position>> lots; // individual holdings as original objects

    /**
     * Creates an account with no holdings.
     *
     * @param id unique identifier for this account
     * @param userId identifier of the user who owns this account
     */
    public Account(String id, String userId){
        this.id = id;
        this.userId = userId;
        this.lots = new HashMap<>(new HashMap<>());
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
     * Adds a position to this account, using the trade parameter.
     *
     * @param trade The trade record to add a new position from.
     * @return A long representing the updated number of held shares from the traded ticker.
     * @throws TradeException If an inconsistent trade is passed in.
     */
    public synchronized Long addPosition(Trade trade, IdGenerator idGenerator){
        if(trade.buyerId().equals(id)) {
            var pos = new Position(idGenerator.nextId(), trade.ticker(),
                    trade.quantity(), trade.pricePerShare(), trade.soldAt());

            if (lots.get(trade.ticker()) == null){
                lots.put(trade.ticker(), new HashMap<>(Map.of(idGenerator.nextId(), pos)));
            }
            else {
                lots.get(trade.ticker()).put(idGenerator.nextId(), pos);
            }

            return getTotalTicketHeld(trade.ticker());
        }
        else {
            throw new TradeException(TradeException.Reason.BUYER_ID_DOESNT_MATCH,
                    "Buyer's id differs from sellerId in trade");
        }
    }

    /**
     * Removes a quantity of sold position following the details in trade.
     * @return The updated number of held positions of the same ticker.
     * @throws TradeException If an inconsistent trade is passed in.
     */
    public synchronized Long popPosition(Trade trade){
        if(trade.sellerId().equals(id)) {

            // prevent getPositionFromLotId from failing
            if (lots.get(trade.ticker()) == null){
                throw new TradeException(TradeException.Reason.SELLER_DOES_NOT_HOLD_TICKER,
                        "Seller does not hold any positions with ticker.");
            }

            Position pos = getPositionFromLotId(trade.lotId(), trade.ticker());

            if (pos == null) {
                throw new TradeException(TradeException.Reason.LOTID_INCONSISTENCY,
                        "Seller does not hold position with lotId from trade.");
            }
            if (!pos.getId().equals(trade.lotId())) {
                throw new TradeException(TradeException.Reason.LOTID_INCONSISTENCY,
                        "Seller's lotId differs from the trade lotId");
            }
            if (!pos.getTicker().equals(trade.ticker())){
                throw new TradeException(TradeException.Reason.LOTID_INCONSISTENCY,
                        "Seller's lotId's ticker differs from trade ticker");
            }
            if (pos.getQuantity() < trade.quantity()){
                throw new TradeException(TradeException.Reason.SELL_LESS_THAN_HELD, "Seller does not hold enough" +
                        "stocks");
            }

            pos.reduceQuantity(trade.quantity());
            return getTotalTicketHeld(pos.getTicker());
            // make sure account's money is updated somehow
        }
        else {
            throw new TradeException(TradeException.Reason.SELLER_ID_DOESNT_MATCH,
                    "Seller's id differs from sellerId in trade");
        }
    }

    /**
     * Returns a reference to the held position of the account sharing the same lotId. Throws exception if lotId does not exist
     * @param lotId the lotId of a existing held position.
     * @return A reference to the held position with the same lotId.
     */
    private Position getPositionFromLotId(String lotId, String ticker) {
        return lots.get(ticker).get(lotId);
    }

    /**
     * Returns how many shares of a ticker this account holds, summed across all its lots.
     *
     * @param ticker symbol of the security
     * @return the total quantity held, or 0 if the account holds none
     */
    public Long getTotalTicketHeld(String ticker){
        long tot = 0;
        var ids = lots.getOrDefault(ticker, Map.of());
        for (var position : ids.values()){
            tot += position.getQuantity();
        }
        return tot;
    }

    /**
     * Returns the total shares held for every ticker in this account.
     *
     * @return a new map from ticker to total quantity held
     */
    public Map<String, Long> getAllPositions(){
        Map<String, Long> positions = new HashMap<>();
        for (var ticker : lots.keySet()){
            positions.put(ticker, getTotalTicketHeld(ticker));
        }
        return positions;
    }
}
