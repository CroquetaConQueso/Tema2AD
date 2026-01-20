package app;

import dao.RegistrosDAO;
import model.Registro;
import util.ExportUtils;
import util.UiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class FrmInicio extends JFrame {

    // FILA 1 (búsqueda)
    private final JComboBox<String> cbCampo = new JComboBox<>(new String[]{"ID","Nombre","Fecha","Cantidad"});
    private final JTextField tfValor = new JTextField(22);
    private final JButton btnBuscar = UiUtils.btnPrimary("Buscar");
    private final JButton btnLimpiar = UiUtils.btnLight("Limpiar");

    // FILA 2 (acciones)
    private final JButton btnNuevo = UiUtils.btnAccent("Nuevo");
    private final JButton btnModificar = UiUtils.btnAccent("Modificar");
    private final JButton btnEliminar = UiUtils.btnDanger("Eliminar");
    private final JLabel sep = new JLabel("  |  ");
    private final JButton btnMostrarVarios = UiUtils.btnLight("Mostrar varios");
    private final JButton btnEliminarVarios = UiUtils.btnDanger("Eliminar varios");
    private final JButton btnExportCsv = UiUtils.btnLight("CSV");
    private final JButton btnExportPdf = UiUtils.btnLight("PDF");

    // Formulario compacto (abajo)
    private final JTextField tfNombre = new JTextField(16);
    private final JFormattedTextField tfFecha = UiUtils.dateFieldEU(); // dd/MM/yyyy
    private final JButton btnHoy = UiUtils.btnLight("Hoy");
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(0, -1_000_000, 1_000_000, 1));
    private final JButton btnGuardar = UiUtils.btnPrimary("Guardar");
    private final JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT));

    // Tabla
    private final JTable tabla = new JTable();

    // DAO
    private final RegistrosDAO dao = new RegistrosDAO();

    // debounce búsqueda
    private final Timer debounce = new Timer(250, e -> buscar());

    public FrmInicio() {
        super("test1 — Mini CRUD (Swing + JDBC)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 620));
        setLocationRelativeTo(null);
        UiUtils.installTheme(); // colores + Nimbus

        // ===== Top bars =====
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.setBackground(UiUtils.COL_BG);
        row1.add(new JLabel("Campo:"));
        row1.add(cbCampo);
        row1.add(new JLabel("Valor:"));
        row1.add(tfValor);
        row1.add(btnBuscar);
        row1.add(btnLimpiar);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setBackground(UiUtils.COL_BG);
        row2.add(btnNuevo);
        row2.add(btnModificar);
        row2.add(btnEliminar);
        row2.add(sep);
        row2.add(btnMostrarVarios);
        row2.add(btnEliminarVarios);
        row2.add(new JLabel("   Exportar:"));
        row2.add(btnExportCsv);
        row2.add(btnExportPdf);

        JPanel top = new JPanel(new BorderLayout());
        top.add(row1, BorderLayout.NORTH);
        top.add(row2, BorderLayout.SOUTH);

        // ===== Formulario =====
        pnlForm.setBackground(Color.white);
        pnlForm.add(new JLabel("Nombre:"));
        pnlForm.add(tfNombre);
        pnlForm.add(new JLabel("Fecha (dd/MM/yyyy):"));
        pnlForm.add(tfFecha);
        pnlForm.add(btnHoy);
        pnlForm.add(new JLabel("Cantidad:"));
        pnlForm.add(spCantidad);
        pnlForm.add(btnGuardar);

        // ===== Tabla =====
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(tabla);

        // ===== Layout principal =====
        JPanel center = new JPanel(new BorderLayout());
        center.add(pnlForm, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        setLayout(new BorderLayout(0, 6));
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // ===== Menú de estilos opcional =====
        setJMenuBar(UiUtils.simpleStyleMenu(this));

        // ===== Eventos =====
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> { tfValor.setText(""); buscar(); });

        tfValor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { debounce.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { debounce.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { debounce.restart(); }
        });

        btnHoy.addActionListener(e -> UiUtils.setToday(tfFecha));
        btnNuevo.addActionListener(e -> {
            tabla.clearSelection();
            llenarFormulario(null, null, 0);
            tfNombre.requestFocusInWindow();
        });

        btnModificar.addActionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r < 0) { UiUtils.info(this, "Selecciona una fila para modificar."); return; }
            llenarFormulario(
                    (String) tabla.getValueAt(r, 1),
                    UiUtils.toDateEU(tabla.getValueAt(r, 2)),
                    (Integer) tabla.getValueAt(r, 3)
            );
        });

        btnEliminar.addActionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r < 0) { UiUtils.info(this, "Selecciona una fila para eliminar."); return; }
            if (!UiUtils.confirm(this, "¿Eliminar el registro seleccionado?")) return;
            Integer id = (Integer) tabla.getValueAt(r, 0);
            dao.eliminar(id);
            buscar();
        });

        btnMostrarVarios.addActionListener(e -> UiUtils.showSelectionDialog(this, tabla));
        btnEliminarVarios.addActionListener(e -> eliminarVarios());

        btnGuardar.addActionListener(e -> guardar());

        btnExportCsv.addActionListener(e -> util.ExportUtils.exportCsv(this, tabla));
        btnExportPdf.addActionListener(e -> util.ExportUtils.printTableAsPdf(this, tabla));

        // ===== Carga inicial =====
        buscar();
        pack(); // acomoda todo para que no quede oculto
    }

    private void llenarFormulario(String nombre, String fechaEU, Integer cantidad) {
        tfNombre.setText(nombre == null ? "" : nombre);
        tfFecha.setText(fechaEU == null ? "" : fechaEU);
        spCantidad.setValue(cantidad == null ? 0 : cantidad);
    }

    private void buscar() {
        String campo = (String) cbCampo.getSelectedItem();
        String valor = tfValor.getText().trim();

        // SwingWorker para UI fluida
        new SwingWorker<List<Registro>, Void>() {
            @Override protected List<Registro> doInBackground() {
                return dao.buscar(campo, valor, 500, 0);
            }
            @Override protected void done() {
                try { tabla.setModel(makeModel(get())); }
                catch (Exception ex) { UiUtils.error(FrmInicio.this, ex.getMessage()); }
            }
        }.execute();
    }

    private void guardar() {
        try {
            String nombre = tfNombre.getText().trim();
            LocalDate fecha = UiUtils.parseEU(tfFecha.getText());
            Integer cantidad = (Integer) spCantidad.getValue();

            UiUtils.check(!nombre.isBlank(), "Nombre obligatorio.");
            UiUtils.check(fecha != null, "Fecha inválida (usa dd/MM/yyyy).");

            int[] rows = tabla.getSelectedRows();
            if (rows.length <= 1) {
                Integer id = (rows.length == 1) ? (Integer) tabla.getValueAt(rows[0], 0) : null;
                model.Registro r = new model.Registro(id, nombre, fecha, cantidad);
                if (id == null) dao.insertar(r); else dao.actualizar(r);
            } else {
                for (int row : rows) {
                    Integer id = (Integer) tabla.getValueAt(row, 0);
                    model.Registro r = new model.Registro(id,
                            nombre.isBlank() ? null : nombre, fecha, cantidad);
                    dao.actualizarParcial(r);
                }
            }
            buscar();
            UiUtils.info(this, "Guardado OK.");
        } catch (Exception ex) {
            UiUtils.error(this, "Error: " + ex.getMessage());
        }
    }

    private void eliminarVarios() {
        int[] rows = tabla.getSelectedRows();
        if (rows.length == 0) { UiUtils.info(this, "No hay selección."); return; }
        if (!UiUtils.confirm(this, "¿Eliminar " + rows.length + " registros?")) return;
        int count = 0;
        for (int r : rows) count += dao.eliminar((Integer) tabla.getValueAt(r, 0));
        UiUtils.info(this, "Eliminados: " + count);
        buscar();
    }

    private DefaultTableModel makeModel(List<Registro> list) {
        String[] cols = {"ID","Nombre","Fecha","Cantidad"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0,3 -> Integer.class;
                    default -> String.class; // Fecha se muestra en dd/MM/yyyy como String
                };
            }
        };
        for (Registro r : list) {
            m.addRow(new Object[]{
                    r.getId(),
                    r.getNombre(),
                    UiUtils.toDateEU(r.getFecha()),
                    r.getCantidad()
            });
        }
        return m;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }
}
