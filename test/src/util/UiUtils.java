package util;

import view.DialogSeleccion;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UiUtils {

    public static void applyNimbusLF() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName()); break;
                }
            }
        } catch (Exception ignored) {}
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static Integer getSelectedId(JTable tabla) {
        int row = tabla.getSelectedRow();
        if (row < 0) return null;
        Object val = tabla.getValueAt(row, 0);
        return val == null ? null : Integer.valueOf(String.valueOf(val));
    }

    public static Integer valInt(Object o) {
        return Integer.valueOf(String.valueOf(o));
    }
    public static Integer valIntNullable(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        if (s.equalsIgnoreCase("null") || s.isBlank()) return null;
        return Integer.valueOf(s);
    }

    public static <T> List<T> tableSelectionToList(JTable t, Function<List<Object>, T> mapper) {
        int[] rows = t.getSelectedRows();
        List<T> out = new ArrayList<>();
        TableModel m = t.getModel();
        for (int r : rows) {
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < m.getColumnCount(); c++) row.add(m.getValueAt(r, c));
            out.add(mapper.apply(row));
        }
        return out;
    }

    public static void mostrarSeleccion(Window owner, List<?> objs) {
        if (objs == null || objs.isEmpty()) {
            info(owner, "Sin elementos seleccionados.");
            return;
        }
        String[] cols;
        List<Object[]> rows = new ArrayList<>();

        Object first = objs.get(0);
        if (first.getClass().getSimpleName().equals("Persona")) {
            cols = new String[]{"ID", "Nombre", "Edad", "CiudadId"};
            for (Object o : objs) {
                model.Persona p = (model.Persona) o;
                rows.add(new Object[]{p.getId(), p.getNombre(), p.getEdad(), p.getCiudadId()});
            }
        } else if (first.getClass().getSimpleName().equals("ArchivoBinario")) {
            cols = new String[]{"ID", "Nombre", "MIME", "Tamaño", "Creado"};
            for (Object o : objs) {
                model.ArchivoBinario a = (model.ArchivoBinario) o;
                rows.add(new Object[]{a.getId(), a.getNombreOriginal(), a.getMime(),
                        (a.getDatos() != null ? a.getDatos().length : "-"), a.getCreadoAt()});
            }
        } else if (first.getClass().getSimpleName().equals("Ciudad")) {
            cols = new String[]{"ID", "Nombre"};
            for (Object o : objs) {
                model.Ciudad c = (model.Ciudad) o;
                rows.add(new Object[]{c.getId(), c.getNombre()});
            }
        } else {
            cols = new String[]{"Objeto"};
            for (Object o : objs) rows.add(new Object[]{String.valueOf(o)});
        }

        DialogSeleccion dlg = new DialogSeleccion(owner, cols, rows);
        dlg.setVisible(true);
    }
}
