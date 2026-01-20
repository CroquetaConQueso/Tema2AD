package vista.datos;

import dao.DatosDAO;
import model.Dato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;

public class TabDatos extends JPanel {

    private final DatosDAO dao = new DatosDAO();

    // Barra superior de búsqueda
    private final JComboBox<String> cmbCampo = new JComboBox<>(new String[]{"Todos", "ID", "Nombre", "Edad"});
    private final JTextField txtBuscar = new JTextField(24);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiarFiltro = new JButton("Limpiar");

    // Botones CRUD
    private final JButton btnNuevo = new JButton("Insertar");
    private final JButton btnGuardar = new JButton("Actualizar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    // Formulario de edición
    private final JTextField txtId = new JTextField(6);
    private final JTextField txtNombre = new JTextField(28);
    private final JTextField txtEdad = new JTextField(6);

    // Tabla
    private final DefaultTableModel modelo =
            new DefaultTableModel(new String[]{"ID", "Nombre", "Edad"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable tabla = new JTable(modelo);

    public TabDatos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(crearToolbar(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearForm(), BorderLayout.SOUTH);

        configurarTabla();
        wiringEventos();
        cargar();
    }

    private JComponent crearToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(new JLabel("Buscar por: "));
        tb.add(cmbCampo);
        tb.add(Box.createHorizontalStrut(6));
        tb.add(txtBuscar);
        tb.add(btnBuscar);
        tb.add(btnLimpiarFiltro);
        tb.addSeparator(new Dimension(16, 0));

        tb.add(btnNuevo);
        tb.add(btnGuardar);
        tb.add(btnEliminar);
        tb.addSeparator(new Dimension(16, 0));
        tb.add(btnRefrescar);
        return tb;
    }

    private JComponent crearCentro() {
        JScrollPane sp = new JScrollPane(tabla);
        tabla.setFillsViewportHeight(true);
        return sp;
    }

    private JComponent crearForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Edición"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);

        c.gridy = 0; c.gridx = 0; c.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("ID:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        txtId.setEditable(false);
        p.add(txtId, c);

        c.gridx = 2; c.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Nombre:"), c);
        c.gridx = 3; c.anchor = GridBagConstraints.LINE_START;
        p.add(txtNombre, c);

        c.gridx = 4; c.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Edad:"), c);
        c.gridx = 5; c.anchor = GridBagConstraints.LINE_START;
        p.add(txtEdad, c);

        return p;
    }

    private void configurarTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        var sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int r = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                txtId.setText(String.valueOf(modelo.getValueAt(r, 0)));
                txtNombre.setText(String.valueOf(modelo.getValueAt(r, 1)));
                txtEdad.setText(String.valueOf(modelo.getValueAt(r, 2)));
            }
        });
    }

    private void wiringEventos() {
        btnRefrescar.addActionListener(e -> { txtBuscar.setText(""); cmbCampo.setSelectedIndex(0); cargar(); });
        btnLimpiarFiltro.addActionListener(e -> { txtBuscar.setText(""); cmbCampo.setSelectedIndex(0); cargar(); });
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar()); // Enter en la caja

        btnNuevo.addActionListener(e -> insertar());
        btnGuardar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
    }

    /* ==== Acciones ==== */

    private void cargar() {
        try {
            modelo.setRowCount(0);
            for (Dato d : dao.listarTodos())
                modelo.addRow(new Object[]{d.getIdentificador(), d.getNombre(), d.getEdad()});
            limpiarForm();
        } catch (SQLException ex) { error("Cargando datos", ex); }
    }

    private void buscar() {
        String campo = cmbCampo.getSelectedItem().toString();
        String q = txtBuscar.getText().trim();
        if (q.isEmpty()) { cargar(); return; }
        try {
            modelo.setRowCount(0);
            for (Dato d : dao.buscar(campo, q))
                modelo.addRow(new Object[]{d.getIdentificador(), d.getNombre(), d.getEdad()});
            limpiarForm();
        } catch (SQLException ex) { error("Buscando", ex); }
    }

    private void insertar() {
        try {
            var v = validarEntrada(false);
            if (!v.ok) { warn(v.mensaje); return; }

            // Regla de duplicados: (nombre, edad) único (case-insensitive)
            if (dao.existeNombreEdad(v.nombre, v.edad, null)) {
                warn("Ya existe un registro con ese nombre y edad.");
                return;
            }

            int id = dao.insertar(v.nombre, v.edad);
            info("Insertado ID " + id);
            cargar();
            seleccionarId(id);
        } catch (SQLException ex) { error("Insertando", ex); }
    }

    private void actualizar() {
        try {
            var v = validarEntrada(true);
            if (!v.ok) { warn(v.mensaje); return; }

            if (dao.existeNombreEdad(v.nombre, v.edad, v.id)) {
                warn("Ya existe otro registro con ese nombre y edad.");
                return;
            }

            if (dao.actualizar(v.id, v.nombre, v.edad)) {
                info("Actualizado");
                cargar();
                seleccionarId(v.id);
            } else {
                warn("No se actualizó (ID inexistente).");
            }
        } catch (SQLException ex) { error("Actualizando", ex); }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isEmpty()) { warn("Selecciona una fila."); return; }
            int id = Integer.parseInt(txtId.getText());
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar ID " + id + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            if (dao.eliminar(id)) { info("Eliminado"); cargar(); }
            else warn("No se eliminó (ID inexistente).");
        } catch (SQLException ex) { error("Eliminando", ex); }
    }

    /* ==== Validación & utilidades ==== */

    private static class Val {
        boolean ok; String mensaje; int id; String nombre; int edad;
    }

    /** @param requireId true si es UPDATE/DELETE */
    private Val validarEntrada(boolean requireId) {
        Val v = new Val(); v.ok = false;

        if (requireId) {
            if (txtId.getText().isEmpty()) { v.mensaje = "Selecciona una fila (ID)."; return v; }
            try { v.id = Integer.parseInt(txtId.getText()); }
            catch (NumberFormatException e) { v.mensaje = "ID inválido."; return v; }
        }

        v.nombre = txtNombre.getText().trim();
        if (v.nombre.isEmpty()) { v.mensaje = "Nombre obligatorio."; return v; }

        try { v.edad = Integer.parseInt(txtEdad.getText().trim()); }
        catch (NumberFormatException e) { v.mensaje = "Edad numérica."; return v; }

        if (v.edad < 0 || v.edad > 120) { v.mensaje = "Edad entre 0 y 120."; return v; }

        v.ok = true; return v;
    }

    private void seleccionarId(int id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if ((int) modelo.getValueAt(i, 0) == id) {
                int view = tabla.convertRowIndexToView(i);
                tabla.setRowSelectionInterval(view, view);
                tabla.scrollRectToVisible(tabla.getCellRect(view, 0, true));
                break;
            }
        }
    }

    private void limpiarForm() {
        txtId.setText(""); txtNombre.setText(""); txtEdad.setText("");
        tabla.clearSelection();
    }

    private void info(String m) { JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
    private void error(String ctx, Exception ex) {
        JOptionPane.showMessageDialog(this, ctx + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

