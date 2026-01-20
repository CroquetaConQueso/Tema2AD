package ui;

import model.Empleado;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class EmpleadoDialog extends JDialog {

    private boolean ok = false;
    private Empleado result;

    private final JTextField txtEmpNo = new JTextField(10);
    private final JTextField txtApellido = new JTextField(12);
    private final JTextField txtOficio = new JTextField(12);
    private final JTextField txtDir = new JTextField(10);
    private final JTextField txtFechaAlt = new JTextField(12); // yyyy-mm-dd
    private final JTextField txtSalario = new JTextField(10);
    private final JTextField txtComision = new JTextField(10);
    private final JTextField txtDeptNo = new JTextField(10);

    private final JButton btnAceptar = new JButton("Aceptar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final ModoDialogo modo;

    public EmpleadoDialog(Window owner, String title, Empleado e, ModoDialogo modo) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.modo = modo;

        setSize(520, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, c, y++, "emp_no:", txtEmpNo);
        addRow(form, c, y++, "apellido:", txtApellido);
        addRow(form, c, y++, "oficio:", txtOficio);
        addRow(form, c, y++, "dir (num):", txtDir);
        addRow(form, c, y++, "fecha_alt (yyyy-mm-dd):", txtFechaAlt);
        addRow(form, c, y++, "salario (num):", txtSalario);
        addRow(form, c, y++, "comision (num):", txtComision);
        addRow(form, c, y++, "dept_no (num):", txtDeptNo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnAceptar);
        buttons.add(btnCancelar);

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (e != null) {
            txtEmpNo.setText(String.valueOf(e.getEmpNo()));
            txtApellido.setText(nvl(e.getApellido()));
            txtOficio.setText(nvl(e.getOficio()));
            txtDir.setText(e.getDir() == null ? "" : String.valueOf(e.getDir()));
            txtFechaAlt.setText(e.getFechaAlt() == null ? "" : e.getFechaAlt().toString());
            txtSalario.setText(e.getSalario() == null ? "" : String.valueOf(e.getSalario()));
            txtComision.setText(e.getComision() == null ? "" : String.valueOf(e.getComision()));
            txtDeptNo.setText(e.getDeptNo() == null ? "" : String.valueOf(e.getDeptNo()));
        }

        if (modo != ModoDialogo.NUEVO) {
            txtEmpNo.setEnabled(false); // PK no se cambia
        }

        if (modo == ModoDialogo.VER) {
            setAllEnabled(false);
            btnAceptar.setVisible(false);
        }

        btnCancelar.addActionListener(ev -> dispose());
        btnAceptar.addActionListener(ev -> onAceptar());
    }

    private static void addRow(JPanel form, GridBagConstraints c, int y, String label, JComponent comp) {
        c.gridx = 0; c.gridy = y;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(comp, c);
    }

    private void setAllEnabled(boolean enabled) {
        txtEmpNo.setEnabled(enabled);
        txtApellido.setEnabled(enabled);
        txtOficio.setEnabled(enabled);
        txtDir.setEnabled(enabled);
        txtFechaAlt.setEnabled(enabled);
        txtSalario.setEnabled(enabled);
        txtComision.setEnabled(enabled);
        txtDeptNo.setEnabled(enabled);
    }

    private void onAceptar() {
        try {
            int empNo = Integer.parseInt(txtEmpNo.getText().trim());
            String apellido = txtApellido.getText().trim();
            String oficio = txtOficio.getText().trim();

            if (apellido.isEmpty() || oficio.isEmpty()) {
                JOptionPane.showMessageDialog(this, "apellido y oficio son obligatorios.");
                return;
            }

            Integer dir = parseIntOrNull(txtDir.getText());
            Date fecha = parseDateOrNull(txtFechaAlt.getText());
            Integer salario = parseIntOrNull(txtSalario.getText());
            Integer comision = parseIntOrNull(txtComision.getText());
            Integer deptNo = parseIntOrNull(txtDeptNo.getText());

            result = new Empleado(empNo, apellido, oficio, dir, fecha, salario, comision, deptNo);
            ok = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "emp_no debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private static Integer parseIntOrNull(String s) {
        String t = (s == null) ? "" : s.trim();
        if (t.isEmpty()) return null;
        return Integer.parseInt(t);
    }

    private static Date parseDateOrNull(String s) {
        String t = (s == null) ? "" : s.trim();
        if (t.isEmpty()) return null;
        try {
            return Date.valueOf(t);
        } catch (Exception ex) {
            throw new IllegalArgumentException("fecha_alt debe tener formato yyyy-mm-dd (ej: 1981-02-20).");
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    public boolean isOk() { return ok; }
    public Empleado getEmpleado() { return result; }
}
