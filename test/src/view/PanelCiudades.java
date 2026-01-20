package view;

import controller.CiudadesController;
import model.Ciudad;
import util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelCiudades extends JPanel {

    private final JTable tabla;
    private final JTextField tfNombre;
    private final JPanel form;

    private CiudadesController controller;

    public PanelCiudades() {
        setLayout(new BorderLayout());

        tabla = new JTable();
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(tabla);

        form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfNombre = new JTextField(20);

        form.add(new JLabel("Nombre:"));
        form.add(tfNombre);

        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public JPanel getFormPanel() { return form; }
    public JTable getTabla() { return tabla; }
    public void setController(CiudadesController c) { this.controller = c; }

    public void clearFormulario() { tfNombre.setText(""); }

    public void cargarTabla(List<Ciudad> lista) {
        String[] cols = {"ID", "Nombre"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Ciudad c : lista) m.addRow(new Object[]{c.getId(), c.getNombre()});
        tabla.setModel(m);
    }

    public Ciudad leerFormulario() {
        Integer id = UiUtils.getSelectedId(tabla);
        String nombre = tfNombre.getText().trim();
        return new Ciudad(id, nombre);
    }

    public List<Ciudad> obtenerSeleccion() {
        return UiUtils.tableSelectionToList(tabla, (r) -> new Ciudad(
                UiUtils.valInt(r.get(0)), String.valueOf(r.get(1))
        ));
    }
}
