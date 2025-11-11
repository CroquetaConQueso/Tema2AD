package dao;

import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivosDAO {

    public List<Object[]> listarTodos() throws SQLException {
        String sql = """
            SELECT id_archivo, id_cliente, nombre, mime, tamano, creado_en
            FROM archivos
            ORDER BY id_archivo
        """;
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Object[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Object[]{
                        rs.getInt("id_archivo"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("mime"),
                        rs.getLong("tamano"),
                        rs.getTimestamp("creado_en").toString()
                });
            }
            return out;
        }
    }

    public Object[] getById(int id) throws SQLException {
        String sql = """
            SELECT id_archivo, id_cliente, nombre, mime, tamano, creado_en
            FROM archivos WHERE id_archivo=?
        """;
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Object[]{
                        rs.getInt("id_archivo"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("mime"),
                        rs.getLong("tamano"),
                        rs.getTimestamp("creado_en").toString()
                };
            }
        }
    }

    public void insertar(int idCliente, String nombre, String mime, byte[] datos) throws SQLException {
        String sql = """
            INSERT INTO archivos (id_cliente, nombre, mime, bytes, tamano)
            VALUES (?,?,?,?, ?)
        """;
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, nombre);
            ps.setString(3, mime);
            ps.setBytes(4, datos);
            ps.setLong(5, datos == null ? 0L : datos.length); // sincroniza 'tamano'
            ps.executeUpdate();
        }
    }

    public void modificar(Integer id, int idCliente, String nombre, String mime, byte[] datos) throws SQLException {
        if (datos == null) {
            String sql = """
                UPDATE archivos
                   SET id_cliente=?, nombre=?, mime=?
                 WHERE id_archivo=?
            """;
            try (Connection c = Conexion.get();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setString(2, nombre);
                ps.setString(3, mime);
                ps.setInt(4, id);
                ps.executeUpdate();
            }
        } else {
            String sql = """
                UPDATE archivos
                   SET id_cliente=?, nombre=?, mime=?, bytes=?, tamano=?
                 WHERE id_archivo=?
            """;
            try (Connection c = Conexion.get();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setString(2, nombre);
                ps.setString(3, mime);
                ps.setBytes(4, datos);
                ps.setLong(5, datos.length);      // sincroniza 'tamano'
                ps.setInt(6, id);
                ps.executeUpdate();
            }
        }
    }

    public int borrar(int id) throws SQLException {
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM archivos WHERE id_archivo=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
