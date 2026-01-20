package view;

import controller.AppController;
import model.EventoSocial;
import model.FotoEventoSocial;
import util.ByteUtils;
import util.DateUtils;
import util.UiUtils;
import validation.Validador;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class PanelFotoForm extends JPanel {

    private final AppController controller;
    private FotoEventoSocial foto;
    private final boolean esAlta;
    private final boolean soloConsulta;

    private final JComboBox<EventoSocial> cboEvento = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(20);
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
    private final JLabel lblPreview = new JLabel();

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnVolver = new JButton("Volver");
    private final JButton btnCambiarImagen = new JButton("Seleccionar imagen");

    public PanelFotoForm(AppController controller, FotoEventoSocial foto,
                         boolean esAlta, boolean soloConsulta) {
        this.controller = controller;
        this.foto = (foto != null) ? foto : new FotoEventoSocial();
        this.esAlta = esAlta;
        this.soloConsulta = soloConsulta;

        setLayout(new GridBagLayout());
        setBackground(new Color(0xF7E2BB));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Combobox eventos
        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Tipo / Evento:"), c);
        c.gridx = 1;
        add(cboEvento, c);

        // Fecha del evento (solo visual)
        c.gridx = 0; c.gridy = 1;
        add(new JLabel("Fecha evento:"), c);
        JTextField txtFechaEvento = new JTextField(10);
        txtFechaEvento.setEditable(false);
        c.gridx = 1; add(txtFechaEvento, c);

        // Descripción
        c.gridx = 0; c.gridy = 2;
        add(new JLabel("Descripción:"), c);
        c.gridx = 1; add(txtDescripcion, c);

        // Cantidad
        c.gridx = 0; c.gridy = 3;
        add(new JLabel("Cantidad:"), c);
        c.gridx = 1; add(spnCantidad, c);

        // Imagen
        c.gridx = 0; c.gridy = 4;
        add(new JLabel("Imagen:"), c);
        c.gridx = 1;
        lblPreview.setPreferredSize(new Dimension(200, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        add(lblPreview, c);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        UiUtils.estiloBotonSecundario(btnGuardar);
        UiUtils.estiloBotonPlano(btnVolver);
        UiUtils.estiloBotonSecundario(btnCambiarImagen);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);
        panelBotones.add(btnCambiarImagen);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        add(panelBotones, c);

        cargarEventosEnCombo();
        cargarDatosFormulario(txtFechaEvento);

        if (soloConsulta) {
            btnGuardar.setEnabled(false);
            btnCambiarImagen.setEnabled(false);
            cboEvento.setEnabled(false);
            txtDescripcion.setEditable(false);
            spnCantidad.setEnabled(false);
        }

        btnCambiarImagen.addActionListener(e -> seleccionarImagen());
        btnGuardar.addActionListener(e -> onGuardar());
        btnVolver.addActionListener(e -> cerrar());
    }

    private void cargarEventosEnCombo() {
        try {
            List<EventoSocial> eventos = controller.listarEventos();
            DefaultComboBoxModel<EventoSocial> model = new DefaultComboBoxModel<>();
            for (EventoSocial e : eventos) {
                model.addElement(e);
            }
            cboEvento.setModel(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar eventos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosFormulario(JTextField txtFecha) {
        if (foto.getEvento() != null) {
            cboEvento.setSelectedItem(foto.getEvento());
            txtFecha.setText(DateUtils.format(foto.getEvento().getFechaEvento()));
        } else if (cboEvento.getItemCount() > 0) {
            EventoSocial e = cboEvento.getItemAt(0);
            txtFecha.setText(DateUtils.format(e.getFechaEvento()));
        }

        cboEvento.addActionListener(e -> {
            EventoSocial ev = (EventoSocial) cboEvento.getSelectedItem();
            if (ev != null) {
                txtFecha.setText(DateUtils.format(ev.getFechaEvento()));
            }
        });

        if (foto.getDescripcion() != null) {
            txtDescripcion.setText(foto.getDescripcion());
        }
        if (foto.getCantidad() != 0) {
            spnCantidad.setValue(foto.getCantidad());
        }
        if (foto.getFoto() != null) {
            mostrarImagen(foto.getFoto());
        }
    }

    private void seleccionarImagen() {
        JFileChooser fc = new JFileChooser();
        int opc = fc.showOpenDialog(this);
        if (opc == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                byte[] data = ByteUtils.readFile(f);
                foto.setFoto(data);
                mostrarImagen(data);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer la imagen: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarImagen(byte[] data) {
        ImageIcon icon = new ImageIcon(data);
        Image img = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        lblPreview.setIcon(new ImageIcon(img));
    }

    private void onGuardar() {
        try {
            EventoSocial ev = (EventoSocial) cboEvento.getSelectedItem();
            foto.setEvento(ev);
            foto.setDescripcion(txtDescripcion.getText());
            foto.setCantidad((Integer) spnCantidad.getValue());
            // la imagen ya está en foto.setFoto()

            Validador.validar(foto);
            controller.guardarFoto(foto);
            JOptionPane.showMessageDialog(this, "Foto guardada correctamente.",
                    "OK", JOptionPane.INFORMATION_MESSAGE);
            cerrar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar foto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrar() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}
