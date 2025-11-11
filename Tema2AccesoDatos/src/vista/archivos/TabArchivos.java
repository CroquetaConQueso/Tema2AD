package vista.archivos;

import dao.ArchivosDAO;
import vista.base.CrudTab;
import vista.validacion.UiFeedback;
import vista.validacion.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TabArchivos extends CrudTab {

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    private JSpinner eIdCliente;
    private JTextField eNombre;
    private JTextField eMime;
    private JLabel eArchivoSel;
    private byte[] eBytes; // contenido del archivo seleccionado

    public TabArchivos() { super(); }

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
        tabla.setModel(modelo);
        setColumnWidths(50,70,90,280,150,120,140,90,90,90);
        TableColumnModel cols = tabla.getColumnModel();
        cols.getColumn(7).setCellRenderer(new BtnRenderer("Detalles"));
        cols.getColumn(7).setCellEditor  (new BtnEditor  ("Detalles", row -> { Integer id=idFromViewRow(row); if (id!=null) verDetalles(id); }));
        cols.getColumn(8).setCellRenderer(new BtnRenderer("Editar"));
        cols.getColumn(8).setCellEditor  (new BtnEditor  ("Editar",   row -> { Integer id=idFromViewRow(row); if (id!=null) editar(id);   }));
        cols.getColumn(9).setCellRenderer(new BtnRenderer("Borrar"));
        cols.getColumn(9).setCellEditor  (new BtnEditor  ("Borrar",   row -> { Integer id=idFromViewRow(row); if (id!=null) borrar(id);   }));
    }

    @Override
    protected void configurarEditor() {
        editorPanel.removeAll();

        if (eIdCliente == null) eIdCliente = new JSpinner(new SpinnerNumberModel(1,1,1_000_000,1));
        if (eNombre    == null) eNombre    = new JTextField(28);
        if (eMime      == null) eMime      = new JTextField(18);
        if (eArchivoSel== null) eArchivoSel= new JLabel("(ningún archivo)");

        JButton btnElegir   = new JButton("Elegir archivo…");
        JButton btnGuardar  = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnElegir.addActionListener(e -> elegirArchivo());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4); gbc.anchor = GridBagConstraints.WEST;
        int y=0;
        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("ClienteID:"), gbc);
        gbc.gridx=1;             editorPanel.add(eIdCliente, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx=1;             editorPanel.add(eNombre, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Mime:"), gbc);
        gbc.gridx=1;             editorPanel.add(eMime, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Archivo:"), gbc);
        gbc.gridx=1;             editorPanel.add(eArchivoSel, gbc);
        gbc.gridx=2;             editorPanel.add(btnElegir, gbc); y++;

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        pnlBtns.add(btnCancelar); pnlBtns.add(btnGuardar);
        gbc.gridx=0; gbc.gridy=++y; gbc.gridwidth=3; gbc.fill=GridBagConstraints.HORIZONTAL;
        editorPanel.add(pnlBtns, gbc);

        editorPanel.setVisible(false);
    }

    @Override
    protected void cargar() {
        try {
            List<Object[]> filas = new ArchivosDAO().listarTodos();
            modelo.setRowCount(0);
            for (Object[] f : filas) {
                // Esperado: [id, id_cliente, nombre, mime, tamano, subido_en]
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
        UiFeedback.clearAll(eNombre, eMime);
        editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
    }

    @Override
    protected void editar(int id) {
        try {
            Object[] a = new ArchivosDAO().getById(id);
            if (a == null) return;
            // [id, id_cliente, nombre, mime, tamano, subido_en]
            editId = (Integer) a[0];
            eIdCliente.setValue((Integer) a[1]);
            eNombre.setText(String.valueOf(a[2]));
            eMime.setText(String.valueOf(a[3]));
            eBytes = null; // solo si el usuario selecciona un archivo nuevo
            eArchivoSel.setText("(sin cambios)");
            UiFeedback.clearAll(eNombre, eMime);
            editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
        } catch (Exception ex) { showError("cargar archivo (editar)", ex); }
    }

    @Override
    protected void verDetalles(int id) {
        try {
            Object[] a = new ArchivosDAO().getById(id);
            if (a == null) { JOptionPane.showMessageDialog(this,"No existe el archivo."); return; }
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
            if (eMime.getText().isBlank()) {
                String guessed = Files.probeContentType(f.toPath());
                eMime.setText(guessed != null ? guessed : "application/octet-stream");
            }
            eArchivoSel.setText(f.getName() + " (" + eBytes.length + " B)");
        } catch (Exception ex) { showError("leer archivo", ex); }
    }

    private void guardar() {
        int    idCliente = (Integer) eIdCliente.getValue();
        String nombre    = eNombre.getText().trim();
        String mime      = eMime.getText().trim();

        UiFeedback.clearAll(eNombre, eMime);
        List<String> errores = new ArrayList<>();

        if (!Validator.lengthBetween(nombre, 1, 150)) {
            errores.add("Nombre: obligatorio, máximo 150");
            UiFeedback.markError(eNombre, "Obligatorio");
        }
        if (!Validator.mime(mime)) {
            errores.add("MIME: formato inválido (p.ej. image/png, application/pdf)");
            UiFeedback.markError(eMime, "tipo/subtipo");
        }
        if (editId == null) {
            if (eBytes == null || eBytes.length == 0) {
                errores.add("Archivo: obligatorio en altas");
            } else if (!Validator.bytesMax(eBytes, MAX_BYTES)) {
                errores.add("Archivo: tamaño máximo 10 MB");
            }
        } else {
            if (eBytes != null && !Validator.bytesMax(eBytes, MAX_BYTES)) {
                errores.add("Archivo: tamaño máximo 10 MB");
            }
        }
        if (idCliente <= 0) errores.add("ClienteID: debe ser mayor que 0");

        if (!errores.isEmpty()) {
            UiFeedback.showErrors(this, errores);
            return;
        }

        try {
            ArchivosDAO dao = new ArchivosDAO();
            if (editId == null) {
                dao.insertar(idCliente, nombre, mime, eBytes);
            } else {
                // si eBytes==null ⇒ el DAO mantiene el binario anterior
                dao.modificar(editId, idCliente, nombre, mime, eBytes);
            }
            cancelar(); cargar();
        } catch (Exception ex) { showError("guardar archivo", ex); }
    }

    private void cancelar(){ editorPanel.setVisible(false); editId = null; eBytes = null; eArchivoSel.setText("(ningún archivo)"); }

    @Override
    protected void borrar(int id) {
        if (!confirm("¿Borrar archivo ID "+id+"?")) return;
        try { new ArchivosDAO().borrar(id); cargar(); }
        catch (Exception ex){ showError("borrar archivo", ex); }
    }
}
