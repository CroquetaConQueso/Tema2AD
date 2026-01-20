package view;

import controller.PersonasController;
import model.Persona;
import util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelPersonas extends JPanel {

    private final JTable tabla;
    private final JTextField tfNombre;
    private final JSpinner spEdad;
    private final JComboBox<Object> cbCiudadId;
    private final JPanel form;

    private PersonasController controller;

    public PanelPersonas() {
        setLayout(new BorderLayout());

        tabla = new JTable();
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(tabla);

        form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfNombre = new JTextField(15);
        spEdad = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        cbCiudadId = new JComboBox<>(new Object[]{null, 1, 2, 3}); // demo

        form.add(new JLabel("Nombre:"));
        form.add(tfNombre);
        form.add(new JLabel("Edad:"));
        form.add(spEdad);
        form.add(new JLabel("CiudadId:"));
        form.add(cbCiudadId);

        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public JPanel getFormPanel() { return form; }
    public JTable getTabla() { return tabla; }
    public void setController(PersonasController c) { this.controller = c; }

    public void clearFormulario() {
        tfNombre.setText("");
        spEdad.setValue(0);
        cbCiudadId.setSelectedIndex(0);
    }

    public void cargarTabla(List<Persona> lista) {
        String[] cols = {"ID", "Nombre", "Edad", "CiudadId"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Persona p : lista) m.addRow(new Object[]{p.getId(), p.getNombre(), p.getEdad(), p.getCiudadId()});
        tabla.setModel(m);
    }

    public Persona leerFormulario() {
        Integer id = UiUtils.getSelectedId(tabla);
        String nombre = tfNombre.getText().trim();
        int edad = (int) spEdad.getValue();
        Integer ciudadId = (cbCiudadId.getSelectedItem() instanceof Integer) ? (Integer) cbCiudadId.getSelectedItem() : null;
        return new Persona(id, nombre, edad, ciudadId);
    }

    public List<Persona> obtenerSeleccion() {
        return UiUtils.tableSelectionToList(tabla, (r) -> new Persona(
                UiUtils.valInt(r.get(0)), String.valueOf(r.get(1)),
                UiUtils.valInt(r.get(2)), UiUtils.valIntNullable(r.get(3))
        ));
    }
}
