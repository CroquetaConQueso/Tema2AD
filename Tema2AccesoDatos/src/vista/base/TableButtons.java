package vista.base;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TableButtons {

    public static class BtnRenderer extends JButton implements TableCellRenderer {
        public BtnRenderer(String text){
            setText(text);
            setFocusable(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v==null? "" : v.toString());
            return this;
        }
    }

    public static class BtnEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private JTable table;
        private final java.util.function.IntConsumer onClick;

        public BtnEditor(String text, java.util.function.IntConsumer onClick) {
            this.onClick = onClick;
            btn.setText(text);
            btn.addActionListener(this::handle);
        }
        private void handle(ActionEvent e) {
            int row = table.getEditingRow();
            fireEditingStopped();
            onClick.accept(row);
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.table = t;
            btn.setText(v==null? "" : v.toString());
            return btn;
        }
        @Override
        public Object getCellEditorValue() { return btn.getText(); }
    }
}
