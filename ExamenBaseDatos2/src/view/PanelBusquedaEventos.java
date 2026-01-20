package view;

import controller.AppController;
import model.EventoSocial;
import util.ButtonColumn;
import util.DateUtils;
import util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PanelBusquedaEventos extends JPanel {

    private final AppController controller;

    private final JTextField txtFechaInicio = new JTextField(10);
    private final JTextField txtFechaFin = new JTextField(10);

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelBusquedaEventos(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        setBackground(new Color(0xF7E2BB));

        // --------- Panel superior: filtros ----------
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBackground(new Color(0xF7E2BB));

        panelFiltros.add(new JLabel("Fecha inicio (dd/mm/yyyy):"));
        panelFiltros.add(txtFechaInicio);
        panelFiltros.add(new JLabel("Fecha fin:"));
        panelFiltros.add(txtFechaFin);

        JButton btnBuscar = new JButton("Buscar");
        UiUtils.estiloBotonPrimario(btnBuscar);
        panelFiltros.add(btnBuscar);

        add(panelFiltros, BorderLayout.NORTH);

        // --------- Tabla ----------
        String[] columnas = {
                "Sel.", "ID", "Fecha evento", "Nombre",
                "Consultar", "Modificar", "Borrar"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column >= 4;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);

        // --------- Panel inferior: botones globales ----------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.setBackground(new Color(0xF7E2BB));

        JButton btnAlta = new JButton("Alta");
        JButton btnConsultaMultiple = new JButton("Consulta múltiple");
        JButton btnBorradoMultiple = new JButton("Borrado múltiple");

        UiUtils.estiloBotonSecundario(btnAlta);
        UiUtils.estiloBotonPlano(btnConsultaMultiple);
        UiUtils.estiloBotonPeligro(btnBorradoMultiple);

        panelBotones.add(btnAlta);
        panelBotones.add(btnConsultaMultiple);
        panelBotones.add(btnBorradoMultiple);

        add(panelBotones, BorderLayout.SOUTH);

        // --------- Botones por fila ----------
        Action consultarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarSeleccionado(true);
            }
        };
        new ButtonColumn(tabla, consultarAction, 4, false);

        Action modificarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarSeleccionado(false);
            }
        };
        new ButtonColumn(tabla, modificarAction, 5, false);

        Action borrarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                borrarSeleccionado();
            }
        };
        new ButtonColumn(tabla, borrarAction, 6, true);

        // Listeners
        btnBuscar.addActionListener(e -> recargarTabla());
        btnAlta.addActionListener(e -> mostrarFormulario(null, true, false));
        btnBorradoMultiple.addActionListener(e -> borrarMultiple());
        btnConsultaMultiple.addActionListener(e -> consultaMultiple());

        recargarTabla();
    }

    // ================== LÓGICA ==================

    private LocalDate[] leerRangoFechas() {
        try {
            LocalDate desde = DateUtils.parse(txtFechaInicio.getText());
            LocalDate hasta = DateUtils.parse(txtFechaFin.getText());
            return new LocalDate[]{desde, hasta};
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Introduce fechas válidas en formato dd/mm/yyyy.",
                    "Fechas incorrectas",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void recargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<EventoSocial> lista;
            if (txtFechaInicio.getText().isBlank() || txtFechaFin.getText().isBlank()) {
                lista = controller.listarEventos();
            } else {
                LocalDate[] rango = leerRangoFechas();
                if (rango == null) return;
                lista = controller.buscarEventos(rango[0], rango[1]);
            }

            for (EventoSocial e : lista) {
                modeloTabla.addRow(new Object[]{
                        Boolean.FALSE,
                        e.getIdEvento(),
                        DateUtils.format(e.getFechaEvento()),
                        e.getNombre(),
                        "Consultar",
                        "Modificar",
                        "Borrar"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar eventos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer obtenerIdFilaSeleccionSimple() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una fila de la tabla.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (Integer) modeloTabla.getValueAt(fila, 1);
    }

    private List<Integer> obtenerIdsMarcados() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Boolean sel = (Boolean) modeloTabla.getValueAt(i, 0);
            if (Boolean.TRUE.equals(sel)) {
                ids.add((Integer) modeloTabla.getValueAt(i, 1));
            }
        }
        return ids;
    }

    private void mostrarFormulario(EventoSocial evento, boolean esAlta, boolean soloConsulta) {
        PanelEventoForm form = new PanelEventoForm(this, controller, evento, esAlta);
        form.setSoloConsulta(soloConsulta);

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, true);
        dlg.setTitle(esAlta ? "Alta de evento"
                : (soloConsulta ? "Consulta de evento" : "Modificar evento"));
        dlg.setContentPane(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        recargarTabla();
    }

    private void editarSeleccionado(boolean soloConsulta) {
        Integer id = obtenerIdFilaSeleccionSimple();
        if (id == null) return;
        try {
            EventoSocial e = controller.listarEventos().stream()
                    .filter(ev -> ev.getIdEvento() == id)
                    .findFirst().orElse(null);
            if (e == null) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado el evento seleccionado.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            mostrarFormulario(e, false, soloConsulta);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar evento: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarSeleccionado() {
        Integer id = obtenerIdFilaSeleccionSimple();
        if (id == null) return;

        int opc = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas borrar el evento seleccionado?",
                "Confirmar borrado", JOptionPane.YES_NO_OPTION);
        if (opc != JOptionPane.YES_OPTION) return;

        try {
            controller.eliminarEvento(id);
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo borrar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarMultiple() {
        List<Integer> ids = obtenerIdsMarcados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Marca al menos una fila con el checkbox.");
            return;
        }

        int opc = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas borrar " + ids.size() + " eventos?",
                "Borrado múltiple", JOptionPane.YES_NO_OPTION);
        if (opc != JOptionPane.YES_OPTION) return;

        try {
            for (Integer id : ids) {
                controller.eliminarEvento(id);
            }
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error en borrado múltiple: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==== NUEVA CONSULTA MÚLTIPLE: muestra todos los datos visibles ====
    private void consultaMultiple() {
        List<Integer> ids = obtenerIdsMarcados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Marca al menos una fila.");
            return;
        }

        StringBuilder sb = new StringBuilder("Eventos seleccionados:\n\n");
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Boolean sel = (Boolean) modeloTabla.getValueAt(i, 0);
            if (Boolean.TRUE.equals(sel)) {
                Object id = modeloTabla.getValueAt(i, 1);
                Object fecha = modeloTabla.getValueAt(i, 2);
                Object nombre = modeloTabla.getValueAt(i, 3);

                sb.append("ID: ").append(id).append("\n");
                sb.append("Fecha: ").append(fecha).append("\n");
                sb.append("Nombre: ").append(nombre).append("\n\n");
            }
        }

        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "Consulta múltiple",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
