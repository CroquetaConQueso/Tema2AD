package vista.base;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class CrudTab extends JPanel {
    protected final JLabel lblBuscar = new JLabel("Buscar:");
    protected final JComboBox<String> cbCampo = new JComboBox<>();
    protected final JTextField txtBuscar = new JTextField(18);
    protected final JButton btnRefrescar = new JButton("Refrescar");
    protected final JButton btnNuevo = new JButton("Nuevo");
    protected final JButton btnBorrarSel = new JButton("Borrar seleccionados");
    protected final JButton btnCsv = new JButton("Export CSV");
    protected final JButton btnJson = new JButton("Export JSON");
    protected final JButton btnExportBin = new JButton("Export binarios");
    protected final JTable tabla = new JTable();
    protected DefaultTableModel modelo;
    protected TableRowSorter<TableModel> sorter;
    protected final JPanel editorPanel = new JPanel(new GridBagLayout());
    protected Integer editId = null;

    public CrudTab() {
        setLayout(new BorderLayout(0, 0));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.add(lblBuscar);
        izq.add(cbCampo);
        izq.add(txtBuscar);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnExportBin.setVisible(false);
        der.add(btnExportBin);
        der.add(btnCsv);
        der.add(btnJson);
        der.add(btnBorrarSel);
        der.add(btnRefrescar);
        der.add(btnNuevo);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.add(izq, BorderLayout.WEST);
        top.add(der, BorderLayout.EAST);
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(top, BorderLayout.NORTH);

        construirModelo();
        tabla.setModel(modelo);
        tabla.setRowHeight(30);
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            void filtrar() {
                String q = txtBuscar.getText().trim();
                int campo = cbCampo.getSelectedIndex();
                aplicarFiltro(q, campo);
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        alInstalarModelo();

        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(sp, BorderLayout.CENTER);

        editorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        editorPanel.setVisible(false);
        add(editorPanel, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> cargar());
        btnNuevo.addActionListener(e -> nuevo());
        btnBorrarSel.addActionListener(this::borrarSeleccionados);
        btnCsv.addActionListener(e -> exportCSV());
        btnJson.addActionListener(e -> exportJSON());

        cargar();
    }

    protected abstract void construirModelo();
    protected abstract void configurarEditor();
    protected abstract void cargar();
    protected abstract void nuevo();
    protected abstract void editar(int id);
    protected abstract void borrar(int id);
    protected abstract void verDetalles(int id);

    protected void alInstalarModelo() {}

    protected void setColumnWidths(int... widths) {
        TableColumnModel cols = tabla.getColumnModel();
        for (int i = 0; i < widths.length && i < cols.getColumnCount(); i++) {
            cols.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    protected Integer idFromViewRow(int viewRow) {
        if (viewRow < 0) return null;
        int modelRow = tabla.convertRowIndexToModel(viewRow);
        Object v = modelo.getValueAt(modelRow, 1);
        return (v instanceof Number) ? ((Number) v).intValue() : null;
    }

    protected void aplicarFiltro(String q, int idxCampo) {
        if (q == null || q.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        String regex = "(?i)" + Pattern.quote(q.trim());
        if (idxCampo <= 0) {
            sorter.setRowFilter(RowFilter.regexFilter(regex));
            return;
        }
        int modelIndex = campoAColumnaModelo(idxCampo);
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
                Object val = entry.getValue(modelIndex);
                return val != null && val.toString().matches(regex);
            }
        });
    }

    protected int campoAColumnaModelo(int idxCampo) {
        return idxCampo;
    }

    protected static boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(null, msg, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    protected static void showError(String donde, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error en " + donde + ": " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected static boolean emailValido(String s) {
        return s != null && s.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    protected void borrarSeleccionados(ActionEvent e) {
        List<Integer> ids = new ArrayList<>();
        for (int viewRow = 0; viewRow < tabla.getRowCount(); viewRow++) {
            int modelRow = tabla.convertRowIndexToModel(viewRow);
            Object sel = modelo.getValueAt(modelRow, 0);
            if (Boolean.TRUE.equals(sel)) {
                Object idObj = modelo.getValueAt(modelRow, 1);
                if (idObj instanceof Number) ids.add(((Number) idObj).intValue());
            }
        }
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay filas seleccionadas.");
            return;
        }
        if (!confirm("¿Borrar " + ids.size() + " registro(s) seleccionados?")) return;
        for (Integer id : ids) {
            borrar(id);
        }
        cargar();
    }

    protected void exportCSV() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new java.io.File("export.csv"));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File f = ch.getSelectedFile();
        try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
            for (int c = 0; c < tabla.getColumnCount(); c++) {
                String name = tabla.getColumnName(c);
                if (esColAccion(c)) continue;
                fw.write(escapeCsv(name));
                if (c < tabla.getColumnCount() - 1) fw.write(",");
            }
            fw.write("\n");
            for (int vrow = 0; vrow < tabla.getRowCount(); vrow++) {
                for (int c = 0; c < tabla.getColumnCount(); c++) {
                    if (esColAccion(c)) continue;
                    Object val = tabla.getValueAt(vrow, c);
                    fw.write(escapeCsv(val == null ? "" : val.toString()));
                    if (c < tabla.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Exportado CSV: " + f.getAbsolutePath());
        } catch (Exception ex) { showError("export CSV", ex); }
    }

    protected void exportJSON() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new java.io.File("export.json"));
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File f = ch.getSelectedFile();
        try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
            fw.write("[\n");
            for (int vrow = 0; vrow < tabla.getRowCount(); vrow++) {
                fw.write("  {");
                int written = 0;
                for (int c = 0; c < tabla.getColumnCount(); c++) {
                    if (esColAccion(c)) continue;
                    String key = tabla.getColumnName(c);
                    Object val = tabla.getValueAt(vrow, c);
                    if (written++ > 0) fw.write(", ");
                    fw.write("\"" + key + "\": " + jsonValue(val));
                }
                fw.write("}");
                if (vrow < tabla.getRowCount() - 1) fw.write(",");
                fw.write("\n");
            }
            fw.write("]\n");
            JOptionPane.showMessageDialog(this, "Exportado JSON: " + f.getAbsolutePath());
        } catch (Exception ex) { showError("export JSON", ex); }
    }

    private boolean esColAccion(int viewCol) {
        int last = tabla.getColumnCount();
        return (viewCol >= last - 3);
    }

    private String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String jsonValue(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        String s = v.toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + s + "\"";
    }

    public static class BtnRenderer extends JButton implements TableCellRenderer {
        public BtnRenderer(String text) { setText(text); setFocusable(false); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v == null ? "" : v.toString()); return this;
        }
    }

    public static class BtnEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private JTable table;
        private final java.util.function.IntConsumer onClick;
        public BtnEditor(String text, java.util.function.IntConsumer onClick) {
            this.onClick = onClick; btn.setText(text); btn.addActionListener(this::handle);
        }
        private void handle(ActionEvent e) {
            int row = table.getEditingRow();
            fireEditingStopped();
            onClick.accept(row);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.table = t; btn.setText(v == null ? "" : v.toString()); return btn;
        }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }
}

