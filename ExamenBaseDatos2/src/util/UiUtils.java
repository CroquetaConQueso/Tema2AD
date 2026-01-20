package util;

import javax.swing.*;
import java.awt.*;

public class UiUtils {

    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    // Botón "principal" (Buscar)
    public static void estiloBotonPrimario(JButton btn) {
        btn.setBackground(new Color(0xF0A64A)); // naranja claro
        btn.setForeground(Color.BLACK);         // TEXTO NEGRO
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
    }

    // Botones tipo "Alta"
    public static void estiloBotonSecundario(JButton btn) {
        btn.setBackground(new Color(0xE89B3A));
        btn.setForeground(Color.BLACK);         // TEXTO NEGRO
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
    }

    // Botones de peligro (Borrar, Borrado múltiple)
    public static void estiloBotonPeligro(JButton btn) {
        btn.setBackground(new Color(0xFF6666)); // rojo claro
        btn.setForeground(Color.BLACK);         // TEXTO NEGRO
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
    }

    // Botones planos (menú lateral, consultar, modificar…)
    public static void estiloBotonPlano(JButton btn) {
        btn.setBackground(new Color(0xE0C080));
        btn.setForeground(Color.BLACK);         // TEXTO NEGRO
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
    }
}
