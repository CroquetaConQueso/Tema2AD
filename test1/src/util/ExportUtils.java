package util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;

public class ExportUtils {

    /** Exporta el contenido de la JTable a CSV (con separador coma, escapando comillas). */
    public static void exportCsv(JFrame parent, JTable table) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("tabla.csv"));
        if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try (FileWriter w = new FileWriter(f, false)) {
            TableModel m = table.getModel();
            // cabeceras
            for (int c=0;c<m.getColumnCount();c++){
                if (c>0) w.write(",");
                w.write(escape(m.getColumnName(c)));
            }
            w.write("\n");
            // filas
            for (int r=0;r<m.getRowCount();r++){
                for (int c=0;c<m.getColumnCount();c++){
                    if (c>0) w.write(",");
                    Object val = m.getValueAt(r,c);
                    w.write(escape(String.valueOf(val)));
                }
                w.write("\n");
            }
            UiUtils.info(parent,"CSV exportado en: "+f.getAbsolutePath());
        } catch (Exception ex) {
            UiUtils.error(parent,"Error exportando CSV: "+ex.getMessage());
        }
    }

    private static String escape(String s){
        String v = s.replace("\"","\"\"");
        return "\""+v+"\"";
    }

    /** Imprime la tabla: selecciona en el diálogo una impresora PDF (p.ej., Microsoft Print to PDF). */
    public static void printTableAsPdf(JFrame parent, JTable table) {
        try {
            boolean ok = table.print(JTable.PrintMode.FIT_WIDTH, null, null, true, null, true);
            if (ok) UiUtils.info(parent,"Enviado a impresión. Elige 'PDF' en el diálogo.");
        } catch (PrinterException e) {
            UiUtils.error(parent,"Error al imprimir: "+e.getMessage());
        }
    }
}
