package interfaceadapter.navigation;

import interfaceadapter.ViewManagerModel;

/**
 * Switches to another screen. Used by plain navigation clicks (sidebar, back links) that have no
 * use case behind them. Clicks that do real work, such as logging in, should go through that
 * feature's controller and presenter instead.
 */
public class NavigationController {

  private final ViewManagerModel viewManagerModel;

  public NavigationController(ViewManagerModel viewManagerModel) {
    this.viewManagerModel = viewManagerModel;
  }

  /**
   * Shows the screen with the given card name.
   *
   * @param viewName one of the constants in {@link interfaceadapter.ViewNames}
   */
  public void navigateTo(String viewName) {
    viewManagerModel.setState(viewName);
    viewManagerModel.firePropertyChanged();
  }
}
