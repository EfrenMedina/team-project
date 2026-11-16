package entity;

import java.util.Objects;

/**
 * The company details shown on a stock's page: the stock itself, its industry and its market
 * capitalization.
 *
 * @param stock the stock's ticker, company name and exchange
 * @param industry the company's industry, for example {@code "Technology"}; empty if the data
 *     service doesn't know it
 * @param marketCap the total value of all the company's shares; zero if unknown, never negative
 */
public record CompanyProfile(Stock stock, String industry, Money marketCap) {

  /**
   * Creates a profile.
   *
   * @throws NullPointerException if any component is null
   * @throws IllegalArgumentException if {@code marketCap} is negative
   */
  public CompanyProfile {
    Objects.requireNonNull(stock, "stock");
    Objects.requireNonNull(industry, "industry");
    Objects.requireNonNull(marketCap, "marketCap");

    if (marketCap.isNegative()) {
      throw new IllegalArgumentException("Market cap must not be negative: " + marketCap);
    }
  }
}
