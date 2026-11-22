package usecase.viewstock;

import entity.CompanyProfile;
import entity.PricePoint;
import entity.Quote;
import java.util.List;
import java.util.Objects;

/**
 * Everything the Stock detail screen needs for one stock, passed to the presenter.
 *
 * @param quote the latest price, the day's open, high and low, and the previous close
 * @param profile the company's name, exchange, industry and market cap
 * @param range the range the history covers
 * @param history one point per trading day in the range, oldest first; empty if the stock
 *     didn't trade in the range
 */
public record ViewStockOutputData(
    Quote quote, CompanyProfile profile, ChartRange range, List<PricePoint> history) {

  /**
   * Creates the output and keeps its own copy of the history, so later changes to the list
   * passed in don't change it.
   *
   * @throws NullPointerException if any value, or any point in {@code history}, is null
   */
  public ViewStockOutputData {
    Objects.requireNonNull(quote, "quote");
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(range, "range");
    history = List.copyOf(history);
  }
}
