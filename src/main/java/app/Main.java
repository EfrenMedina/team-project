package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import view.Theme;

/** Starts the Paper Trading App. */
public class Main {

  /**
   * Builds the window and shows it.
   *
   * @param args unused; use {@code -Ddata.mode=real} or {@code config.properties} for settings
   */
  public static void main(String[] args) {
    AppConfig config = AppConfig.load();
    Theme.install();
    SwingUtilities.invokeLater(
        () -> {
          JFrame app =
              new AppBuilder(config)
                  .addLoginView()
                  .addDashboardView()
                  .addSearchView()
                  .addStockDetailView()
                  .addTradeTicketView()
                  .addOpenOrdersView()
                  .addPortfolioView()
                  .addTradeHistoryView()
                  .addAccountStatementView()
                  .addRealizedGainsView()
                  .build();
          app.setVisible(true);
        });
  }
}
