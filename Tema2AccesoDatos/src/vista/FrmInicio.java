// src/vista/FrmInicio.java
package vista;

import dao.ClientesDAO;
import dao.DireccionesDAO;
import dao.ContactosDAO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.regex.Pattern;

public class FrmInicio extends JFrame {

    public FrmInicio() {
        setTitle("Empresa · One Page (empresa_clientes)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Tamaño inicial cómodo + mínimo + centrado; intenta abrir maximizado
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) (screen.width * 0.90);
        int h = (int) (screen.height * 0.85);
        setSize(Math.max(1100, w), Math.max(650, h));
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // Título
        JLabel titulo = new JLabel("Base de datos: empresa_clientes", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,12,4,12));
        header.add(titulo, BorderLayout.WEST);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes",    new TabClientes());
        tabs.addTab("Direcciones", new TabDirecciones());
        tabs.addTab("Contactos",   new TabContactos());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    /* =============== BASE GENÉRICA PARA PESTAÑAS CRUD =============== */
    abstract class CrudTab extends JPanel {
        protected final JTextField txtBuscar = new JTextField(28);
        protected final JButton btnRefrescar = new JButton("Refrescar");
        protected final JButton btnNuevo     = new JButton("Nuevo");
        protected final JTable tabla         = new JTable();
        protected final DefaultTableModel modelo;
        protected final JPanel editorPanel   = new JPanel(new GridBagLayout());
        protected Integer editId = null; // null = nuevo; !=null = editar

