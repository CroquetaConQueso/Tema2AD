package dao;

import model.Ciudad;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CiudadesDAO {

    public List<Ciudad> buscar(String campo, String valor, int limit, int offset) {
        List<Ciudad> out = new ArrayList<>();
        String base = "SELECT id, nombre FROM ciudades";
        String where = "";
        List<Object> params = new ArrayList<>();

        if (campo != null && valor != null && !valor.isBlank()) {
            switch (campo) {
                case "ID" -> { where = " WHERE id = ?"; params.add(Integer.valueOf(valor)); }
                case "Nombre" -> { where = " WHERE nombre LIKE ?"; params.add("%" + valor + "%"); }
            }
        }
        String sql = base + where + " ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) ps.setObject(i++, p);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new Ciudad(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public int insertar(Ciudad c) {
        String sql = "INSERT INTO ciudades(nombre) VALUES(?)";
        try (Connection cx = Conexion.get(); PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            return ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("La ciudad ya existe (único).");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int actualizar(Ciudad c) {
        if (c.getId() == null) throw new IllegalArgumentException("ID requerido para actualizar");
        String sql = "UPDATE ciudades SET nombre=? WHERE id=?";
        try (Connection cx = Conexion.get(); PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            return ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("La ciudad ya existe (único).");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int eliminar(Integer id) {
        String sql = "DELETE FROM ciudades WHERE id=?";
        try (Connection cx = Conexion.get(); PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
