package controller;

import app.FrmInicio;
import view.PanelArchivos;
import dao.ArchivosDAO;
import model.ArchivoBinario;
import util.ByteUtils;
import util.MimeDetect;
import util.UiUtils;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ArchivosController {

    private final PanelArchivos view;
    private final ArchivosDAO dao;
    private final FrmInicio frm;

    public ArchivosController(PanelArchivos view, FrmInicio frm) {
        this.view = view;
        this.dao = new ArchivosDAO();
        this.frm = frm;
        this.view.setController(this);
    }

    public void refrescarTabla(String campo, String valor) {
        List<ArchivoBinario> lista = dao.buscar(campo, valor, 500, 0);
        view.cargarTabla(lista);
    }

    public void guardarDesdeBarra(String campo, String valor) {
        ArchivoBinario a = view.leerFormularioMetadatos();
        if (view.getTabla().getSelectedRowCount() == 1 && a.getId() != null) {
            dao.actualizarMetadatos(a);
            UiUtils.info(frm, "Archivo actualizado (metadatos).");
            refrescarTabla(null, null);
        } else {
            UiUtils.info(frm, "Selecciona una fila para actualizar metadatos o usa Importar para añadir nuevos.");
        }
    }

    public void importar() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(frm);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f.length() > 10_000_000) {
                UiUtils.error(frm, "El archivo supera 10MB.");
                return;
            }
            try {
                byte[] datos = Files.readAllBytes(f.toPath());
                String mime = MimeDetect.detect(datos, f.getName());
                ArchivoBinario ab = new ArchivoBinario(null, f.getName(), mime, datos, null);
                dao.insertar(ab);
                UiUtils.info(frm, "Importado: " + f.getName());
                refrescarTabla(null, null);
            } catch (Exception ex) {
                UiUtils.error(frm, "Error importando: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /** Exporta recargando el BLOB real por ID (evita datos=null). */
    public void exportarSeleccion() {
        List<ArchivoBinario> sel = view.obtenerSeleccion();
        if (sel.isEmpty()) { UiUtils.info(frm, "No hay filas seleccionadas."); return; }

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = fc.showSaveDialog(frm);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File dir = fc.getSelectedFile();
        int ok = 0;
        for (ArchivoBinario meta : sel) {
            try {
                ArchivoBinario ab = dao.findById(meta.getId()); // ← BLOB real
                if (ab == null || ab.getDatos() == null) throw new IllegalStateException("Sin datos binarios.");
                File out = new File(dir, ab.getNombreOriginal());
                ByteUtils.writeBytes(out, ab.getDatos());
                ok++;
            } catch (Exception ex) {
                UiUtils.error(frm, "Fallo exportando " + meta.getNombreOriginal() + ": " + ex.getMessage());
            }
        }
        UiUtils.info(frm, "Exportados: " + ok + "/" + sel.size());
    }

    public void verSeleccion() {
        List<ArchivoBinario> sel = view.obtenerSeleccion();
        UiUtils.mostrarSeleccion(frm, sel);
    }

    public void eliminarSeleccionados() {
        List<ArchivoBinario> sel = view.obtenerSeleccion();
        if (sel.isEmpty()) { UiUtils.info(frm, "No hay filas seleccionadas."); return; }
        if (!UiUtils.confirm(frm, "¿Eliminar " + sel.size() + " archivos?")) return;
        int count = 0;
        for (ArchivoBinario a : sel) count += dao.eliminar(a.getId());
        UiUtils.info(frm, "Eliminados: " + count);
        refrescarTabla(null, null);
    }
}
