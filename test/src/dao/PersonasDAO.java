package dao;

import model.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonasDAO {

    public List<Persona> buscar(String campo, String valor, int limit, int offset) {
        List<Persona> out = new ArrayList<>();
        String base = "SELECT id, nombre, edad, ciudad_id FROM personas";
        String where = "";
        List<Object> params = new ArrayList<>();

        if (campo != null && valor != null && !valor.isBlank()) {
            switch (campo) {
                case "ID" -> { where = " WHERE id = ?"; params.add(Integer.valueOf(valor)); }
                case "Nombre" -> { where = " WHERE nombre LIKE ?"; params.add("%" + valor + "%"); }
                case "Edad" -> { where = " WHERE edad = ?"; params.add(Integer.valueOf(valor)); }
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
                out.add(new Persona(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("edad"),
                        (Integer) rs.getObject("ciudad_id")
                ));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public int insertar(Persona p) {
        String sql = "INSERT INTO personas(nombre, edad, ciudad_id) VALUES(?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getEdad());
            if (p.getCiudadId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, p.getCiudadId());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int actualizar(Persona p) {
        if (p.getId() == null) throw new IllegalArgumentException("ID requerido para actualizar");
        String sql = "UPDATE personas SET nombre=?, edad=?, ciudad_id=? WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getEdad());
            if (p.getCiudadId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, p.getCiudadId());
            ps.setInt(4, p.getId());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int eliminar(Integer id) {
        String sql = "DELETE FROM personas WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
