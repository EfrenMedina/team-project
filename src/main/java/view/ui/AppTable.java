package view.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * A table that right-aligns numeric columns.
 */
public class AppTable extends JTable {
  private final DefaultTableModel model;

  /**
   * numericColumns: column indexes to right-align (money, quantity, percent).
   */
  public AppTable(String[] columns, int... numericColumns) {
    super(new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int r, int c) {
        return false;
      }
    });
    this.model = (DefaultTableModel) getModel();
    setRowHeight(28);
    setFillsViewportHeight(true);

    DefaultTableCellRenderer right = new DefaultTableCellRenderer();
    right.setHorizontalAlignment(SwingConstants.RIGHT);
    for (int c : numericColumns) {
      getColumnModel().getColumn(c).setCellRenderer(right);
    }
  }

  /**
   * Right-aligns the given columns and colours them by sign (+ green, − red).
   *
   * @param columns column indexes holding signed values
   */
  public void useSignedColumns(int... columns) {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        String text = String.valueOf(value);
        if (!isSelected) {
          if (text.startsWith("+")) {
            c.setForeground(Theme.GAIN);
          } else if (text.startsWith(Formatters.MINUS)) {
            c.setForeground(Theme.LOSS);
          }
        }
        return c;
      }
    };
    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    for (int col : columns) {
      getColumnModel().getColumn(col).setCellRenderer(renderer);
    }
  }

  public void addRow(Object... cells) {
    model.addRow(cells);
  }

  public void clearRows() {
    model.setRowCount(0);
  }
}