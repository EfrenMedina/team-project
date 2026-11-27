package usecase.viewstock;

/**
 * The view-stock use case, as seen by the controller. The controller calls {@link #execute}
 * without knowing which class does the work.
 */
public interface ViewStockInputBoundary {

  /**
   * Loads one stock's quote, company profile and price history for the chosen range. The
   * outcome is reported to the {@link ViewStockOutputBoundary}, not returned.
   *
   * @param inputData which stock to show and how far back its chart goes
   */
  void execute(ViewStockInputData inputData);
}
