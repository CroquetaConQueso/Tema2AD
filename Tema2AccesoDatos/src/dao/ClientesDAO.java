package dao;

import bd.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientesDAO {

    public List<Object[]> listarTodos() throws SQLException {
        String sql = "SELECT id_cliente, nombre, email, edad FROM clientes ORDER BY id_cliente";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Object[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Object[]{
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getInt("edad")
                });
            }
            return out;
        }
    }

    public Object[] getById(int id) throws SQLException {
        String sql = "SELECT id_cliente, nombre, email, edad FROM clientes WHERE id_cliente=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Object[]{
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getInt("edad")
                };
            }
        }
    }

    public int insertar(String nombre, String email, int edad) throws SQLException {
        String sql = "INSERT INTO clientes(nombre, email, edad) VALUES(?,?,?)";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setInt(3, edad);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        }
    }

    public int modificar(int id, String nombre, String email, int edad) throws SQLException {
        String sql = "UPDATE clientes SET nombre=?, email=?, edad=? WHERE id_cliente=?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setInt(3, edad);
            ps.setInt(4, id);
            return ps.executeUpdate();
        }
    }

    public int borrar(int idCliente) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate();
        }
    }
}
