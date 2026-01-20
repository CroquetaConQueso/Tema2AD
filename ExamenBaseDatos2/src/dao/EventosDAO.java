package dao;

import model.EventoSocial;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventosDAO {

    public List<EventoSocial> buscarPorFechas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT id_evento, nombre, fecha_evento FROM eventos_sociales " +
                     "WHERE fecha_evento BETWEEN ? AND ? ORDER BY fecha_evento";
        List<EventoSocial> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    public List<EventoSocial> listarTodos() throws SQLException {
        String sql = "SELECT id_evento, nombre, fecha_evento FROM eventos_sociales ORDER BY fecha_evento";
        List<EventoSocial> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public EventoSocial findById(int id) throws SQLException {
        String sql = "SELECT id_evento, nombre, fecha_evento FROM eventos_sociales WHERE id_evento = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int insertar(EventoSocial e) throws SQLException {
        String sql = "INSERT INTO eventos_sociales(nombre, fecha_evento) VALUES (?,?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setDate(2, Date.valueOf(e.getFechaEvento()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setIdEvento(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void actualizar(EventoSocial e) throws SQLException {
        String sql = "UPDATE eventos_sociales SET nombre = ?, fecha_evento = ? WHERE id_evento = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setDate(2, Date.valueOf(e.getFechaEvento()));
            ps.setInt(3, e.getIdEvento());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM eventos_sociales WHERE id_evento = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private EventoSocial mapRow(ResultSet rs) throws SQLException {
        EventoSocial e = new EventoSocial();
        e.setIdEvento(rs.getInt("id_evento"));
        e.setNombre(rs.getString("nombre"));
        e.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
        return e;
    }
}
