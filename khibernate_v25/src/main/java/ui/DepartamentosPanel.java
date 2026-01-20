package ui;

import model.Departamento;
import persistence.DepartamentoDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentosPanel extends JPanel {

    private final DepartamentoDAO dao = new DepartamentoDAO();

    private final JTextField txtBuscar = new JTextField(25);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnInspeccionar = new JButton("Inspeccionar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnEliminarSeleccionados = new JButton("Eliminar seleccionados");
    private final JButton btnVerSeleccionados = new JButton("Ver seleccionados");

    private final JTable table;
    private final DepartamentosTableModel model = new DepartamentosTableModel();

    private final JCheckBox chkSelectAll = new JCheckBox("Seleccionar todo");

    public DepartamentosPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Buscar (nombre/loc):"));
        top.add(txtBuscar);
        top.add(btnBuscar);
        top.add(btnRefrescar);
        top.add(Box.createHorizontalStrut(20));
        top.add(chkSelectAll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnInspeccionar);
        actions.add(btnEliminar);
        actions.add(btnEliminarSeleccionados);
        actions.add(btnVerSeleccionados);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(actions, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);

        table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        wireEvents();
        cargarTodos();
    }

    private void wireEvents() {
        btnRefrescar.addActionListener(e -> cargarTodos());

        btnBuscar.addActionListener(e -> {
            String t = txtBuscar.getText();
            if (t == null || t.trim().isEmpty()) cargarTodos();
            else cargarLista(dao.buscar(t));
        });

        chkSelectAll.addActionListener(e -> model.setAllSelected(chkSelectAll.isSelected()));

        btnNuevo.addActionListener(e -> {
            DepartamentoDialog dlg = new DepartamentoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Nuevo departamento",
                    null,
                    ModoDialogo.NUEVO
            );
            dlg.setVisible(true);
            if (dlg.isOk()) {
                dao.insertar(dlg.getDepartamento());
                cargarTodos();
            }
        });

        btnEditar.addActionListener(e -> {
            Departamento d = getSeleccionadoUnico();
            if (d == null) return;

            DepartamentoDialog dlg = new DepartamentoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Editar departamento",
                    d,
                    ModoDialogo.EDITAR
            );
            dlg.setVisible(true);
            if (dlg.isOk()) {
                dao.actualizar(dlg.getDepartamento());
                cargarTodos();
            }
        });

        btnInspeccionar.addActionListener(e -> {
            Departamento d = getSeleccionadoUnico();
            if (d == null) return;

            DepartamentoDialog dlg = new DepartamentoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Inspeccionar departamento",
                    d,
                    ModoDialogo.VER
            );
            dlg.setVisible(true);
        });

        btnEliminar.addActionListener(e -> {
            Departamento d = getSeleccionadoUnico();
            if (d == null) return;

            int res = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el departamento " + d.getDeptoNo() + "?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                dao.borrarPorId(d.getDeptoNo());
                cargarTodos();
            }
        });

        btnEliminarSeleccionados.addActionListener(e -> {
            List<Departamento> seleccionados = model.getSelectedRows();
            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay filas seleccionadas.");
                return;
            }

            int res = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar " + seleccionados.size() + " departamentos seleccionados?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                for (Departamento d : seleccionados) dao.borrarPorId(d.getDeptoNo());
                cargarTodos();
            }
        });

        btnVerSeleccionados.addActionListener(e -> {
            List<Departamento> seleccionados = model.getSelectedRows();
            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay filas seleccionadas.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (Departamento d : seleccionados) {
                sb.append("[").append(d.getDeptoNo()).append("] ")
                        .append(d.getDnombre()).append(" - ")
                        .append(d.getLoc()).append("\n");
            }
            JTextArea area = new JTextArea(sb.toString(), 15, 60);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Seleccionados", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void cargarTodos() {
        cargarLista(dao.listarTodos());
    }

    private void cargarLista(List<Departamento> lista) {
        model.setData(lista);
        chkSelectAll.setSelected(false);
    }

    private Departamento getSeleccionadoUnico() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila (clic sobre la fila).");
            return null;
        }
        return model.getAt(row);
    }

    private static class DepartamentosTableModel extends AbstractTableModel {

        private final String[] cols = {"Sel", "depto_no", "dnombre", "loc"};
        private final List<Row> rows = new ArrayList<>();

        private static class Row {
            boolean selected;
            Departamento d;
            Row(Departamento d) { this.d = d; }
        }

        public void setData(List<Departamento> data) {
            rows.clear();
            if (data != null) for (Departamento d : data) rows.add(new Row(d));
            fireTableDataChanged();
        }

        public Departamento getAt(int viewRow) {
            return rows.get(viewRow).d;
        }

        public List<Departamento> getSelectedRows() {
            List<Departamento> out = new ArrayList<>();
            for (Row r : rows) if (r.selected) out.add(r.d);
            return out;
        }

        public void setAllSelected(boolean selected) {
            for (Row r : rows) r.selected = selected;
            if (!rows.isEmpty()) fireTableRowsUpdated(0, rows.size() - 1);
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Boolean.class;
            if (columnIndex == 1) return Integer.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            Departamento d = r.d;
            switch (columnIndex) {
                case 0: return r.selected;
                case 1: return d.getDeptoNo();
                case 2: return d.getDnombre();
                case 3: return d.getLoc();
                default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                rows.get(rowIndex).selected = (Boolean) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
