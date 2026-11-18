package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import interfaceadapter.ViewNames;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Checks the app shell without opening a window, so it also runs on headless CI. */
class AppBuilderTest {

  private static final Set<String> ALL_SCREENS =
      Set.of(
          ViewNames.LOGIN,
          ViewNames.DASHBOARD,
          ViewNames.SEARCH,
          ViewNames.STOCK_DETAIL,
          ViewNames.TRADE_TICKET,
          ViewNames.OPEN_ORDERS,
          ViewNames.PORTFOLIO,
          ViewNames.TRADE_HISTORY,
          ViewNames.ACCOUNT_STATEMENT,
          ViewNames.REALIZED_GAINS);

  private AppBuilder builder;

  @BeforeEach
  void setUp() {
    builder =
        new AppBuilder(new AppConfig(AppConfig.DataMode.FAKE))
            .addLoginView()
            .addDashboardView()
            .addSearchView()
            .addStockDetailView()
            .addTradeTicketView()
            .addOpenOrdersView()
            .addPortfolioView()
            .addTradeHistoryView()
            .addAccountStatementView()
            .addRealizedGainsView();
  }

  @Test
  void start_opensOnLogin() {
    builder.showFirstScreen();

    assertEquals(ViewNames.LOGIN, builder.getViewManagerModel().getState());
    assertTrue(card(ViewNames.LOGIN).isVisible());
  }

  @Test
  void clickingButtons_fromLogin_reachesEveryScreen() {
    builder.showFirstScreen();
    Set<String> reached = new HashSet<>();
    Deque<String> toVisit = new ArrayDeque<>(List.of(ViewNames.LOGIN));
    while (!toVisit.isEmpty()) {
      String screen = toVisit.pop();
      if (!reached.add(screen)) {
        continue;
      }
      for (AbstractButton button : buttonsIn(card(screen))) {
        button.doClick(0);
        String next = builder.getViewManagerModel().getState();
        assertTrue(card(next).isVisible(), "'" + next + "' should be on show");
        toVisit.push(next);
      }
    }

    assertEquals(ALL_SCREENS, reached);
  }

  private Component card(String viewName) {
    for (Component c : builder.getCardPanel().getComponents()) {
      if (viewName.equals(c.getName())) {
        return c;
      }
    }
    return fail("no card named '" + viewName + "'");
  }

  private static List<AbstractButton> buttonsIn(Component root) {
    List<AbstractButton> found = new ArrayList<>();
    if (root instanceof AbstractButton button) {
      found.add(button);
    }
    if (root instanceof Container container && !(root instanceof AbstractButton)) {
      for (Component child : container.getComponents()) {
        found.addAll(buttonsIn(child));
      }
    }
    return found;
  }
}
