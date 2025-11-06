package dao;
import bd.Conexion;
import java.sql.*;
import java.util.*;

public class DireccionesDAO {
    public List<Object[]> listarTodos() throws SQLException {
        String sql = """
            SELECT d.id_direccion, d.id_cliente, d.via, d.ciudad, d.cp
            FROM direcciones d ORDER BY d.id_direccion
        """;
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Object[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Object[]{
                    rs.getInt(1), rs.getInt(2),
                    rs.getString(3), rs.getString(4), rs.getString(5)
                });
            }
            return out;
        }
    }
    public int borrar(int idDireccion) throws SQLException {
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM direcciones WHERE id_direccion=?")) {
            ps.setInt(1, idDireccion);
            return ps.executeUpdate();
        }
    }
    // Stubs:
    public void insertar(int idCliente, String via, String ciudad, String cp) throws SQLException { /* TODO */ }
    public int modificar(int idDireccion, int idCliente, String via, String ciudad, String cp) throws SQLException { return 0; }
}
