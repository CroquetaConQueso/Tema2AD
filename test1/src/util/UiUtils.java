package util;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class UiUtils {

    // Paleta
    public static final Color COL_PRIMARY = new Color(33,150,243);
    public static final Color COL_ACCENT  = new Color(0, 168, 132);
    public static final Color COL_DANGER  = new Color(219, 68, 55);
    public static final Color COL_BG      = new Color(245,248,252);

    private static final DateTimeFormatter EU =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void installTheme() {
        // Nimbus si está disponible
        try {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
        } catch (Exception ignored) {}
        // Botones con foco visible
        UIManager.put("Button.focus", Color.LIGHT_GRAY);
    }

    // Botones con estilo
    public static JButton btnPrimary(String text){ return style(new JButton(text), COL_PRIMARY, Color.WHITE); }
    public static JButton btnAccent(String text){ return style(new JButton(text), COL_ACCENT, Color.WHITE); }
    public static JButton btnDanger(String text){ return style(new JButton(text), COL_DANGER, Color.WHITE); }
    public static JButton btnLight(String text){ return style(new JButton(text), new Color(230,234,240), Color.DARK_GRAY); }

    private static JButton style(JButton b, Color bg, Color fg){
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(true); b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        return b;
    }

    // Campos y diálogos
    public static void info(Component p, String msg){ JOptionPane.showMessageDialog(p,msg,"Info",JOptionPane.INFORMATION_MESSAGE);}
    public static void error(Component p, String msg){ JOptionPane.showMessageDialog(p,msg,"Error",JOptionPane.ERROR_MESSAGE);}
    public static boolean confirm(Component p, String msg){
        return JOptionPane.showConfirmDialog(p,msg,"Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }

    public static void showSelectionDialog(Window parent, JTable table){
        int[] rows = table.getSelectedRows();
        if (rows.length==0) { info(parent,"No hay selección."); return; }
        StringBuilder sb = new StringBuilder("Selección:\n");
        for (int r: rows){
            sb.append("[ ");
            for (int c=0;c<table.getColumnCount();c++){
                sb.append(table.getColumnName(c)).append("=")
                  .append(table.getValueAt(r,c)).append(" ");
            }
            sb.append("]\n");
        }
        JTextArea ta = new JTextArea(sb.toString(), 12, 70);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(parent, new JScrollPane(ta), "Datos seleccionados", JOptionPane.PLAIN_MESSAGE);
    }

    public static JMenuBar simpleStyleMenu(JFrame owner){
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu("Estilo");
        ButtonGroup g = new ButtonGroup();
        addLaf(owner, m, g, "Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        addLaf(owner, m, g, "Sistema", UIManager.getSystemLookAndFeelClassName());
        addLaf(owner, m, g, "Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
        mb.add(m); return mb;
    }
    private static void addLaf(JFrame owner, JMenu m, ButtonGroup g, String name, String cn){
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        g.add(r); m.add(r);
        if ("Nimbus".equals(name)) r.setSelected(true);
        r.addActionListener(ev -> {
            try {
                UIManager.setLookAndFeel(cn);
                SwingUtilities.updateComponentTreeUI(owner);
            } catch (Exception e) { error(owner, "No se pudo aplicar estilo."); }
        });
    }

    // ==== Fecha europea (dd/MM/yyyy) ====

    /** Campo con máscara dd/MM/yyyy que sí permite escribir. */
    public static JFormattedTextField dateFieldEU() {
        try {
            MaskFormatter mf = new MaskFormatter("##/##/####");
            mf.setPlaceholderCharacter('_');
            JFormattedTextField f = new JFormattedTextField(mf);
            f.setColumns(10);
            f.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
            return f;
        } catch (Exception e) {
            // fallback simple
            JFormattedTextField f = new JFormattedTextField();
            f.setColumns(10);
            return f;
        }
    }

    /** Pone la fecha de hoy en formato europeo en el campo. */
    public static void setToday(JFormattedTextField f){
        f.setText(toDateEU(LocalDate.now()));
    }

    /** Parsea dd/MM/yyyy a LocalDate (STRICT). */
    public static LocalDate parseEU(String ddMMyyyy){
        try {
            if (ddMMyyyy==null || ddMMyyyy.isBlank() || ddMMyyyy.contains("_")) return null;
            return LocalDate.parse(ddMMyyyy, EU);
        } catch (Exception e){ return null; }
    }

    /** Convierte LocalDate/objeto fecha a dd/MM/yyyy. */
    public static String toDateEU(Object date){
        if (date==null) return "";
        if (date instanceof LocalDate ld) return ld.format(EU);
        try { // por si llega un java.sql.Date o String "yyyy-MM-dd"
            if (date instanceof java.sql.Date d) return d.toLocalDate().format(EU);
            if (date instanceof String s) {
                if (s.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(s).format(EU);
                return s;
            }
        } catch (Exception ignored) {}
        return String.valueOf(date);
    }

    // Utilidades varias
    public static Integer getSelectedId(JTable t){
        int r = t.getSelectedRow();
        if (r<0) return null;
        Object v = t.getValueAt(r,0);
        return (v instanceof Integer) ? (Integer) v : null;
    }

    public static void check(boolean cond, String msg){
        if (!cond) throw new IllegalArgumentException(msg);
    }
}
