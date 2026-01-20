package ui;

import model.Empleado;
import persistence.EmpleadoDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EmpleadosPanel extends JPanel {

    private final EmpleadoDAO dao = new EmpleadoDAO();

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
    private final EmpleadosTableModel model = new EmpleadosTableModel();

    private final JCheckBox chkSelectAll = new JCheckBox("Seleccionar todo");

    public EmpleadosPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Buscar (apellido/oficio):"));
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
        btnRefrescar.addActionListener(evt -> cargarTodos());

        btnBuscar.addActionListener(evt -> {
            String t = txtBuscar.getText();
            if (t == null || t.trim().isEmpty()) cargarTodos();
            else cargarLista(dao.buscar(t));
        });

        chkSelectAll.addActionListener(evt -> model.setAllSelected(chkSelectAll.isSelected()));

        btnNuevo.addActionListener(evt -> {
            EmpleadoDialog dlg = new EmpleadoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Nuevo empleado",
                    null,
                    ModoDialogo.NUEVO
            );
            dlg.setVisible(true);
            if (dlg.isOk()) {
                dao.insertar(dlg.getEmpleado());
                cargarTodos();
            }
        });

        btnEditar.addActionListener(evt -> {
            Empleado emp = getSeleccionadoUnico();
            if (emp == null) return;

            EmpleadoDialog dlg = new EmpleadoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Editar empleado",
                    emp,
                    ModoDialogo.EDITAR
            );
            dlg.setVisible(true);
            if (dlg.isOk()) {
                dao.actualizar(dlg.getEmpleado());
                cargarTodos();
            }
        });

        btnInspeccionar.addActionListener(evt -> {
            Empleado emp = getSeleccionadoUnico();
            if (emp == null) return;

            EmpleadoDialog dlg = new EmpleadoDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Inspeccionar empleado",
                    emp,
                    ModoDialogo.VER
            );
            dlg.setVisible(true);
        });

        btnEliminar.addActionListener(evt -> {
            Empleado emp = getSeleccionadoUnico();
            if (emp == null) return;

            int res = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el empleado " + emp.getEmpNo() + " (" + emp.getApellido() + ")?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                dao.borrarPorId(emp.getEmpNo());
                cargarTodos();
            }
        });

        btnEliminarSeleccionados.addActionListener(evt -> {
            List<Empleado> seleccionados = model.getSelectedRows();
            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay filas seleccionadas.");
                return;
            }

            int res = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar " + seleccionados.size() + " empleados seleccionados?",
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                for (Empleado emp : seleccionados) dao.borrarPorId(emp.getEmpNo());
                cargarTodos();
            }
        });

        btnVerSeleccionados.addActionListener(evt -> {
            List<Empleado> seleccionados = model.getSelectedRows();
            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay filas seleccionadas.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (Empleado emp : seleccionados) {
                sb.append("[").append(emp.getEmpNo()).append("] ")
                        .append(emp.getApellido()).append(" / ")
                        .append(emp.getOficio()).append(" / ")
                        .append(emp.getFechaAlt()).append("\n");
            }
            JTextArea area = new JTextArea(sb.toString(), 15, 80);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Seleccionados", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void cargarTodos() {
        cargarLista(dao.listarTodos());
    }

    private void cargarLista(List<Empleado> lista) {
        model.setData(lista);
        chkSelectAll.setSelected(false);
    }

    private Empleado getSeleccionadoUnico() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila (clic sobre la fila).");
            return null;
        }
        return model.getAt(row);
    }

    private static class EmpleadosTableModel extends AbstractTableModel {

        private final String[] cols = {"Sel", "emp_no", "apellido", "oficio", "dir", "fecha_alt", "salario", "comision", "dept_no"};
        private final List<Row> rows = new ArrayList<>();

        private static class Row {
            boolean selected;
            Empleado emp;
            Row(Empleado emp) { this.emp = emp; }
        }

        public void setData(List<Empleado> data) {
            rows.clear();
            if (data != null) for (Empleado emp : data) rows.add(new Row(emp));
            fireTableDataChanged();
        }

        public Empleado getAt(int viewRow) {
            return rows.get(viewRow).emp;
        }

        public List<Empleado> getSelectedRows() {
            List<Empleado> out = new ArrayList<>();
            for (Row r : rows) if (r.selected) out.add(r.emp);
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
            if (columnIndex == 4) return Integer.class;
            if (columnIndex == 5) return Date.class;
            if (columnIndex == 6) return Integer.class;
            if (columnIndex == 7) return Integer.class;
            if (columnIndex == 8) return Integer.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            Empleado emp = r.emp;
            switch (columnIndex) {
                case 0: return r.selected;
                case 1: return emp.getEmpNo();
                case 2: return emp.getApellido();
                case 3: return emp.getOficio();
                case 4: return emp.getDir();
                case 5: return emp.getFechaAlt();
                case 6: return emp.getSalario();
                case 7: return emp.getComision();
                case 8: return emp.getDeptNo();
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
