package controller;

import app.FrmInicio;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import util.UiUtils;

public class AppController {

    private final FrmInicio frm;
    private final PersonasController personasCtrl;
    private final ArchivosController archivosCtrl;
    private final CiudadesController ciudadesCtrl;

    private final Timer liveSearchTimer;

    public AppController(FrmInicio frm) {
        this.frm = frm;
        this.personasCtrl = new PersonasController(frm.getPanelPersonas(), frm);
        this.archivosCtrl = new ArchivosController(frm.getPanelArchivos(), frm);
        this.ciudadesCtrl = new CiudadesController(frm.getPanelCiudades(), frm);

        liveSearchTimer = new Timer(250, e -> doSearch());
        liveSearchTimer.setRepeats(false);

        frm.getTfQuery().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { trigger(); }
            @Override public void removeUpdate(DocumentEvent e) { trigger(); }
            @Override public void changedUpdate(DocumentEvent e) { trigger(); }
            private void trigger() { if (!frm.getChkModoEdicion().isSelected()) liveSearchTimer.restart(); }
        });
    }

    public void onTabChanged(int tabIndex) {
        JComboBox<String> cb = frm.getCbCampo();
        cb.removeAllItems();
        switch (tabIndex) {
            case 0 -> {
                cb.addItem("ID"); cb.addItem("Nombre"); cb.addItem("Edad");
                personasCtrl.refrescarTabla(null, null);
                frm.getPanelPersonas().getFormPanel().setVisible(false);
            }
            case 1 -> {
                cb.addItem("ID"); cb.addItem("Nombre"); cb.addItem("MIME");
                archivosCtrl.refrescarTabla(null, null);
                frm.getPanelArchivos().getFormPanel().setVisible(false);
            }
            case 2 -> {
                cb.addItem("ID"); cb.addItem("Nombre");
                ciudadesCtrl.refrescarTabla(null, null);
                frm.getPanelCiudades().getFormPanel().setVisible(false);
            }
        }
        frm.getTfQuery().setText("");
        frm.getChkModoEdicion().setSelected(false);
        onToggleEdicion(false);
        frm.getBtnBuscarGuardar().setText("Buscar");
        frm.getBtnLimpiarCancelar().setText("Limpiar");
    }

    public void onToggleEdicion(boolean modo) {
        frm.getBtnBuscarGuardar().setText(modo ? "Guardar" : "Buscar");
        frm.getBtnLimpiarCancelar().setText(modo ? "Cancelar" : "Limpiar");
        switch (frm.getActiveTabIndex()) {
            case 0 -> frm.getPanelPersonas().getFormPanel().setVisible(modo);
            case 1 -> frm.getPanelArchivos().getFormPanel().setVisible(modo);
            case 2 -> frm.getPanelCiudades().getFormPanel().setVisible(modo);
        }
        if (modo) liveSearchTimer.stop();
    }

    /** Botón NUEVO: entra en edición y limpia formulario de la pestaña actual. */
    public void onNuevo() {
        frm.getChkModoEdicion().setSelected(true);
        onToggleEdicion(true);
        switch (frm.getActiveTabIndex()) {
            case 0 -> { frm.getPanelPersonas().getTabla().clearSelection(); frm.getPanelPersonas().clearFormulario(); }
            case 1 -> { frm.getPanelArchivos().getTabla().clearSelection(); frm.getPanelArchivos().clearFormulario(); }
            case 2 -> { frm.getPanelCiudades().getTabla().clearSelection(); frm.getPanelCiudades().clearFormulario(); }
        }
    }

    public void onBuscarGuardar() {
        boolean edicion = frm.getChkModoEdicion().isSelected();
        if (!edicion) { doSearch(); return; }

        int tab = frm.getActiveTabIndex();
        String campo = (String) frm.getCbCampo().getSelectedItem();
        String valor = frm.getTfQuery().getText().trim();
        try {
            switch (tab) {
                case 0 -> personasCtrl.guardarDesdeBarra(campo, valor);
                case 1 -> archivosCtrl.guardarDesdeBarra(campo, valor);
                case 2 -> ciudadesCtrl.guardarDesdeBarra(campo, valor);
            }
        } catch (Exception ex) { UiUtils.error(frm, "Error: " + ex.getMessage()); ex.printStackTrace(); }
    }

    public void onLimpiarCancelar() {
        boolean edicion = frm.getChkModoEdicion().isSelected();
        if (!edicion) {
            frm.getTfQuery().setText("");
        } else {
            frm.getChkModoEdicion().setSelected(false);
            onToggleEdicion(false);
        }
    }

    public void onVerSeleccion() {
        switch (frm.getActiveTabIndex()) {
            case 0 -> personasCtrl.verSeleccion();
            case 1 -> archivosCtrl.verSeleccion();
            case 2 -> ciudadesCtrl.verSeleccion();
        }
    }

    public void onEliminarSeleccionados() {
        switch (frm.getActiveTabIndex()) {
            case 0 -> personasCtrl.eliminarSeleccionados();
            case 1 -> archivosCtrl.eliminarSeleccionados();
            case 2 -> ciudadesCtrl.eliminarSeleccionados();
        }
    }

    private void doSearch() {
        int tab = frm.getActiveTabIndex();
        String campo = (String) frm.getCbCampo().getSelectedItem();
        String valor = frm.getTfQuery().getText().trim();
        try {
            switch (tab) {
                case 0 -> personasCtrl.refrescarTabla(campo, valor);
                case 1 -> archivosCtrl.refrescarTabla(campo, valor);
                case 2 -> ciudadesCtrl.refrescarTabla(campo, valor);
            }
        } catch (Exception ex) { UiUtils.error(frm, "Error: " + ex.getMessage()); ex.printStackTrace(); }
    }
}
