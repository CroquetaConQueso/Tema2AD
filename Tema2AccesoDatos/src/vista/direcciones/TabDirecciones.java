package vista.direcciones;

import dao.DireccionesDAO;
import vista.base.CrudTab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class TabDirecciones extends CrudTab {

    private JSpinner eIdCliente;
    private JTextField eVia;
    private JTextField eCiudad;
    private JTextField eCP;

    public TabDirecciones() { super(); }

    @Override
    protected void construirModelo() {
        modelo = new DefaultTableModel(new Object[]{
                "✓", "ID", "ClienteID", "Vía", "Ciudad", "CP", "Detalles", "Editar", "Borrar"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c >= 6; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Boolean.class;
                    case 1, 2 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        cbCampo.removeAllItems();
        cbCampo.addItem("Todos");
        cbCampo.addItem("ID");
        cbCampo.addItem("ClienteID");
        cbCampo.addItem("Vía");
        cbCampo.addItem("Ciudad");
        cbCampo.addItem("CP");
        configurarEditor();
    }

    @Override
    protected void alInstalarModelo() {
        setColumnWidths(50, 70, 90, 300, 220, 100, 100, 100, 100);
        TableColumnModel cols = tabla.getColumnModel();
        cols.getColumn(6).setCellRenderer(new BtnRenderer("Detalles"));
        cols.getColumn(6).setCellEditor  (new BtnEditor  ("Detalles", row -> { Integer id = idFromViewRow(row); if (id != null) verDetalles(id); }));
        cols.getColumn(7).setCellRenderer(new BtnRenderer("Editar"));
        cols.getColumn(7).setCellEditor  (new BtnEditor  ("Editar",   row -> { Integer id = idFromViewRow(row); if (id != null) editar(id); }));
        cols.getColumn(8).setCellRenderer(new BtnRenderer("Borrar"));
        cols.getColumn(8).setCellEditor  (new BtnEditor  ("Borrar",   row -> { Integer id = idFromViewRow(row); if (id != null) borrar(id); }));
    }

    @Override
    protected void configurarEditor() {
        editorPanel.removeAll();
        if (eIdCliente == null) eIdCliente = new JSpinner(new SpinnerNumberModel(1,1,1_000_000,1));
        if (eVia == null) eVia = new JTextField(28);
        if (eCiudad == null) eCiudad = new JTextField(18);
        if (eCP == null) eCP = new JTextField(10);

        JButton btnGuardar  = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4); gbc.anchor = GridBagConstraints.WEST;
        int y=0;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("ClienteID:"), gbc);
        gbc.gridx=1;             editorPanel.add(eIdCliente, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Vía:"), gbc);
        gbc.gridx=1;             editorPanel.add(eVia, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Ciudad:"), gbc);
        gbc.gridx=1;             editorPanel.add(eCiudad, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("CP:"), gbc);
        gbc.gridx=1;             editorPanel.add(eCP, gbc); y++;

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        pnl.add(btnCancelar); pnl.add(btnGuardar);
        gbc.gridx=0; gbc.gridy=++y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
        editorPanel.add(pnl, gbc);

        editorPanel.setVisible(false);
    }

    @Override
    protected void cargar() {
        try {
            List<Object[]> filas = new DireccionesDAO().listarTodos();
            modelo.setRowCount(0);
            for (Object[] f : filas) {
                modelo.addRow(new Object[]{false, f[0], f[1], f[2], f[3], f[4], "Detalles", "Editar", "Borrar"});
            }
        } catch (Exception ex) { showError("listar direcciones", ex); }
    }

    @Override
    protected void nuevo() {
        editId = null;
        eIdCliente.setValue(1); eVia.setText(""); eCiudad.setText(""); eCP.setText("");
        editorPanel.setVisible(true); eVia.requestFocus(); revalidate();
    }

    @Override
    protected void editar(int id) {
        try {
            Object[] d = new DireccionesDAO().getById(id);
            if (d == null) return;
            editId = (Integer) d[0];
            eIdCliente.setValue((Integer) d[1]);
            eVia.setText(String.valueOf(d[2]));
            eCiudad.setText(String.valueOf(d[3]));
            eCP.setText(String.valueOf(d[4]));
            editorPanel.setVisible(true); eVia.requestFocus(); revalidate();
        } catch (Exception ex) { showError("cargar dirección (editar)", ex); }
    }

    @Override
    protected void verDetalles(int id) {
        try {
            Object[] d = new DireccionesDAO().getById(id);
            if (d == null) { JOptionPane.showMessageDialog(this,"No existe la dirección."); return; }
            String msg = """
                    Detalles de la dirección
                    -----------------------
                    ID:        %s
                    ClienteID: %s
                    Vía:       %s
                    Ciudad:    %s
                    CP:        %s
                    """.formatted(d[0], d[1], d[2], d[3], d[4]);
            JOptionPane.showMessageDialog(this, msg, "Detalles", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError("detalles dirección", ex); }
    }

    private void guardar() {
        int idCliente = (Integer)eIdCliente.getValue();
        String via = eVia.getText().trim();
        String ciudad = eCiudad.getText().trim();
        String cp = eCP.getText().trim();
        if (via.isBlank() || ciudad.isBlank() || cp.isBlank()) { JOptionPane.showMessageDialog(this,"Todos los campos son obligatorios"); return; }

        try {
            DireccionesDAO dao = new DireccionesDAO();
            if (editId == null) dao.insertar(idCliente, via, ciudad, cp);
            else dao.modificar(editId, idCliente, via, ciudad, cp);
            cancelar(); cargar();
        } catch (Exception ex) { showError("guardar dirección", ex); }
    }

    private void cancelar() { editorPanel.setVisible(false); editId = null; }

    @Override
    protected void borrar(int id) {
        if (!confirm("¿Borrar dirección ID "+id+"?")) return;
        try { new DireccionesDAO().borrar(id); cargar(); }
        catch (Exception ex){ showError("borrar dirección", ex); }
    }
}

