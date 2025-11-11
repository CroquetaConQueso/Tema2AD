package vista.archivos;

import dao.ArchivosDAO;
import vista.base.CrudTab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TabArchivos extends CrudTab {

    private JSpinner eIdCliente;
    private JTextField eNombre;
    private JTextField eMime;
    private JLabel eArchivoSel;
    private byte[] eBytes;

    public TabArchivos() {
        super();
        btnExportBin.setVisible(true);
        btnExportBin.addActionListener(e -> exportarBinarios());
    }

    @Override
    protected void construirModelo() {
        modelo = new DefaultTableModel(new Object[]{
                "✓", "ID", "ClienteID", "Nombre", "Mime", "Tamaño(B)", "Subido", "Detalles", "Editar", "Borrar"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c >= 7; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Boolean.class;
                    case 1, 2, 5 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        cbCampo.removeAllItems();
        cbCampo.addItem("Todos");
        cbCampo.addItem("ID");
        cbCampo.addItem("ClienteID");
        cbCampo.addItem("Nombre");
        cbCampo.addItem("Mime");
        cbCampo.addItem("Tamaño(B)");
        cbCampo.addItem("Subido");
        configurarEditor();
    }

    @Override
    protected void alInstalarModelo() {
        setColumnWidths(50, 70, 90, 280, 150, 120, 140, 90, 90, 90);
        TableColumnModel cols = tabla.getColumnModel();
        cols.getColumn(7).setCellRenderer(new CrudTab.BtnRenderer("Detalles"));
        cols.getColumn(7).setCellEditor(new CrudTab.BtnEditor("Detalles", row -> { Integer id = idFromViewRow(row); if (id != null) verDetalles(id); }));
        cols.getColumn(8).setCellRenderer(new CrudTab.BtnRenderer("Editar"));
        cols.getColumn(8).setCellEditor(new CrudTab.BtnEditor("Editar", row -> { Integer id = idFromViewRow(row); if (id != null) editar(id); }));
        cols.getColumn(9).setCellRenderer(new CrudTab.BtnRenderer("Borrar"));
        cols.getColumn(9).setCellEditor(new CrudTab.BtnEditor("Borrar", row -> { Integer id = idFromViewRow(row); if (id != null) borrar(id); }));
    }

    @Override
    protected void configurarEditor() {
        editorPanel.removeAll();
        if (eIdCliente == null) eIdCliente = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        if (eNombre == null) eNombre = new JTextField(28);
        if (eMime == null) eMime = new JTextField(18);
        if (eArchivoSel == null) eArchivoSel = new JLabel("(ningún archivo)");

        JButton btnElegir = new JButton("Elegir archivo…");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnElegir.addActionListener(e -> elegirArchivo());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); gbc.anchor = GridBagConstraints.WEST;
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; editorPanel.add(new JLabel("ClienteID:"), gbc);
        gbc.gridx = 1;             editorPanel.add(eIdCliente, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; editorPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;             editorPanel.add(eNombre, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; editorPanel.add(new JLabel("Mime:"), gbc);
        gbc.gridx = 1;             editorPanel.add(eMime, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; editorPanel.add(new JLabel("Archivo:"), gbc);
        gbc.gridx = 1;             editorPanel.add(eArchivoSel, gbc);
        gbc.gridx = 2;             editorPanel.add(btnElegir, gbc); y++;

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtns.add(btnCancelar); pnlBtns.add(btnGuardar);
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        editorPanel.add(pnlBtns, gbc);

        editorPanel.setVisible(false);
    }

    @Override
    protected void cargar() {
        try {
            List<Object[]> filas = new ArchivosDAO().listarTodos();
            modelo.setRowCount(0);
            for (Object[] f : filas) {
                modelo.addRow(new Object[]{false, f[0], f[1], f[2], f[3], f[4], f[5], "Detalles", "Editar", "Borrar"});
            }
        } catch (Exception ex) { showError("listar archivos", ex); }
    }

    @Override
    protected void nuevo() {
        editId = null;
        eIdCliente.setValue(1);
        eNombre.setText("");
        eMime.setText("");
        eBytes = null;
        eArchivoSel.setText("(ningún archivo)");
        editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
    }

    @Override
    protected void editar(int id) {
        try {
            Object[] a = new ArchivosDAO().getById(id);
            if (a == null) return;
            editId = (Integer) a[0];
            eIdCliente.setValue((Integer) a[1]);
            eNombre.setText(String.valueOf(a[2]));
            eMime.setText(String.valueOf(a[3]));
            eBytes = null;
            eArchivoSel.setText("(sin cambios)");
            editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
        } catch (Exception ex) { showError("cargar archivo (editar)", ex); }
    }

    @Override
    protected void verDetalles(int id) {
        try {
            Object[] a = new ArchivosDAO().getById(id);
            if (a == null) { JOptionPane.showMessageDialog(this, "No existe el archivo."); return; }
            String msg = """
                    Detalles del archivo
                    --------------------
                    ID:        %s
                    ClienteID: %s
                    Nombre:    %s
                    Mime:      %s
                    Tamaño(B): %s
                    Subido:    %s
                    """.formatted(a[0], a[1], a[2], a[3], a[4], a[5]);
            JOptionPane.showMessageDialog(this, msg, "Detalles", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError("detalles archivo", ex); }
    }

    private void elegirArchivo() {
        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = ch.getSelectedFile();
        try {
            eBytes = Files.readAllBytes(f.toPath());
            if (eNombre.getText().isBlank()) eNombre.setText(f.getName());
            if (eMime.getText().isBlank()) eMime.setText(Files.probeContentType(f.toPath()));
            eArchivoSel.setText(f.getName() + " (" + eBytes.length + " B)");
        } catch (Exception ex) { showError("leer archivo", ex); }
    }

    private void guardar() {
        int idCliente = (Integer) eIdCliente.getValue();
        String nombre = eNombre.getText().trim();
        String mime = eMime.getText().trim();
        if (nombre.isBlank()) { JOptionPane.showMessageDialog(this, "Nombre requerido"); return; }
        if (editId == null && (eBytes == null || eBytes.length == 0)) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo"); return;
        }
        try {
            ArchivosDAO dao = new ArchivosDAO();
            if (editId == null) {
                dao.insertar(idCliente, nombre, mime, eBytes);
            } else {
                dao.modificar(editId, idCliente, nombre, mime, eBytes);
            }
            cancelar(); cargar();
        } catch (Exception ex) { showError("guardar archivo", ex); }
    }

    private void cancelar() {
        editorPanel.setVisible(false); editId = null; eBytes = null; eArchivoSel.setText("(ningún archivo)");
    }

    @Override
    protected void borrar(int id) {
        if (!confirm("¿Borrar archivo ID " + id + "?")) return;
        try { new ArchivosDAO().borrar(id); cargar(); }
        catch (Exception ex) { showError("borrar archivo", ex); }
    }

    private void exportarBinarios() {
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
        Object choice = JOptionPane.showInputDialog(
                this,
                "¿Cómo quieres exportar los binarios?",
                "Exportar binarios",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Archivo original", "Como .dat"},
                "Archivo original"
        );
        if (choice == null) return;
        boolean comoDat = choice.toString().toLowerCase().contains(".dat");

        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ch.setDialogTitle("Selecciona carpeta de destino");
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dir = ch.getSelectedFile();

        ArchivosDAO dao = new ArchivosDAO();
        int ok = 0, fail = 0;

        for (int id : ids) {
            try {
                int modelRow = -1;
                for (int r = 0; r < modelo.getRowCount(); r++) {
                    Object v = modelo.getValueAt(r, 1);
                    if (v instanceof Number && ((Number) v).intValue() == id) { modelRow = r; break; }
                }
                String nombre = (modelRow >= 0 && modelo.getValueAt(modelRow, 3) != null)
                        ? modelo.getValueAt(modelRow, 3).toString() : ("archivo_" + id);
                String mime = (modelRow >= 0 && modelo.getValueAt(modelRow, 4) != null)
                        ? modelo.getValueAt(modelRow, 4).toString() : null;

                byte[] bytes = dao.getBytesById(id);
                if (bytes == null || bytes.length == 0) throw new RuntimeException("BLOB vacío");

                String outName;
                if (comoDat) {
                    outName = sanitizarBase(nombre) + "_" + id + ".dat";
                } else {
                    String ext = extensionPorMimeONombre(mime, nombre);
                    outName = aseguradoConExtension(nombre, ext, id);
                }
                File out = new File(dir, outName);
                Files.write(out.toPath(), bytes);
                ok++;
            } catch (Exception ex) {
                fail++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Exportación completada.\nCorrectos: " + ok + "\nFallidos: " + fail,
                "Exportar binarios", (fail == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE));
    }

    private static String extensionPorMimeONombre(String mime, String nombre) {
        if (nombre != null && nombre.contains(".")) {
            String extFromName = nombre.substring(nombre.lastIndexOf('.')).toLowerCase();
            if (extFromName.matches("\\.[a-z0-9]{1,6}")) return extFromName;
        }
        if (mime == null) return ".bin";
        String m = mime.toLowerCase();
        if (m.contains("png")) return ".png";
        if (m.contains("jpeg") || m.contains("jpg")) return ".jpg";
        if (m.contains("gif")) return ".gif";
        if (m.contains("bmp")) return ".bmp";
        if (m.contains("webp")) return ".webp";
        if (m.contains("pdf")) return ".pdf";
        return ".bin";
    }

    private static String aseguradoConExtension(String nombre, String ext, int id) {
        String base = (nombre == null || nombre.isBlank()) ? ("archivo_" + id) : nombre;
        base = sanitizarBase(base);
        if (!base.toLowerCase().endsWith(ext)) base += ext;
        return base;
    }

    private static String sanitizarBase(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }
}
