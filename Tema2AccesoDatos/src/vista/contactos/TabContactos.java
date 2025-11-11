package vista.contactos;

import dao.ContactosDAO;
import vista.base.CrudTab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class TabContactos extends CrudTab {

    private JSpinner eIdCliente;
    private JComboBox<String> eTipo;
    private JTextField eValor;

    public TabContactos() { super(); }

    @Override
    protected void construirModelo() {
        modelo = new DefaultTableModel(new Object[]{
                "✓", "ID", "ClienteID", "Tipo", "Valor", "Detalles", "Editar", "Borrar"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c >= 5; }
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
        cbCampo.addItem("Tipo");
        cbCampo.addItem("Valor");
        configurarEditor();
    }

    @Override
    protected void alInstalarModelo() {
        setColumnWidths(50, 70, 90, 140, 520, 100, 100, 100);
        TableColumnModel cols = tabla.getColumnModel();
        cols.getColumn(5).setCellRenderer(new BtnRenderer("Detalles"));
        cols.getColumn(5).setCellEditor  (new BtnEditor  ("Detalles", row -> { Integer id = idFromViewRow(row); if (id != null) verDetalles(id); }));
        cols.getColumn(6).setCellRenderer(new BtnRenderer("Editar"));
        cols.getColumn(6).setCellEditor  (new BtnEditor  ("Editar",   row -> { Integer id = idFromViewRow(row); if (id != null) editar(id); }));
        cols.getColumn(7).setCellRenderer(new BtnRenderer("Borrar"));
        cols.getColumn(7).setCellEditor  (new BtnEditor  ("Borrar",   row -> { Integer id = idFromViewRow(row); if (id != null) borrar(id); }));
    }

    @Override
    protected void configurarEditor() {
        editorPanel.removeAll();
        if (eIdCliente == null) eIdCliente = new JSpinner(new SpinnerNumberModel(1,1,1_000_000,1));
        if (eTipo == null) eTipo = new JComboBox<>(new String[]{"telefono","email","otro"});
        if (eValor == null) eValor = new JTextField(28);

        JButton btnGuardar  = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4); gbc.anchor = GridBagConstraints.WEST;
        int y=0;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("ClienteID:"), gbc);
        gbc.gridx=1;             editorPanel.add(eIdCliente, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx=1;             editorPanel.add(eTipo, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; editorPanel.add(new JLabel("Valor:"), gbc);
        gbc.gridx=1;             editorPanel.add(eValor, gbc); y++;

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        pnl.add(btnCancelar); pnl.add(btnGuardar);
        gbc.gridx=0; gbc.gridy=++y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
        editorPanel.add(pnl, gbc);

        editorPanel.setVisible(false);
    }

    @Override
    protected void cargar() {
        try {
            List<Object[]> filas = new ContactosDAO().listarTodos();
            modelo.setRowCount(0);
            for (Object[] f : filas) {
                modelo.addRow(new Object[]{false, f[0], f[1], String.valueOf(f[2]), f[3], "Detalles", "Editar", "Borrar"});
            }
        } catch (Exception ex) { showError("listar contactos", ex); }
    }

    @Override
    protected void nuevo() {
        editId = null;
        eIdCliente.setValue(1); eTipo.setSelectedIndex(0); eValor.setText("");
        editorPanel.setVisible(true); eValor.requestFocus(); revalidate();
    }

    @Override
    protected void editar(int id) {
        try {
            Object[] d = new ContactosDAO().getById(id);
            if (d == null) return;
            editId = (Integer) d[0];
            eIdCliente.setValue((Integer) d[1]);
            eTipo.setSelectedItem(String.valueOf(d[2]));
            eValor.setText(String.valueOf(d[3]));
            editorPanel.setVisible(true); eValor.requestFocus(); revalidate();
        } catch (Exception ex) { showError("cargar contacto (editar)", ex); }
    }

    @Override
    protected void verDetalles(int id) {
        try {
            Object[] d = new ContactosDAO().getById(id);
            if (d == null) { JOptionPane.showMessageDialog(this,"No existe el contacto."); return; }
            String msg = """
                    Detalles del contacto
                    ---------------------
                    ID:        %s
                    ClienteID: %s
                    Tipo:      %s
                    Valor:     %s
                    """.formatted(d[0], d[1], d[2], d[3]);
            JOptionPane.showMessageDialog(this, msg, "Detalles", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError("detalles contacto", ex); }
    }

    private void guardar() {
        int idCliente = (Integer)eIdCliente.getValue();
        String tipo   = (String)eTipo.getSelectedItem();
        String valor  = eValor.getText().trim();

        if (valor.isBlank()) { JOptionPane.showMessageDialog(this,"Valor requerido"); return; }
        if ("email".equalsIgnoreCase(tipo) && !emailValido(valor)) {
            JOptionPane.showMessageDialog(this,"Email de contacto inválido"); return;
        }

        try {
            ContactosDAO dao = new ContactosDAO();
            if (editId == null) dao.insertar(idCliente, tipo, valor);
            else dao.modificar(editId, idCliente, tipo, valor);
            cancelar(); cargar();
        } catch (Exception ex) { showError("guardar contacto", ex); }
    }

    private void cancelar() { editorPanel.setVisible(false); editId = null; }

    @Override
    protected void borrar(int id) {
        if (!confirm("¿Borrar contacto ID "+id+"?")) return;
        try { new ContactosDAO().borrar(id); cargar(); }
        catch (Exception ex){ showError("borrar contacto", ex); }
    }
}
