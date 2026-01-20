package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Muestra un resumen de los seleccionados (genérico) */
public class DialogSeleccion extends JDialog {
    private final JTable tabla;

    public DialogSeleccion(Window owner, String[] cols, List<Object[]> rows) {
        super(owner, "Selección", ModalityType.APPLICATION_MODAL);
        setSize(700, 400);
        setLocationRelativeTo(owner);

        tabla = new JTable(new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        DefaultTableModel m = (DefaultTableModel) tabla.getModel();
        for (Object[] r : rows) m.addRow(r);

        getContentPane().add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}
