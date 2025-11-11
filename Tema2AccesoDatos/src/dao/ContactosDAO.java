package dao;

import bd.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactosDAO {

    public List<Object[]> listarTodos() throws SQLException {
        String sql = "SELECT id_contacto, id_cliente, tipo, valor FROM contactos ORDER BY id_contacto";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Object[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Object[]{
                        rs.getInt("id_contacto"),
                        rs.getInt("id_cliente"),
                        rs.getString("tipo"),
                        rs.getString("valor")
                });
            }
            return out;
        }
    }

    public Object[] getById(int id) throws SQLException {
        String sql = "SELECT id_contacto, id_cliente, tipo, valor FROM contactos WHERE id_contacto=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Object[]{
                        rs.getInt("id_contacto"),
                        rs.getInt("id_cliente"),
                        rs.getString("tipo"),
                        rs.getString("valor")
                };
            }
        }
    }

    public int insertar(int idCliente, String tipo, String valor) throws SQLException {
        String sql = "INSERT INTO contactos(id_cliente, tipo, valor) VALUES(?,?,?)";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            ps.setString(2, tipo);
            ps.setString(3, valor);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        }
    }

    public int modificar(int id, int idCliente, String tipo, String valor) throws SQLException {
        String sql = "UPDATE contactos SET id_cliente=?, tipo=?, valor=? WHERE id_contacto=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, tipo);
            ps.setString(3, valor);
            ps.setInt(4, id);
            return ps.executeUpdate();
        }
    }

    public int borrar(int id) throws SQLException {
        String sql = "DELETE FROM contactos WHERE id_contacto = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
