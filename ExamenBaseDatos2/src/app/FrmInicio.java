package app;

import controller.AppController;
import util.UiUtils;
import view.PanelBusquedaEventos;
import view.PanelBusquedaFotos;

import javax.swing.*;
import java.awt.*;

public class FrmInicio extends JFrame {

    private final CardLayout cardLayoutCentral = new CardLayout();
    private final JPanel panelCentral = new JPanel(cardLayoutCentral);

    private final PanelBusquedaEventos panelEventos;
    private final PanelBusquedaFotos panelFotos;

    public FrmInicio() {
        UiUtils.setSystemLookAndFeel();
        AppController controller = AppController.getInstance();

        this.panelEventos = new PanelBusquedaEventos(controller);
        this.panelFotos = new PanelBusquedaFotos(controller);

        initUi();
        setTitle("Examen Base de Datos 2 - I.E.S. Fuengirola Nº1");
        setSize(1024, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ================= ENCABEZADO =================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x993300));

        JLabel lblLogo;
        try {
            ImageIcon icon = new ImageIcon(
                    FrmInicio.class.getResource("/img/logo_web_p-1.png"));
            lblLogo = new JLabel(icon);
        } catch (Exception e) {
            lblLogo = new JLabel("LOGO", SwingConstants.CENTER);
            lblLogo.setForeground(Color.WHITE);
        }
        lblLogo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel lblTitulo = new JLabel("I.E.S. Fuengirola Nº 1", SwingConstants.LEFT);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 24f));

        JLabel lblCurso = new JLabel("Curso 2025/2026", SwingConstants.LEFT);
        lblCurso.setForeground(Color.WHITE);

        JPanel panelTextos = new JPanel();
        panelTextos.setOpaque(false);
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.add(lblTitulo);
        panelTextos.add(lblCurso);

        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCentro.setOpaque(false);
        panelCentro.add(lblLogo);
        panelCentro.add(panelTextos);

        header.add(panelCentro, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // =============== MENÚ LATERAL ===============
        JPanel menu = new JPanel(new GridLayout(0, 1));
        menu.setBackground(new Color(0xD9B382));

        JButton btnEventos = new JButton("Eventos sociales");
        UiUtils.estiloBotonPlano(btnEventos);

        JButton btnFotos = new JButton("Fotos de eventos");
        UiUtils.estiloBotonPlano(btnFotos);

        menu.add(btnEventos);
        menu.add(btnFotos);
        add(menu, BorderLayout.WEST);

        // =============== ZONA CENTRAL ===============
        panelCentral.add(panelEventos, "EVENTOS");
        panelCentral.add(panelFotos, "FOTOS");
        add(panelCentral, BorderLayout.CENTER);

        // =============== PIE DE PÁGINA ===============
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x993300));
        JLabel lblCopy = new JLabel(
                "@Copyright. Carlos Torres León. Todos los derechos reservados",
                SwingConstants.CENTER);
        lblCopy.setForeground(Color.WHITE);
        footer.add(lblCopy, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Listeners menú lateral
        btnEventos.addActionListener(e -> cardLayoutCentral.show(panelCentral, "EVENTOS"));
        btnFotos.addActionListener(e -> cardLayoutCentral.show(panelCentral, "FOTOS"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }
}
