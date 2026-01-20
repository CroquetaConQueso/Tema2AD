package view;

import controller.AppController;
import model.EventoSocial;
import util.DateUtils;
import util.UiUtils;
import validation.Validador;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class PanelEventoForm extends JPanel {

    private final AppController controller;
    private EventoSocial evento;
    private boolean esAlta;
    private boolean soloConsulta;

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtFecha = new JTextField(10);
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnVolver = new JButton("Volver");

    public PanelEventoForm(JPanel parent, AppController controller,
                           EventoSocial evento, boolean esAlta) {
        this.controller = controller;
        this.evento = (evento != null) ? evento : new EventoSocial();
        this.esAlta = esAlta;

        setLayout(new GridBagLayout());
        setBackground(new Color(0xF7E2BB));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Nombre:"), c);
        c.gridx = 1; add(txtNombre, c);

        c.gridx = 0; c.gridy = 1;
        add(new JLabel("Fecha (dd/mm/yyyy):"), c);
        c.gridx = 1; add(txtFecha, c);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        UiUtils.estiloBotonSecundario(btnGuardar);
        UiUtils.estiloBotonPlano(btnVolver);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        add(panelBotones, c);

        cargarDatosEnFormulario();

        btnGuardar.addActionListener(e -> onGuardar());
        btnVolver.addActionListener(e -> cerrarVentana());
    }

    public void setSoloConsulta(boolean soloConsulta) {
        this.soloConsulta = soloConsulta;
        txtNombre.setEditable(!soloConsulta);
        txtFecha.setEditable(!soloConsulta);
        btnGuardar.setEnabled(!soloConsulta);
    }

    private void cargarDatosEnFormulario() {
        if (evento.getIdEvento() != 0) {
            txtNombre.setText(evento.getNombre());
            txtFecha.setText(DateUtils.format(evento.getFechaEvento()));
        }
    }

    private void onGuardar() {
        try {
            evento.setNombre(txtNombre.getText());
            evento.setFechaEvento(DateUtils.parse(txtFecha.getText()));
            Validador.validar(evento);
            controller.guardarEvento(evento);
            JOptionPane.showMessageDialog(this,
                    "Evento guardado correctamente.",
                    "OK", JOptionPane.INFORMATION_MESSAGE);
            cerrarVentana();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarVentana() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}
