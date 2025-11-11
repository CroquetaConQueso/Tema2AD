package vista.clientes;

import dao.ClientesDAO;
import vista.base.CrudTab;
import vista.validacion.UiFeedback;
import vista.validacion.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TabClientes extends CrudTab {

    private JTextField eNombre;
    private JTextField eEmail;
    private JSpinner   eEdad;

    public TabClientes() { super(); }

    @Override
    protected void construirModelo() {
        modelo = new DefaultTableModel(new Object[]{
                "✓", "ID", "Nombre", "Email", "Edad", "Detalles", "Editar", "Borrar"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c >= 5; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Boolean.class;
                    case 1, 4 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        cbCampo.removeAllItems();
        cbCampo.addItem("Todos");
        cbCampo.addItem("ID");
        cbCampo.addItem("Nombre");
        cbCampo.addItem("Email");
        cbCampo.addItem("Edad");

        configurarEditor();
    }

    @Override
    protected void alInstalarModelo() {
        tabla.setModel(modelo);
        setColumnWidths(50, 70, 300, 380, 70, 90, 90, 90);

        TableColumnModel cols = tabla.getColumnModel();
        cols.getColumn(5).setCellRenderer(new BtnRenderer("Detalles"));
        cols.getColumn(5).setCellEditor  (new BtnEditor  ("Detalles", row -> { Integer id=idFromViewRow(row); if (id!=null) verDetalles(id); }));
        cols.getColumn(6).setCellRenderer(new BtnRenderer("Editar"));
        cols.getColumn(6).setCellEditor  (new BtnEditor  ("Editar",   row -> { Integer id=idFromViewRow(row); if (id!=null) editar(id);   }));
        cols.getColumn(7).setCellRenderer(new BtnRenderer("Borrar"));
        cols.getColumn(7).setCellEditor  (new BtnEditor  ("Borrar",   row -> { Integer id=idFromViewRow(row); if (id!=null) borrar(id);   }));
    }

    @Override
    protected void configurarEditor() {
        editorPanel.removeAll();

        if (eNombre == null) eNombre = new JTextField(24);
        if (eEmail  == null) eEmail  = new JTextField(24);
        if (eEdad   == null) eEdad   = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));

        JButton btnGuardar  = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());

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

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        pnlBtns.add(btnCancelar); pnlBtns.add(btnGuardar);
        gbc.gridx=0; gbc.gridy=++y; gbc.gridwidth=2; gbc.fill=GridBagConstraints.HORIZONTAL;
        editorPanel.add(pnlBtns, gbc);

        editorPanel.setVisible(false);
    }

    @Override
    protected void cargar() {
        try {
            List<Object[]> filas = new ClientesDAO().listarTodos();
            modelo.setRowCount(0);
            for (Object[] f : filas) {
                modelo.addRow(new Object[]{false, f[0], f[1], f[2], f[3], "Detalles", "Editar", "Borrar"});
            }
        } catch (Exception ex) { showError("listar clientes", ex); }
    }

    @Override
    protected void nuevo() {
        editId = null;
        eNombre.setText(""); eEmail.setText(""); eEdad.setValue(18);
        UiFeedback.clearAll(eNombre, eEmail);
        editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
    }

    @Override
    protected void editar(int id) {
        try {
            Object[] c = new ClientesDAO().getById(id);
            if (c == null) return;
            editId = (Integer)c[0];
            eNombre.setText(String.valueOf(c[1]));
            eEmail.setText(String.valueOf(c[2]));
            eEdad.setValue((Integer)c[3]);
            UiFeedback.clearAll(eNombre, eEmail);
            editorPanel.setVisible(true); eNombre.requestFocus(); revalidate();
        } catch (Exception ex) { showError("cargar cliente (editar)", ex); }
    }

    @Override
    protected void verDetalles(int id) {
        try {
            Object[] c = new ClientesDAO().getById(id);
            if (c == null) { JOptionPane.showMessageDialog(this,"No existe el cliente."); return; }
            String msg = """
                    Detalles del cliente
                    --------------------
                    ID:     %s
                    Nombre: %s
                    Email:  %s
                    Edad:   %s
                    """.formatted(c[0],c[1],c[2],c[3]);
            JOptionPane.showMessageDialog(this, msg, "Detalles", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError("detalles cliente", ex); }
    }

    private void guardar() {
        String nombre = eNombre.getText().trim();
        String email  = eEmail.getText().trim();
        int    edad   = (Integer) eEdad.getValue();

        UiFeedback.clearAll(eNombre, eEmail);
        List<String> errores = new ArrayList<>();

        if (!Validator.lengthBetween(nombre, 2, 100)) {
            errores.add("Nombre: entre 2 y 100 caracteres");
            UiFeedback.markError(eNombre, "Introduce entre 2 y 100 caracteres");
        }
        if (!Validator.email(email)) {
            errores.add("Email: formato inválido (usuario@dominio.tld)");
            UiFeedback.markError(eEmail, "Formato de email inválido");
        }
        if (!Validator.edad(edad)) {
            errores.add("Edad: debe estar entre 0 y 120");
        }

        if (!errores.isEmpty()) {
            UiFeedback.showErrors(this, errores);
            return;
        }

        try {
            ClientesDAO dao = new ClientesDAO();
            if (editId == null) dao.insertar(nombre,email,edad);
            else dao.modificar(editId,nombre,email,edad);
            cancelar(); cargar();
        } catch (Exception ex) { showError("guardar cliente", ex); }
    }

    private void cancelar() { editorPanel.setVisible(false); editId = null; }
    
    @Override
    protected void borrar(int id) {
        if (!confirm("¿Borrar cliente ID "+id+"?")) return;
        try { new ClientesDAO().borrar(id); cargar(); }
        catch (Exception ex){ showError("borrar cliente", ex); }
    }
}
