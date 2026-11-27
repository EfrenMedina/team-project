package usecase.viewstock;

import java.util.Objects;

/**
 * Which stock to show and how far back its chart goes. The ticker is passed exactly as given;
 * the view-stock use case trims it, upper-cases it and decides whether it is valid.
 *
 * @param ticker the stock's ticker, for example {@code "AAPL"}
 * @param range how far back the chart goes
 */
public record ViewStockInputData(String ticker, ChartRange range) {

  /**
   * Creates the input.
   *
   * @throws NullPointerException if {@code range} is null; the screen always has a range
   *     selected
   */
  public ViewStockInputData {
    Objects.requireNonNull(range, "range");
  }
}
