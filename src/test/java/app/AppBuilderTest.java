package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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

  private static final String PASSWORD = "hunter222";

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
            .addRealizedGainsView()
            .addSignupUseCase()
            .addLoginUseCase()
            .addLogoutUseCase();
  }

  @Test
  void start_opensOnLogin() {
    builder.showFirstScreen();

    assertEquals(ViewNames.LOGIN, builder.getViewManagerModel().getState());
    assertTrue(card(ViewNames.LOGIN).isVisible());
  }

  @Test
  void clickingButtons_fromDashboard_reachesEveryScreenIncludingLogin() {
    // Login needs real credentials now, so explore from the first screen after it.
    navigateTo(ViewNames.DASHBOARD);
    Set<String> reached = new HashSet<>();
    Deque<String> toVisit = new ArrayDeque<>(List.of(ViewNames.DASHBOARD));
    while (!toVisit.isEmpty()) {
      String screen = toVisit.pop();
      if (!reached.add(screen)) {
        continue;
      }
      for (AbstractButton button : all(card(screen), AbstractButton.class)) {
        button.doClick(0);
        String next = builder.getViewManagerModel().getState();
        assertTrue(card(next).isVisible(), "'" + next + "' should be on show");
        toVisit.push(next);
      }
    }

    assertEquals(ALL_SCREENS, reached);
  }

  @Test
  void signUpLogOutLogIn_throughTheScreens_showsDashboardWithUsername() throws Exception {
    builder.showFirstScreen();

    // Sign up
    List<JTextField> usernames = textFields(card(ViewNames.LOGIN));
    List<JPasswordField> passwords = all(card(ViewNames.LOGIN), JPasswordField.class);
    onSwingThread(() -> {
      usernames.get(1).setText("alice");
      passwords.get(1).setText(PASSWORD);
      passwords.get(2).setText(PASSWORD);
      button(card(ViewNames.LOGIN), "Create account").doClick();
    });
    waitForScreen(ViewNames.DASHBOARD);
    assertTrue(hasLabel(card(ViewNames.DASHBOARD), "alice"));

    // Log out
    onSwingThread(() -> button(card(ViewNames.DASHBOARD), "Log out").doClick());
    waitForScreen(ViewNames.LOGIN);
    assertFalse(hasLabel(card(ViewNames.DASHBOARD), "alice"));

    // A wrong password stays on Login with the generic message
    onSwingThread(() -> {
      usernames.get(0).setText("alice");
      passwords.get(0).setText("wrong-password");
      lastButton(card(ViewNames.LOGIN), "Log in").doClick();
    });
    waitForLabel(card(ViewNames.LOGIN), "! Username or password is incorrect");
    assertEquals(ViewNames.LOGIN, builder.getViewManagerModel().getState());
    waitUntilEnabled(lastButton(card(ViewNames.LOGIN), "Log in"));

    // Log in
    onSwingThread(() -> {
      passwords.get(0).setText(PASSWORD);
      lastButton(card(ViewNames.LOGIN), "Log in").doClick();
    });
    waitForScreen(ViewNames.DASHBOARD);
    assertTrue(hasLabel(card(ViewNames.DASHBOARD), "alice"));
  }

  // ---- Helpers ----

  private void navigateTo(String viewName) {
    builder.getViewManagerModel().setState(viewName);
    builder.getViewManagerModel().firePropertyChanged();
  }

  private Component card(String viewName) {
    for (Component c : builder.getCardPanel().getComponents()) {
      if (viewName.equals(c.getName())) {
        return c;
      }
    }
    return fail("no card named '" + viewName + "'");
  }

  /** Waits for a background login or sign-up to switch to a screen. */
  private void waitForScreen(String viewName) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    while (!viewName.equals(builder.getViewManagerModel().getState())
        && System.nanoTime() < deadline) {
      Thread.sleep(20);
    }
    flushSwingThread();
    assertEquals(viewName, builder.getViewManagerModel().getState());
    assertTrue(card(viewName).isVisible());
  }

  /** Waits for a background attempt to show a message. */
  private static void waitForLabel(Component root, String text) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    while (System.nanoTime() < deadline) {
      flushSwingThread();
      if (hasLabel(root, text)) {
        return;
      }
      Thread.sleep(20);
    }
    fail("'" + text + "' never appeared");
  }

  /** Waits for a form's button to come back after its background attempt finishes. */
  private static void waitUntilEnabled(AbstractButton button) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
    while (!button.isEnabled() && System.nanoTime() < deadline) {
      Thread.sleep(20);
    }
    flushSwingThread();
    assertTrue(button.isEnabled());
  }

  private static boolean hasLabel(Component root, String text) {
    return all(root, JLabel.class).stream().anyMatch(label -> text.equals(label.getText()));
  }

  private static AbstractButton button(Component root, String text) {
    return all(root, AbstractButton.class).stream()
        .filter(b -> text.equals(b.getText()))
        .findFirst()
        .orElseThrow();
  }

  /** The last button with this text; "Log in" is both a tab and, later, the form's button. */
  private static AbstractButton lastButton(Component root, String text) {
    List<AbstractButton> matches = all(root, AbstractButton.class).stream()
        .filter(b -> text.equals(b.getText()))
        .toList();
    return matches.get(matches.size() - 1);
  }

  /** Text fields that aren't password fields: the Log-in username, then the Sign-up one. */
  private static List<JTextField> textFields(Component root) {
    return all(root, JTextField.class).stream()
        .filter(field -> !(field instanceof JPasswordField))
        .toList();
  }

  private static <T> List<T> all(Component root, Class<T> type) {
    List<T> found = new ArrayList<>();
    if (type.isInstance(root)) {
      found.add(type.cast(root));
    }
    if (root instanceof Container container && !(root instanceof AbstractButton)) {
      for (Component child : container.getComponents()) {
        found.addAll(all(child, type));
      }
    }
    return found;
  }

  private static void onSwingThread(Runnable action) throws Exception {
    SwingUtilities.invokeAndWait(action);
  }

  private static void flushSwingThread() throws Exception {
    SwingUtilities.invokeAndWait(() -> { });
  }
}
