package vista;

import javax.swing.*;
import java.awt.*;
import vista.datos.TabDatos;


import vista.datos.TabDatos;  // <-- Asegúrate de que existe esta clase/pestaña

public class FrmInicio extends JFrame {

    private JTabbedPane tabs;
    private JLabel titulo;
    private JPanel header;

    public FrmInicio() {
        setTitle("Tema2_1_AccesoDatos · One Page");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Tamaño y posición similares a tus otros proyectos
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) (screen.width * 0.90);
        int h = (int) (screen.height * 0.85);
        setSize(Math.max(1200, w), Math.max(700, h));
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        initComponents();  // construye la UI

        // Mostrar
        setVisible(true);
    }

    private void initComponents() {
        // Cabecera
        titulo = new JLabel("Base de datos: tema2_1_accesodatos", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
        header.add(titulo, BorderLayout.WEST);

        // Pestañas
        tabs = new JTabbedPane();
        tabs.addTab("Datos", new TabDatos());   // única pestaña del ejercicio

        // Layout principal
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // Punto de entrada (como usas en tus proyectos)
    public static void main(String[] args) {
        // (Opcional) Nimbus Look&Feel
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(i.getName())) {
                    UIManager.setLookAndFeel(i.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(FrmInicio::new);
    }
}
