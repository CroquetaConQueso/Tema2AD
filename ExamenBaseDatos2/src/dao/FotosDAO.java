package dao;

import model.EventoSocial;
import model.FotoEventoSocial;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FotosDAO {

    // Lista o busca por rango de fechas (usando fecha del evento)
    public List<FotoEventoSocial> buscarPorFechas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT f.id_foto, f.id_evento, f.foto, f.descripcion, f.cantidad,
                   e.nombre, e.fecha_evento
            FROM fotos_evento_social f
            JOIN eventos_sociales e ON f.id_evento = e.id_evento
            WHERE e.fecha_evento BETWEEN ? AND ?
            ORDER BY e.fecha_evento, f.id_foto
            """;

        List<FotoEventoSocial> lista = new ArrayList<>();
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

    public List<FotoEventoSocial> listarTodas() throws SQLException {
        String sql = """
            SELECT f.id_foto, f.id_evento, f.foto, f.descripcion, f.cantidad,
                   e.nombre, e.fecha_evento
            FROM fotos_evento_social f
            JOIN eventos_sociales e ON f.id_evento = e.id_evento
            ORDER BY e.fecha_evento, f.id_foto
            """;
        List<FotoEventoSocial> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public FotoEventoSocial findById(int idFoto) throws SQLException {
        String sql = """
            SELECT f.id_foto, f.id_evento, f.foto, f.descripcion, f.cantidad,
                   e.nombre, e.fecha_evento
            FROM fotos_evento_social f
            JOIN eventos_sociales e ON f.id_evento = e.id_evento
            WHERE f.id_foto = ?
            """;
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFoto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int insertar(FotoEventoSocial f) throws SQLException {
        String sql = "INSERT INTO fotos_evento_social(id_evento, foto, descripcion, cantidad) VALUES (?,?,?,?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, f.getEvento().getIdEvento());
            ps.setBytes(2, f.getFoto());
            ps.setString(3, f.getDescripcion());
            ps.setInt(4, f.getCantidad());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    f.setIdFoto(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void actualizar(FotoEventoSocial f) throws SQLException {
        String sql = "UPDATE fotos_evento_social SET id_evento=?, foto=?, descripcion=?, cantidad=? WHERE id_foto=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, f.getEvento().getIdEvento());
            ps.setBytes(2, f.getFoto());
            ps.setString(3, f.getDescripcion());
            ps.setInt(4, f.getCantidad());
            ps.setInt(5, f.getIdFoto());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idFoto) throws SQLException {
        String sql = "DELETE FROM fotos_evento_social WHERE id_foto=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFoto);
            ps.executeUpdate();
        }
    }

    public void eliminarVarios(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;
        String sql = "DELETE FROM fotos_evento_social WHERE id_foto = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Integer id : ids) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private FotoEventoSocial mapRow(ResultSet rs) throws SQLException {
        EventoSocial e = new EventoSocial();
        e.setIdEvento(rs.getInt("id_evento"));
        e.setNombre(rs.getString("nombre"));
        e.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());

        FotoEventoSocial f = new FotoEventoSocial();
        f.setIdFoto(rs.getInt("id_foto"));
        f.setEvento(e);
        f.setFoto(rs.getBytes("foto"));
        f.setDescripcion(rs.getString("descripcion"));
        f.setCantidad(rs.getInt("cantidad"));

        return f;
    }
}

