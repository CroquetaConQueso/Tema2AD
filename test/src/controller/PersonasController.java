package controller;

import app.FrmInicio;
import view.PanelPersonas;
import dao.PersonasDAO;
import model.Persona;
import util.UiUtils;
import validation.Validador;

import java.util.List;

public class PersonasController {

    private final PanelPersonas view;
    private final PersonasDAO dao;
    private final FrmInicio frm;

    public PersonasController(PanelPersonas view, FrmInicio frm) {
        this.view = view;
        this.dao = new PersonasDAO();
        this.frm = frm;
        this.view.setController(this);
    }

    public void refrescarTabla(String campo, String valor) {
        List<Persona> lista = dao.buscar(campo, valor, 500, 0);
        view.cargarTabla(lista);
    }

    public void guardarDesdeBarra(String campo, String valor) {
        Persona p = view.leerFormulario();
        Validador.validarPersona(p);
        if (view.getTabla().getSelectedRowCount() == 1 && p.getId() != null) {
            dao.actualizar(p);
            UiUtils.info(frm, "Persona actualizada.");
        } else {
            dao.insertar(p);
            UiUtils.info(frm, "Persona insertada.");
        }
        refrescarTabla(null, null);
    }

    public void verSeleccion() {
        List<Persona> sel = view.obtenerSeleccion();
        UiUtils.mostrarSeleccion(frm, sel);
    }

    public void eliminarSeleccionados() {
        List<Persona> sel = view.obtenerSeleccion();
        if (sel.isEmpty()) { UiUtils.info(frm, "No hay filas seleccionadas."); return; }
        if (!UiUtils.confirm(frm, "¿Eliminar " + sel.size() + " personas?")) return;
        int count = 0;
        for (Persona p : sel) count += dao.eliminar(p.getId());
        UiUtils.info(frm, "Eliminadas: " + count);
        refrescarTabla(null, null);
    }
}
