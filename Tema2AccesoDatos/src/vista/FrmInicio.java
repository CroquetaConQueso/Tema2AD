package vista;

import javax.swing.*;
import java.awt.*;

import vista.clientes.TabClientes;
import vista.direcciones.TabDirecciones;
import vista.contactos.TabContactos;
import vista.archivos.TabArchivos;

public class FrmInicio extends JFrame {

    public FrmInicio() {
        setTitle("Empresa · One Page (empresa_clientes)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) (screen.width * 0.90);
        int h = (int) (screen.height * 0.85);
        setSize(Math.max(1200, w), Math.max(700, h));
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JLabel titulo = new JLabel("Base de datos: empresa_clientes", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,12,4,12));
        header.add(titulo, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes",    new TabClientes());
        tabs.addTab("Direcciones", new TabDirecciones());
        tabs.addTab("Contactos",   new TabContactos());
        tabs.addTab("Archivos",    new TabArchivos()); // NUEVA PESTAÑA

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo i: UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }
}
