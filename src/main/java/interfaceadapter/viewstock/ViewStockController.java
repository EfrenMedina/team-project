package interfaceadapter.viewstock;

import java.util.Objects;
import usecase.viewstock.ChartRange;
import usecase.viewstock.ViewStockInputBoundary;
import usecase.viewstock.ViewStockInputData;

/**
 * Turns opening a stock, or clicking one of its range buttons, into a call to the view-stock
 * use case. The call may wait on the network, so the screen must make it off the Swing event
 * thread.
 */
public class ViewStockController {

  /** The view-stock use case. */
  private final ViewStockInputBoundary viewStockInteractor;

  /**
   * Creates the controller.
   *
   * @param viewStockInteractor the view-stock use case to call
   * @throws NullPointerException if {@code viewStockInteractor} is null
   */
  public ViewStockController(ViewStockInputBoundary viewStockInteractor) {
    this.viewStockInteractor = Objects.requireNonNull(viewStockInteractor, "viewStockInteractor");
  }

  /**
   * Loads a stock with its chart covering the given range.
   *
   * @param ticker the stock's ticker, for example from a search result
   * @param range how far back the chart goes
   */
  public void execute(String ticker, ChartRange range) {
    viewStockInteractor.execute(new ViewStockInputData(ticker, range));
  }
}
