package dao;

import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientesDAO {

    /* ===================== QUERIES BÁSICAS ===================== */

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

    public int borrar(int idCliente) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate();
        }
    }

    /* ===================== INSERTAR / MODIFICAR ===================== */

    /** Inserta un cliente y devuelve el ID generado (o -1 si no hay auto-increment). */
    public int insertar(String nombre, String email, int edad) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, email, edad) VALUES (?,?,?)";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setInt(3, edad);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1; // si el driver no devuelve claves
        }
    }

    /** Modifica un cliente por ID. Devuelve nº de filas afectadas (0 si no existe). */
    public int modificar(int idCliente, String nombre, String email, int edad) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, email = ?, edad = ? WHERE id_cliente = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setInt(3, edad);
            ps.setInt(4, idCliente);

            return ps.executeUpdate();
        }
    }

    /* ===================== SELECT POR ID ===================== */

    /** Devuelve {id, nombre, email, edad} o null si no existe. Útil para editar. */
    public Object[] getById(int idCliente) throws SQLException {
        String sql = "SELECT id_cliente, nombre, email, edad FROM clientes WHERE id_cliente = ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id_cliente"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getInt("edad")
                    };
                }
                return null;
            }
        }
    }

    /* ===================== (Opcional) COMPROBAR EMAIL ===================== */

    /**
     * Comprueba si existe ya un email. Si excluirId != null, ignora ese cliente (para editar).
     * Útil si decides validar unicidad de email en UI antes del INSERT/UPDATE.
     */
    public boolean emailExiste(String email, Integer excluirId) throws SQLException {
        String base = "SELECT COUNT(*) FROM clientes WHERE email = ?";
        String sql = (excluirId == null) ? base : base + " AND id_cliente <> ?";
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            if (excluirId != null) ps.setInt(2, excluirId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
