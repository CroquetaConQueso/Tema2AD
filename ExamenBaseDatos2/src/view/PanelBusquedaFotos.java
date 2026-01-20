package view;

import controller.AppController;
import model.FotoEventoSocial;
import util.ButtonColumn;
import util.DateUtils;
import util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PanelBusquedaFotos extends JPanel {

    private final AppController controller;

    private final JTextField txtFechaInicio = new JTextField(8);
    private final JTextField txtFechaFin = new JTextField(8);
    private final JComboBox<String> cmbCampo =
            new JComboBox<>(new String[]{"Todos", "ID", "Fecha", "Evento", "Descripción", "Cantidad"});
    private final JTextField txtValor = new JTextField(10);

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public PanelBusquedaFotos(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        setBackground(new Color(0xF7E2BB));

        // --------- Filtros arriba ----------
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBackground(new Color(0xF7E2BB));

        panelFiltros.add(new JLabel("Campo:"));
        panelFiltros.add(cmbCampo);
        panelFiltros.add(new JLabel("Valor:"));
        panelFiltros.add(txtValor);

        panelFiltros.add(new JLabel("Fecha inicio:"));
        panelFiltros.add(txtFechaInicio);
        panelFiltros.add(new JLabel("Fecha fin:"));
        panelFiltros.add(txtFechaFin);

        JButton btnBuscar = new JButton("Buscar");
        UiUtils.estiloBotonPrimario(btnBuscar);
        panelFiltros.add(btnBuscar);

        add(panelFiltros, BorderLayout.NORTH);

        // --------- Tabla ----------
        String[] cols = {
                "Sel.", "ID", "Imagen", "Fecha evento", "Evento",
                "Consultar", "Modificar", "Borrar"
        };
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                if (columnIndex == 2) return Icon.class;   // imagen
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column >= 5;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(60); // más alto para ver la miniatura
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --------- Botones abajo ----------
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

        // --------- Botones por fila ---------
        Action consultarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarSeleccionado(true);
            }
        };
        new ButtonColumn(tabla, consultarAction, 5, false);

        Action modificarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarSeleccionado(false);
            }
        };
        new ButtonColumn(tabla, modificarAction, 6, false);

        Action borrarAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                borrarSeleccionado();
            }
        };
        new ButtonColumn(tabla, borrarAction, 7, true);

        // --------- Listeners globales ----------
        btnBuscar.addActionListener(e -> recargarTabla());
        btnAlta.addActionListener(e -> mostrarFormulario(null, true, false));
        btnBorradoMultiple.addActionListener(e -> borrarMultiple());
        btnConsultaMultiple.addActionListener(e -> consultaMultiple());

        recargarTabla();
    }

    // ================== LÓGICA ==================

    private void recargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            // 1) Traemos TODO de BD
            List<FotoEventoSocial> lista = controller.listarFotos();

            // 2) Filtramos por FECHAS si están rellenas
            if (!txtFechaInicio.getText().isBlank() && !txtFechaFin.getText().isBlank()) {
                LocalDate desde = DateUtils.parse(txtFechaInicio.getText());
                LocalDate hasta = DateUtils.parse(txtFechaFin.getText());
                List<FotoEventoSocial> filtradasFecha = new ArrayList<>();
                for (FotoEventoSocial f : lista) {
                    LocalDate fecha = f.getEvento().getFechaEvento();
                    if ((fecha.isEqual(desde) || fecha.isAfter(desde)) &&
                        (fecha.isEqual(hasta) || fecha.isBefore(hasta))) {
                        filtradasFecha.add(f);
                    }
                }
                lista = filtradasFecha;
            }

            // 3) Filtramos por CAMPO / VALOR
            String valor = txtValor.getText().trim().toLowerCase();
            String campo = (String) cmbCampo.getSelectedItem();

            if (!valor.isBlank()) {
                List<FotoEventoSocial> filtradas = new ArrayList<>();
                for (FotoEventoSocial f : lista) {
                    String idStr = String.valueOf(f.getIdFoto());
                    String fechaStr = DateUtils.format(f.getEvento().getFechaEvento());
                    String eventoStr = f.getEvento().getNombre() == null ? "" :
                            f.getEvento().getNombre().toLowerCase();
                    String descStr = f.getDescripcion() == null ? "" :
                            f.getDescripcion().toLowerCase();
                    String cantStr = String.valueOf(f.getCantidad());

                    boolean match = false;

                    if ("Todos".equals(campo)) {
                        match = idStr.contains(valor)
                                || fechaStr.toLowerCase().contains(valor)
                                || eventoStr.contains(valor)
                                || descStr.contains(valor)
                                || cantStr.contains(valor);
                    } else if ("ID".equals(campo)) {
                        match = idStr.contains(valor);
                    } else if ("Fecha".equals(campo)) {
                        match = fechaStr.toLowerCase().contains(valor);
                    } else if ("Evento".equals(campo)) {
                        match = eventoStr.contains(valor);
                    } else if ("Descripción".equals(campo)) {
                        match = descStr.contains(valor);
                    } else if ("Cantidad".equals(campo)) {
                        match = cantStr.contains(valor);
                    }

                    if (match) {
                        filtradas.add(f);
                    }
                }
                lista = filtradas;
            }

            // 4) Rellenamos tabla
            for (FotoEventoSocial f : lista) {
                Icon icon = null;
                if (f.getFoto() != null && f.getFoto().length > 0) {
                    try {
                        ImageIcon imgIcon = new ImageIcon(f.getFoto());
                        Image img = imgIcon.getImage()
                                .getScaledInstance(80, 50, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(img);
                    } catch (Exception ex) {
                        // Si los bytes NO son una imagen válida, ignoramos y no mostramos miniatura
                        System.err.println("BLOB de imagen inválido para idFoto=" + f.getIdFoto());
                    }
                }

                modeloTabla.addRow(new Object[]{
                        Boolean.FALSE,
                        f.getIdFoto(),
                        icon,
                        DateUtils.format(f.getEvento().getFechaEvento()),
                        f.getEvento().getNombre(),
                        "Consultar",
                        "Modificar",
                        "Borrar"
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar fotos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getIdSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una fila.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (Integer) modeloTabla.getValueAt(row, 1);
    }

    private List<Integer> getIdsMarcados() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Boolean sel = (Boolean) modeloTabla.getValueAt(i, 0);
            if (Boolean.TRUE.equals(sel)) {
                ids.add((Integer) modeloTabla.getValueAt(i, 1));
            }
        }
        return ids;
    }

    private void mostrarFormulario(FotoEventoSocial foto, boolean esAlta, boolean soloConsulta) {
        PanelFotoForm form = new PanelFotoForm(controller, foto, esAlta, soloConsulta);

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, true);
        dlg.setTitle(esAlta ? "Alta foto"
                : (soloConsulta ? "Consulta foto" : "Modificar foto"));
        dlg.setContentPane(form);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        recargarTabla();
    }

    private void editarSeleccionado(boolean soloConsulta) {
        Integer id = getIdSeleccionado();
        if (id == null) return;
        try {
            FotoEventoSocial f = controller.listarFotos().stream()
                    .filter(x -> x.getIdFoto() == id)
                    .findFirst().orElse(null);
            if (f == null) {
                JOptionPane.showMessageDialog(this,
                        "No se ha encontrado la foto seleccionada.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            mostrarFormulario(f, false, soloConsulta);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar foto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarSeleccionado() {
        Integer id = getIdSeleccionado();
        if (id == null) return;

        int opc = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas borrar la foto seleccionada?",
                "Confirmar borrado", JOptionPane.YES_NO_OPTION);
        if (opc != JOptionPane.YES_OPTION) return;

        try {
            controller.eliminarFoto(id);
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al borrar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarMultiple() {
        List<Integer> ids = getIdsMarcados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Marca al menos una fila.");
            return;
        }

        int opc = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas borrar " + ids.size() + " fotos?",
                "Borrado múltiple", JOptionPane.YES_NO_OPTION);
        if (opc != JOptionPane.YES_OPTION) return;

        try {
            controller.eliminarFotos(ids);
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error en borrado múltiple: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==== NUEVA CONSULTA MÚLTIPLE (toda la info) ====
    private void consultaMultiple() {
        List<Integer> ids = getIdsMarcados();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Marca al menos una fila.");
            return;
        }

        StringBuilder sb = new StringBuilder("Fotos seleccionadas:\n\n");

        try {
            // Buscamos los objetos reales para tener descripción y cantidad
            List<FotoEventoSocial> todas = controller.listarFotos();

            for (Integer id : ids) {
                FotoEventoSocial f = todas.stream()
                        .filter(x -> x.getIdFoto() == id)
                        .findFirst().orElse(null);
                if (f != null) {
                    sb.append("ID: ").append(f.getIdFoto()).append("\n");
                    sb.append("Fecha: ").append(DateUtils.format(f.getEvento().getFechaEvento())).append("\n");
                    sb.append("Evento: ").append(f.getEvento().getNombre()).append("\n");
                    sb.append("Descripción: ").append(f.getDescripcion()).append("\n");
                    sb.append("Cantidad: ").append(f.getCantidad()).append("\n\n");
                }
            }

            JOptionPane.showMessageDialog(this,
                    sb.toString(),
                    "Consulta múltiple",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) { // aquí capturamos SQLException y cualquier otra
            JOptionPane.showMessageDialog(this,
                    "Error al obtener las fotos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