        protected CrudTab(String[] columnas) {
            setLayout(new BorderLayout(0,0));

            // Barra superior
            JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            izq.add(new JLabel("Buscar:"));
            izq.add(txtBuscar);
            JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            der.add(btnRefrescar);
            der.add(btnNuevo);
            JPanel top = new JPanel(new BorderLayout(10,10));
            top.add(izq, BorderLayout.WEST);
            top.add(der, BorderLayout.EAST);
            top.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
            add(top, BorderLayout.NORTH);

            // Tabla
            modelo = new DefaultTableModel(columnas, 0) {
                @Override public boolean isCellEditable(int r, int c) { return isAccionesColumn(c); }
                @Override public Class<?> getColumnClass(int c) { return columnClass(c); }
            };
            tabla.setModel(modelo);
            tabla.setRowHeight(30);
            tabla.setFillsViewportHeight(true);
            tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Buscar en vivo
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(modelo);
            tabla.setRowSorter(sorter);
            txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
                private void filtrar() {
                    String q = txtBuscar.getText().trim();
                    sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)"+ Pattern.quote(q)));
                }
                @Override public void insertUpdate(DocumentEvent e){ filtrar(); }
                @Override public void removeUpdate(DocumentEvent e){ filtrar(); }
                @Override public void changedUpdate(DocumentEvent e){ filtrar(); }
            });

            JScrollPane sp = new JScrollPane(tabla);
            sp.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
            add(sp, BorderLayout.CENTER);

            // Panel de edición (se añadirá en cada tab y se oculta por defecto)
            editorPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1,0,0,0,new Color(220,220,220)),
                    BorderFactory.createEmptyBorder(10,12,10,12)
            ));
            editorPanel.setVisible(false);
            add(editorPanel, BorderLayout.SOUTH);

            // Acciones
            btnRefrescar.addActionListener(e -> cargar());
            btnNuevo.addActionListener(e -> nuevo());

            // Carga inicial
            cargar();
        }

        /* Helpers comunes */
        protected int idFromViewRow(int viewRow){
            if (viewRow < 0) return -1;
            int modelRow = tabla.convertRowIndexToModel(viewRow);
            Object v = modelo.getValueAt(modelRow, 0);
            return (v instanceof Number)? ((Number)v).intValue() : -1;
        }
        protected void setWidths(int... widths) {
            TableColumnModel cols = tabla.getColumnModel();
            for (int i=0; i<widths.length && i<cols.getColumnCount(); i++)
                cols.getColumn(i).setPreferredWidth(widths[i]);
        }
        protected JPanel botonesGuardarCancelar(Runnable onGuardar, Runnable onCancelar){
            JButton b1 = new JButton("Guardar");
            JButton b2 = new JButton("Cancelar");
            b1.addActionListener(e -> onGuardar.run());
            b2.addActionListener(e -> onCancelar.run());
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
            p.add(b2); p.add(b1);
            return p;
        }
        protected static boolean emailValido(String s){ return s!=null && s.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"); }
        protected static boolean confirm(String msg){
            return JOptionPane.showConfirmDialog(null, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        protected static void showError(String donde, Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error en "+donde+": "+ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }

        /* Puntos de extensión */
        protected boolean isAccionesColumn(int c) { return false; }
        protected Class<?>  columnClass(int c)     { return String.class; }
        protected abstract void configurarColumnas();
        protected abstract void cargar();
        protected abstract void nuevo(); // pone editorPanel visible en modo "nuevo"
        protected abstract void editar(int id); // pone editorPanel visible en modo "editar"
        protected abstract void borrar(int id);
    }

    /* ========================= CLIENTES ========================= */
    class TabClientes extends CrudTab {
        private final JTextField eNombre = new JTextField(24);
        private final JTextField eEmail  = new JTextField(24);
        private final JSpinner   eEdad   = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));

        public TabClientes() {
            super(new String[]{"ID","Nombre","Email","Edad","Editar","Borrar"});
            configurarColumnas();
            construirEditor();
        }

        @Override protected boolean isAccionesColumn(int c){ return c >= 4; }
        @Override protected Class<?> columnClass(int c){ return (c==0||c==3)? Integer.class : String.class; }

        @Override protected void configurarColumnas() {
            setWidths(70, 300, 380, 70, 90, 90);
            TableColumnModel cols = tabla.getColumnModel();
            cols.getColumn(4).setCellRenderer(new BtnRenderer("Editar"));
            cols.getColumn(4).setCellEditor(new BtnEditor("Editar", row -> editar(idFromViewRow(row))));
            cols.getColumn(5).setCellRenderer(new BtnRenderer("Borrar"));
            cols.getColumn(5).setCellEditor(new BtnEditor("Borrar", row -> borrar(idFromViewRow(row))));
        }

        private void construirEditor() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4,4,4,4);
            gbc.anchor = GridBagConstraints.WEST;

            int y=0;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Nombre:"), gbc);
            gbc.gridx=1;             editorPanel.add(eNombre, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx=1;             editorPanel.add(eEmail,  gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Edad:"), gbc);
            gbc.gridx=1;             editorPanel.add(eEdad,   gbc); y++;

            JPanel botones = botonesGuardarCancelar(this::guardar, this::cancelar);
            gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
            editorPanel.add(botones, gbc);
        }

        @Override protected void cargar() {
            try {
                List<Object[]> filas = new ClientesDAO().listarTodos();
                modelo.setRowCount(0);
                for (Object[] f: filas) modelo.addRow(new Object[]{f[0],f[1],f[2],f[3],"Editar","Borrar"});
            } catch (Exception ex) { showError("listar clientes", ex); }
        }

        @Override protected void nuevo() {
            editId = null;
            eNombre.setText("");
            eEmail.setText("");
            eEdad.setValue(18);
            editorPanel.setVisible(true);
            eNombre.requestFocus();
            revalidate();
        }

        @Override protected void editar(int id) {
            if (id < 0) return;
            editId = id;
            int vr = tabla.getSelectedRow();
            if (vr < 0) { editorPanel.setVisible(false); return; }
            int mr = tabla.convertRowIndexToModel(vr);
            eNombre.setText(String.valueOf(modelo.getValueAt(mr, 1)));
            eEmail.setText(String.valueOf(modelo.getValueAt(mr, 2)));
            eEdad.setValue(modelo.getValueAt(mr, 3));
            editorPanel.setVisible(true);
            eNombre.requestFocus();
            revalidate();
        }

        private void guardar() {
            String nombre = eNombre.getText().trim();
            String email  = eEmail.getText().trim();
            int edad      = (Integer) eEdad.getValue();

            if (nombre.isBlank()) { JOptionPane.showMessageDialog(this,"Nombre requerido"); return; }
            if (!emailValido(email)) { JOptionPane.showMessageDialog(this,"Email no válido"); return; }
            if (edad<0 || edad>120) { JOptionPane.showMessageDialog(this,"Edad fuera de rango"); return; }

            try {
                ClientesDAO dao = new ClientesDAO();
                if (editId == null) {
                    // Necesitas en ClientesDAO:
                    // public void insertar(String nombre, String email, int edad) throws SQLException
                    dao.insertar(nombre, email, edad);
                } else {
                    // Necesitas en ClientesDAO:
                    // public int modificar(int id, String nombre, String email, int edad) throws SQLException
                    dao.modificar(editId, nombre, email, edad);
                }
                cancelar();
                cargar();
            } catch (Exception ex) { showError("guardar cliente", ex); }
        }

        private void cancelar() {
            editorPanel.setVisible(false);
            editId = null;
        }

        @Override protected void borrar(int id) {
            if (id < 0) return;
            if (!confirm("¿Borrar cliente ID "+id+"?")) return;
            try {
                int n = new ClientesDAO().borrar(id);
                if (n > 0) cargar();
            } catch (Exception ex) { showError("borrar cliente", ex); }
        }
    }

    /* ========================= DIRECCIONES ========================= */
    class TabDirecciones extends CrudTab {
        private final JSpinner  eIdCliente = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        private final JTextField eVia      = new JTextField(28);
        private final JTextField eCiudad   = new JTextField(18);
        private final JTextField eCP       = new JTextField(10);

        public TabDirecciones() {
            super(new String[]{"ID","ClienteID","Vía","Ciudad","CP","Editar","Borrar"});
            configurarColumnas();
            construirEditor();
        }

        @Override protected boolean isAccionesColumn(int c){ return c >= 5; }
        @Override protected Class<?> columnClass(int c){ return (c==0||c==1)? Integer.class : String.class; }

        @Override protected void configurarColumnas() {
            setWidths(70, 90, 360, 200, 90, 90, 90);
            TableColumnModel cols = tabla.getColumnModel();
            cols.getColumn(5).setCellRenderer(new BtnRenderer("Editar"));
            cols.getColumn(5).setCellEditor(new BtnEditor("Editar", row -> editar(idFromViewRow(row))));
            cols.getColumn(6).setCellRenderer(new BtnRenderer("Borrar"));
            cols.getColumn(6).setCellEditor(new BtnEditor("Borrar", row -> borrar(idFromViewRow(row))));
        }

        private void construirEditor() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4,4,4,4);
            gbc.anchor = GridBagConstraints.WEST;

            int y=0;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("ClienteID:"), gbc);
            gbc.gridx=1;             editorPanel.add(eIdCliente, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Vía:"), gbc);
            gbc.gridx=1;             editorPanel.add(eVia, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Ciudad:"), gbc);
            gbc.gridx=1;             editorPanel.add(eCiudad, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("CP:"), gbc);
            gbc.gridx=1;             editorPanel.add(eCP, gbc); y++;

            JPanel botones = botonesGuardarCancelar(this::guardar, this::cancelar);
            gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
            editorPanel.add(botones, gbc);
        }

        @Override protected void cargar() {
            try {
                List<Object[]> filas = new DireccionesDAO().listarTodos();
                modelo.setRowCount(0);
                for (Object[] f: filas) modelo.addRow(new Object[]{f[0],f[1],f[2],f[3],f[4],"Editar","Borrar"});
            } catch (Exception ex) { showError("listar direcciones", ex); }
        }

        @Override protected void nuevo() {
            editId = null;
            eIdCliente.setValue(1);
            eVia.setText("");
            eCiudad.setText("");
            eCP.setText("");
            editorPanel.setVisible(true);
            eVia.requestFocus();
            revalidate();
        }

        @Override protected void editar(int id) {
            if (id < 0) return;
            editId = id;
            int vr = tabla.getSelectedRow();
            if (vr < 0) { editorPanel.setVisible(false); return; }
            int mr = tabla.convertRowIndexToModel(vr);
            eIdCliente.setValue(modelo.getValueAt(mr, 1));
            eVia.setText(String.valueOf(modelo.getValueAt(mr, 2)));
            eCiudad.setText(String.valueOf(modelo.getValueAt(mr, 3)));
            eCP.setText(String.valueOf(modelo.getValueAt(mr, 4)));
            editorPanel.setVisible(true);
            eVia.requestFocus();
            revalidate();
        }

        private void guardar() {
            int    idCliente = (Integer) eIdCliente.getValue();
            String via       = eVia.getText().trim();
            String ciudad    = eCiudad.getText().trim();
            String cp        = eCP.getText().trim();

            if (via.isBlank() || ciudad.isBlank() || cp.isBlank()) {
                JOptionPane.showMessageDialog(this,"Todos los campos son obligatorios");
                return;
            }

            try {
                DireccionesDAO dao = new DireccionesDAO();
                if (editId == null) {
                    // Necesitas en DireccionesDAO:
                    // public void insertar(int idCliente, String via, String ciudad, String cp) throws SQLException
                    dao.insertar(idCliente, via, ciudad, cp);
                } else {
                    // Necesitas en DireccionesDAO:
                    // public int modificar(int id, int idCliente, String via, String ciudad, String cp) throws SQLException
                    dao.modificar(editId, idCliente, via, ciudad, cp);
                }
                cancelar();
                cargar();
            } catch (Exception ex) { showError("guardar dirección", ex); }
        }

        private void cancelar() {
            editorPanel.setVisible(false);
            editId = null;
        }

        @Override protected void borrar(int id) {
            if (id < 0) return;
            if (!confirm("¿Borrar dirección ID "+id+"?")) return;
            try {
                int n = new DireccionesDAO().borrar(id);
                if (n > 0) cargar();
            } catch (Exception ex) { showError("borrar dirección", ex); }
        }
    }

    /* ========================= CONTACTOS ========================= */
    class TabContactos extends CrudTab {
        private final JSpinner  eIdCliente = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        private final JComboBox<String> eTipo = new JComboBox<>(new String[]{"telefono","email","otro"});
        private final JTextField eValor = new JTextField(28);

        public TabContactos() {
            super(new String[]{"ID","ClienteID","Tipo","Valor","Editar","Borrar"});
            configurarColumnas();
            construirEditor();
        }

        @Override protected boolean isAccionesColumn(int c){ return c >= 4; }
        @Override protected Class<?> columnClass(int c){ return (c==0||c==1)? Integer.class : String.class; }

        @Override protected void configurarColumnas() {
            setWidths(70, 90, 120, 500, 90, 90);
            TableColumnModel cols = tabla.getColumnModel();
            cols.getColumn(4).setCellRenderer(new BtnRenderer("Editar"));
            cols.getColumn(4).setCellEditor(new BtnEditor("Editar", row -> editar(idFromViewRow(row))));
            cols.getColumn(5).setCellRenderer(new BtnRenderer("Borrar"));
            cols.getColumn(5).setCellEditor(new BtnEditor("Borrar", row -> borrar(idFromViewRow(row))));
        }

        private void construirEditor() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4,4,4,4);
            gbc.anchor = GridBagConstraints.WEST;

            int y=0;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("ClienteID:"), gbc);
            gbc.gridx=1;             editorPanel.add(eIdCliente, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Tipo:"), gbc);
            gbc.gridx=1;             editorPanel.add(eTipo, gbc); y++;
            gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Valor:"), gbc);
            gbc.gridx=1;             editorPanel.add(eValor, gbc); y++;

            JPanel botones = botonesGuardarCancelar(this::guardar, this::cancelar);
            gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
            editorPanel.add(botones, gbc);
        }

        @Override protected void cargar() {
            try {
                List<Object[]> filas = new ContactosDAO().listarTodos();
                modelo.setRowCount(0);
                for (Object[] f: filas) modelo.addRow(new Object[]{f[0],f[1],f[2],f[3],"Editar","Borrar"});
            } catch (Exception ex) { showError("listar contactos", ex); }
        }

        @Override protected void nuevo() {
            editId = null;
            eIdCliente.setValue(1);
            eTipo.setSelectedIndex(0);
            eValor.setText("");
            editorPanel.setVisible(true);
            eValor.requestFocus();
            revalidate();
        }

        @Override protected void editar(int id) {
            if (id < 0) return;
            editId = id;
            int vr = tabla.getSelectedRow();
            if (vr < 0) { editorPanel.setVisible(false); return; }
            int mr = tabla.convertRowIndexToModel(vr);
            eIdCliente.setValue(modelo.getValueAt(mr, 1));
            eTipo.setSelectedItem(String.valueOf(modelo.getValueAt(mr, 2)));
            eValor.setText(String.valueOf(modelo.getValueAt(mr, 3)));
            editorPanel.setVisible(true);
            eValor.requestFocus();
            revalidate();
        }

        private void guardar() {
            int    idCliente = (Integer) eIdCliente.getValue();
            String tipo      = (String) eTipo.getSelectedItem();
            String valor     = eValor.getText().trim();

            if (valor.isBlank()) { JOptionPane.showMessageDialog(this,"Valor requerido"); return; }
            if ("email".equals(tipo) && !emailValido(valor)) {
                JOptionPane.showMessageDialog(this,"Email de contacto no válido"); return;
            }

            try {
                ContactosDAO dao = new ContactosDAO();
                if (editId == null) {
                    // Necesitas en ContactosDAO:
                    // public void insertar(int idCliente, String tipo, String valor) throws SQLException
                    dao.insertar(idCliente, tipo, valor);
                } else {
                    // Necesitas en ContactosDAO:
                    // public int modificar(int id, int idCliente, String tipo, String valor) throws SQLException
                    dao.modificar(editId, idCliente, tipo, valor);
                }
                cancelar();
                cargar();
            } catch (Exception ex) { showError("guardar contacto", ex); }
        }

        private void cancelar() {
            editorPanel.setVisible(false);
            editId = null;
        }

        @Override protected void borrar(int id) {
            if (id < 0) return;
            if (!confirm("¿Borrar contacto ID "+id+"?")) return;
            try {
                int n = new ContactosDAO().borrar(id);
                if (n > 0) cargar();
            } catch (Exception ex) { showError("borrar contacto", ex); }
        }
    }

    /* =================== RENDER / EDITOR DE BOTONES =================== */
    static class BtnRenderer extends JButton implements TableCellRenderer {
        public BtnRenderer(String text){ setText(text); setFocusable(false); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v==null? "" : v.toString());
            return this;
        }
    }
    static class BtnEditor extends AbstractCellEditor implements TableCellEditor {
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
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.table = t;
            btn.setText(v==null? "" : v.toString());
            return btn;
        }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }

    /* =============================== MAIN =============================== */
    public static void main(String[] args) {
        // Nimbus opcional
        try { for (UIManager.LookAndFeelInfo i: UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }
}
