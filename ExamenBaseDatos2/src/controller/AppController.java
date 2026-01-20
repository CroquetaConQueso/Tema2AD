package controller;

import dao.EventosDAO;
import dao.FotosDAO;
import model.EventoSocial;
import model.FotoEventoSocial;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AppController {

    private static AppController instance;

    private final EventosDAO eventosDAO;
    private final FotosDAO fotosDAO;

    private AppController() {
        this.eventosDAO = new EventosDAO();
        this.fotosDAO = new FotosDAO();
    }

    public static AppController getInstance() {
        if (instance == null) {
            instance = new AppController();
        }
        return instance;
    }

    // --- Eventos ---

    public List<EventoSocial> listarEventos() throws SQLException {
        return eventosDAO.listarTodos();
    }

    public List<EventoSocial> buscarEventos(LocalDate desde, LocalDate hasta) throws SQLException {
        return eventosDAO.buscarPorFechas(desde, hasta);
    }

    public EventoSocial guardarEvento(EventoSocial e) throws SQLException {
        if (e.getIdEvento() == 0) {
            eventosDAO.insertar(e);
        } else {
            eventosDAO.actualizar(e);
        }
        return e;
    }

    public void eliminarEvento(int id) throws SQLException {
        eventosDAO.eliminar(id);
    }

    // --- Fotos ---

    public List<FotoEventoSocial> listarFotos() throws SQLException {
        return fotosDAO.listarTodas();
    }

    public List<FotoEventoSocial> buscarFotos(LocalDate desde, LocalDate hasta) throws SQLException {
        return fotosDAO.buscarPorFechas(desde, hasta);
    }

    public FotoEventoSocial guardarFoto(FotoEventoSocial f) throws SQLException {
        if (f.getIdFoto() == 0) {
            fotosDAO.insertar(f);
        } else {
            fotosDAO.actualizar(f);
        }
        return f;
    }

    public void eliminarFoto(int idFoto) throws SQLException {
        fotosDAO.eliminar(idFoto);
    }

    public void eliminarFotos(List<Integer> ids) throws SQLException {
        fotosDAO.eliminarVarios(ids);
    }
}
