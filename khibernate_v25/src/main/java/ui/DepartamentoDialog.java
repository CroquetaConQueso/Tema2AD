package ui;

import model.Departamento;

import javax.swing.*;
import java.awt.*;

public class DepartamentoDialog extends JDialog {

    private boolean ok = false;
    private Departamento result;

    private final JTextField txtDeptoNo = new JTextField(10);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtLoc = new JTextField(15);

    private final JButton btnAceptar = new JButton("Aceptar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final ModoDialogo modo;

    public DepartamentoDialog(Window owner, String title, Departamento d, ModoDialogo modo) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.modo = modo;

        setSize(420, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("depto_no:"), c);
        c.gridx = 1;
        form.add(txtDeptoNo, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("dnombre:"), c);
        c.gridx = 1;
        form.add(txtNombre, c);

        c.gridx = 0; c.gridy = 2;
        form.add(new JLabel("loc:"), c);
        c.gridx = 1;
        form.add(txtLoc, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnAceptar);
        buttons.add(btnCancelar);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (d != null) {
            txtDeptoNo.setText(String.valueOf(d.getDeptoNo()));
            txtNombre.setText(nvl(d.getDnombre()));
            txtLoc.setText(nvl(d.getLoc()));
        }

        if (modo != ModoDialogo.NUEVO) {
            txtDeptoNo.setEnabled(false); // PK no se toca
        }

        if (modo == ModoDialogo.VER) {
            txtNombre.setEnabled(false);
            txtLoc.setEnabled(false);
            btnAceptar.setVisible(false);
        }

        btnCancelar.addActionListener(e -> dispose());
        btnAceptar.addActionListener(e -> onAceptar());
    }

    private void onAceptar() {
        try {
            int id = Integer.parseInt(txtDeptoNo.getText().trim());
            String nom = txtNombre.getText().trim();
            String loc = txtLoc.getText().trim();

            if (nom.isEmpty() || loc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y ubicación son obligatorios.");
                return;
            }

            result = new Departamento(id, nom, loc);
            ok = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "depto_no debe ser numérico.");
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    public boolean isOk() {
        return ok;
    }

    public Departamento getDepartamento() {
        return result;
    }
}
