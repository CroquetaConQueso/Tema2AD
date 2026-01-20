package view;

import controller.ArchivosController;
import model.ArchivoBinario;
import util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelArchivos extends JPanel {

    private final JTable tabla;
    private final JTextField tfNombre;
    private final JTextField tfMime;
    private final JButton btnImportar;
    private final JButton btnExportar;
    private final JPanel form;

    private ArchivosController controller;

    public PanelArchivos() {
        setLayout(new BorderLayout());

        tabla = new JTable();
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(tabla);

        form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfNombre = new JTextField(18);
        tfMime = new JTextField(12);
        btnImportar = new JButton("Importar");
        btnExportar = new JButton("Exportar");

        form.add(new JLabel("Nombre:"));
        form.add(tfNombre);
        form.add(new JLabel("MIME:"));
        form.add(tfMime);
        form.add(btnImportar);
        form.add(btnExportar);

        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        btnImportar.addActionListener(e -> { if (controller != null) controller.importar(); });
        btnExportar.addActionListener(e -> { if (controller != null) controller.exportarSeleccion(); });
    }

    public JPanel getFormPanel() { return form; }
    public JTable getTabla() { return tabla; }
    public void setController(ArchivosController c) { this.controller = c; }

    public void clearFormulario() { tfNombre.setText(""); tfMime.setText(""); }

    public void cargarTabla(List<ArchivoBinario> lista) {
        String[] cols = {"ID", "Nombre", "MIME", "Tamaño(bytes)", "Creado"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ArchivoBinario a : lista) {
            m.addRow(new Object[]{a.getId(), a.getNombreOriginal(), a.getMime(),
                    (a.getDatos() != null ? a.getDatos().length : 0), a.getCreadoAt()});
        }
        tabla.setModel(m);
    }

    public ArchivoBinario leerFormularioMetadatos() {
        Integer id = UiUtils.getSelectedId(tabla);
        String nombre = tfNombre.getText().trim();
        String mime = tfMime.getText().trim();
        return new ArchivoBinario(id, nombre, mime, null, null);
    }

    public List<ArchivoBinario> obtenerSeleccion() {
        return UiUtils.tableSelectionToList(tabla, (r) -> new ArchivoBinario(
                UiUtils.valInt(r.get(0)), String.valueOf(r.get(1)),
                String.valueOf(r.get(2)), null, String.valueOf(r.get(4))
        ));
    }
}
