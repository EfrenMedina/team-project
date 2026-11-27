package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import interfaceadapter.LoggedInViewModel;
import interfaceadapter.ViewManagerModel;
import interfaceadapter.navigation.NavigationController;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SidebarTest {

  private LoggedInViewModel loggedIn;
  private AtomicInteger logouts;
  private Sidebar sidebar;

  @BeforeEach
  void setUp() {
    loggedIn = new LoggedInViewModel();
    logouts = new AtomicInteger();
    sidebar = new Sidebar(SidebarItem.DASHBOARD,
        new NavigationController(new ViewManagerModel()), loggedIn, logouts::incrementAndGet);
  }

  @Test
  void userLogsIn_shown_displaysTheirName() throws Exception {
    // Action
    loggedIn.setState("alice");
    flushSwingThread();

    // Assertion
    assertEquals("alice", sidebar.usernameLabel.getText());
  }

  @Test
  void userLogsOut_shown_removesTheirName() throws Exception {
    // Inputs
    loggedIn.setState("alice");

    // Action
    loggedIn.clearUserData();
    flushSwingThread();

    // Assertion
    assertEquals(" ", sidebar.usernameLabel.getText());
  }

  @Test
  void userAlreadyLoggedIn_newSidebar_showsTheirName() {
    // Inputs
    loggedIn.setState("bob");

    // Action
    Sidebar later = new Sidebar(SidebarItem.SEARCH,
        new NavigationController(new ViewManagerModel()), loggedIn, () -> { });

    // Assertion
    assertEquals("bob", later.usernameLabel.getText());
  }

  @Test
  void logOut_clicked_runsLogOutOnce() {
    // Action
    sidebar.logoutButton.doClick();

    // Assertion
    assertEquals(1, logouts.get());
  }

  @Test
  void constructor_nullUserOrLogOut_throws() {
    // Inputs
    NavigationController navigation = new NavigationController(new ViewManagerModel());

    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new Sidebar(SidebarItem.DASHBOARD, navigation, null, () -> { }));
    assertThrows(NullPointerException.class,
        () -> new Sidebar(SidebarItem.DASHBOARD, navigation, loggedIn, null));
  }

  private static void flushSwingThread() throws Exception {
    SwingUtilities.invokeAndWait(() -> { });
  }
}
