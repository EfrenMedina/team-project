package view;

import interfaceadapter.ViewNames;

/** Entries in the sidebar, top to bottom, and the screen each one opens. */
public enum SidebarItem {
  DASHBOARD("Dashboard", ViewNames.DASHBOARD),
  SEARCH("Search", ViewNames.SEARCH),
  PORTFOLIO("Portfolio", ViewNames.PORTFOLIO),
  ORDERS("Orders", ViewNames.OPEN_ORDERS),
  HISTORY("History", ViewNames.TRADE_HISTORY),
  STATEMENTS("Statements", ViewNames.ACCOUNT_STATEMENT),
  /** No wireframe yet, so it opens nothing. Log out sits at the bottom of the sidebar. */
  ACCOUNT("Account", null);

  private final String label;
  private final String target;

  SidebarItem(String label, String target) {
    this.label = label;
    this.target = target;
  }

  public String label() {
    return label;
  }

  /** The card name this item opens, or null if it has no screen yet. */
  public String target() {
    return target;
  }
}
