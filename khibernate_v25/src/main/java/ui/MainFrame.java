package ui;

import persistence.HibernateUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("AD 1a - Swing + Hibernate (Departamentos / Empleados)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Departamentos", new DepartamentosPanel());
        tabs.addTab("Empleados", new EmpleadosPanel());
        setContentPane(tabs);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                HibernateUtil.shutdown();
            }
        });
    }
}
