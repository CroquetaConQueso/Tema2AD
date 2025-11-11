package dao;

import bd.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DireccionesDAO {

    public List<Object[]> listarTodos() throws SQLException {
        String sql = "SELECT id_direccion, id_cliente, via, ciudad, cp FROM direcciones ORDER BY id_direccion";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Object[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Object[]{
                        rs.getInt("id_direccion"),
                        rs.getInt("id_cliente"),
                        rs.getString("via"),
                        rs.getString("ciudad"),
                        rs.getString("cp")
                });
            }
            return out;
        }
    }

    public Object[] getById(int id) throws SQLException {
        String sql = "SELECT id_direccion, id_cliente, via, ciudad, cp FROM direcciones WHERE id_direccion=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Object[]{
                        rs.getInt("id_direccion"),
                        rs.getInt("id_cliente"),
                        rs.getString("via"),
                        rs.getString("ciudad"),
                        rs.getString("cp")
                };
            }
        }
    }

    public int insertar(int idCliente, String via, String ciudad, String cp) throws SQLException {
        String sql = "INSERT INTO direcciones(id_cliente, via, ciudad, cp) VALUES(?,?,?,?)";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            ps.setString(2, via);
            ps.setString(3, ciudad);
            ps.setString(4, cp);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        }
    }

    public int modificar(int id, int idCliente, String via, String ciudad, String cp) throws SQLException {
        String sql = "UPDATE direcciones SET id_cliente=?, via=?, ciudad=?, cp=? WHERE id_direccion=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, via);
            ps.setString(3, ciudad);
            ps.setString(4, cp);
            ps.setInt(5, id);
            return ps.executeUpdate();
        }
    }

    public int borrar(int id) throws SQLException {
        String sql = "DELETE FROM direcciones WHERE id_direccion = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
