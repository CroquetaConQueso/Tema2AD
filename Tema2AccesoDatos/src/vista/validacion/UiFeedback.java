package vista.validacion;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class UiFeedback {
    private UiFeedback(){}

    private static final Border ERROR_BORDER = BorderFactory.createLineBorder(new Color(0xD32F2F), 2);
    private static final Border OK_BORDER;

    static {
        // Fallback si el L&F no define borde por defecto
        Border b = UIManager.getBorder("TextField.border");
        OK_BORDER = (b != null ? b : BorderFactory.createLineBorder(new Color(0xBDBDBD)));
    }

    public static void markError(JComponent c, String tooltip){
        if (c == null) return;
        c.setBorder(ERROR_BORDER);
        c.setToolTipText(tooltip);
    }

    public static void clear(JComponent c){
        if (c == null) return;
        c.setBorder(OK_BORDER);
        c.setToolTipText(null);
    }

    /** Limpia varios componentes de una vez. */
    public static void clearAll(JComponent... comps){
        if (comps == null) return;
        for (JComponent c : comps) clear(c);
    }

    /** Muestra un diálogo con todos los errores concatenados. */
    public static void showErrors(Component parent, java.util.List<String> errores){
        if (errores == null || errores.isEmpty()) return;
        JOptionPane.showMessageDialog(parent,
                String.join("\n", errores),
                "Revisa los datos",
                JOptionPane.WARNING_MESSAGE);
    }
}
