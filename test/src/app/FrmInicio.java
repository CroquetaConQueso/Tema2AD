package app;

import javax.swing.*;
import java.awt.*;
import controller.AppController;
import view.PanelPersonas;
import view.PanelArchivos;
import view.PanelCiudades;
import util.Consts;
import util.UiUtils;

public class FrmInicio extends JFrame {

    private final JTabbedPane tabs;
    private final JPanel topBar;
    private final JComboBox<String> cbCampo;
    private final JTextField tfQuery;
    private final JButton btnBuscarGuardar;
    private final JButton btnLimpiarCancelar;
    private final JCheckBox chkModoEdicion;
    private final JButton btnNuevo;               // <— NUEVO
    private final JButton btnVerSeleccion;
    private final JButton btnEliminarSel;

    private final PanelPersonas panelPersonas;
    private final PanelArchivos panelArchivos;
    private final PanelCiudades panelCiudades;

    private final AppController controller;

    public FrmInicio() {
        setTitle("test — Examen (Swing + MySQL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        UiUtils.applyNimbusLF();

        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbCampo = new JComboBox<>();
        tfQuery = new JTextField(25);
        btnBuscarGuardar = new JButton("Buscar");
        btnLimpiarCancelar = new JButton("Limpiar");
        chkModoEdicion = new JCheckBox("Modo edición");
        btnNuevo = new JButton("Nuevo");          // <— NUEVO
        btnVerSeleccion = new JButton("Ver selección");
        btnEliminarSel = new JButton("Eliminar seleccionados");

        topBar.setBackground(Consts.TOPBAR_BG);
        btnBuscarGuardar.setBackground(Consts.ACCENT);
        btnBuscarGuardar.setForeground(Color.WHITE);

        topBar.add(new JLabel("Campo:"));
        topBar.add(cbCampo);
        topBar.add(new JLabel("Valor:"));
        topBar.add(tfQuery);
        topBar.add(btnBuscarGuardar);
        topBar.add(btnLimpiarCancelar);
        topBar.add(chkModoEdicion);
        topBar.add(btnNuevo);                     // <— NUEVO
        topBar.add(btnVerSeleccion);
        topBar.add(btnEliminarSel);

        tabs = new JTabbedPane();
        panelPersonas = new PanelPersonas();
        panelArchivos = new PanelArchivos();
        panelCiudades = new PanelCiudades();

        tabs.addTab("Personas", panelPersonas);
        tabs.addTab("Archivos", panelArchivos);
        tabs.addTab("Ciudades", panelCiudades);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);

        controller = new AppController(this);

        tabs.addChangeListener(e -> controller.onTabChanged(getActiveTabIndex()));
        btnBuscarGuardar.addActionListener(e -> controller.onBuscarGuardar());
        btnLimpiarCancelar.addActionListener(e -> controller.onLimpiarCancelar());
        chkModoEdicion.addActionListener(e -> controller.onToggleEdicion(chkModoEdicion.isSelected()));
        btnNuevo.addActionListener(e -> controller.onNuevo());     // <— NUEVO
        btnVerSeleccion.addActionListener(e -> controller.onVerSeleccion());
        btnEliminarSel.addActionListener(e -> controller.onEliminarSeleccionados());

        controller.onTabChanged(0);
    }

    public int getActiveTabIndex() { return tabs.getSelectedIndex(); }
    public JComboBox<String> getCbCampo() { return cbCampo; }
    public JTextField getTfQuery() { return tfQuery; }
    public JButton getBtnBuscarGuardar() { return btnBuscarGuardar; }
    public JButton getBtnLimpiarCancelar() { return btnLimpiarCancelar; }
    public JCheckBox getChkModoEdicion() { return chkModoEdicion; }
    public JButton getBtnNuevo() { return btnNuevo; }

    public PanelPersonas getPanelPersonas() { return panelPersonas; }
    public PanelArchivos getPanelArchivos() { return panelArchivos; }
    public PanelCiudades getPanelCiudades() { return panelCiudades; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }
}
