package view.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Component;
import org.junit.jupiter.api.Test;

/** Tests for {@link AppTable}. */
class AppTableTest {

  private static AppTable signedTable(String... values) {
    AppTable table = new AppTable(new String[] {"P&L"}, 0);
    table.useSignedColumns(0);
    for (String v : values) {
      table.addRow(v);
    }
    return table;
  }

  private static Component cell(AppTable table, int row) {
    return table.prepareRenderer(table.getCellRenderer(row, 0), row, 0);
  }

  @Test
  void render_positiveValue_usesGainColour() {
    AppTable table = signedTable(Formatters.signedMoney(20_000));
    assertEquals(Theme.GAIN, cell(table, 0).getForeground());
  }

  @Test
  void render_negativeValue_usesLossColour() {
    AppTable table = signedTable(Formatters.signedMoney(-5_000));
    assertEquals(Theme.LOSS, cell(table, 0).getForeground());
  }

  @Test
  void render_zeroValue_usesNormalTextColour() {
    AppTable table = signedTable(Formatters.signedMoney(0));
    assertEquals(table.getForeground(), cell(table, 0).getForeground());
  }

  @Test
  void render_zeroAfterColouredRows_usesNormalTextColour() {
    AppTable table = signedTable(
      Formatters.signedMoney(20_000),
      Formatters.signedMoney(0),
      Formatters.signedMoney(-5_000),
      Formatters.signedMoney(0));
    assertEquals(Theme.GAIN, cell(table, 0).getForeground());
    assertEquals(table.getForeground(), cell(table, 1).getForeground());
    assertEquals(Theme.LOSS, cell(table, 2).getForeground());
    assertEquals(table.getForeground(), cell(table, 3).getForeground());
  }

  @Test
  void clearRows_afterAddingRows_leavesTableEmpty() {
    AppTable table = signedTable("a", "b");
    table.clearRows();
    assertEquals(0, table.getRowCount());
  }

  @Test
  void isCellEditable_anyCell_returnsFalse() {
    AppTable table = signedTable("a");
    assertFalse(table.isCellEditable(0, 0));
  }
}
