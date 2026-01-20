package controller;

import app.FrmInicio;
import view.PanelCiudades;
import dao.CiudadesDAO;
import model.Ciudad;
import util.UiUtils;
import validation.Validador;

import java.util.List;

public class CiudadesController {

    private final PanelCiudades view;
    private final CiudadesDAO dao;
    private final FrmInicio frm;

    public CiudadesController(PanelCiudades view, FrmInicio frm) {
        this.view = view;
        this.dao = new CiudadesDAO();
        this.frm = frm;
        this.view.setController(this);
    }

    public void refrescarTabla(String campo, String valor) {
        List<Ciudad> lista = dao.buscar(campo, valor, 500, 0);
        view.cargarTabla(lista);
    }

    public void guardarDesdeBarra(String campo, String valor) {
        Ciudad c = view.leerFormulario();
        Validador.validarCiudad(c);
        if (view.getTabla().getSelectedRowCount() == 1 && c.getId() != null) {
            dao.actualizar(c);
            UiUtils.info(frm, "Ciudad actualizada.");
        } else {
            dao.insertar(c);
            UiUtils.info(frm, "Ciudad insertada.");
        }
        refrescarTabla(null, null);
    }

    public void verSeleccion() {
        List<Ciudad> sel = view.obtenerSeleccion();
        UiUtils.mostrarSeleccion(frm, sel);
    }

    public void eliminarSeleccionados() {
        List<Ciudad> sel = view.obtenerSeleccion();
        if (sel.isEmpty()) { UiUtils.info(frm, "No hay filas seleccionadas."); return; }
        if (!UiUtils.confirm(frm, "¿Eliminar " + sel.size() + " ciudades?")) return;
        int count = 0;
        for (Ciudad c : sel) count += dao.eliminar(c.getId());
        UiUtils.info(frm, "Eliminadas: " + count);
        refrescarTabla(null, null);
    }
}
