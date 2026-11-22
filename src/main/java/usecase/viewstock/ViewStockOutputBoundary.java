package usecase.viewstock;

/**
 * Receives the outcome of the view-stock use case. The presenter implements it to update the
 * screen; tests implement it to record what happened.
 */
public interface ViewStockOutputBoundary {

  /**
   * Called when the quote, profile and history were all loaded.
   *
   * @param outputData everything the screen shows for the stock
   */
  void prepareSuccessView(ViewStockOutputData outputData);

  /**
   * Called when the stock could not be shown. The request is passed back so the screen can
   * still show which stock and range were asked for, without having to remember them itself.
   *
   * @param ticker the ticker as it was requested; may be blank or null
   * @param range the range that was requested
   * @param error a message explaining what went wrong, ready to show to the user
   */
  void prepareFailView(String ticker, ChartRange range, String error);
}
