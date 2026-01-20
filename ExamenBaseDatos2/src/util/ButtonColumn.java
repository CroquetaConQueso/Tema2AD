package util;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener {

    private final JTable table;
    private final Action action;
    private final JButton renderButton = new JButton();
    private final JButton editButton = new JButton();
    private String text = "";
    private final boolean danger;

    public ButtonColumn(JTable table, Action action, int column, boolean danger) {
        this.table = table;
        this.action = action;
        this.danger = danger;

        // Estilo de los botones
        if (danger) {
            UiUtils.estiloBotonPeligro(renderButton);
            UiUtils.estiloBotonPeligro(editButton);
        } else {
            UiUtils.estiloBotonPlano(renderButton);
            UiUtils.estiloBotonPlano(editButton);
        }

        editButton.addActionListener(this);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        renderButton.setText(value == null ? "" : value.toString());
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {

        text = value == null ? "" : value.toString();
        editButton.setText(text);
        return editButton;
    }

    @Override
    public Object getCellEditorValue() {
        return text;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
        // El Action que recibe esta clase no necesita saber la fila;
        // simplemente usará la fila seleccionada de la tabla.
        action.actionPerformed(
                new ActionEvent(table, ActionEvent.ACTION_PERFORMED, null));
    }
}
