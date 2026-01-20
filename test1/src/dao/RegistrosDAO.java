package dao;

import model.Registro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static util.UiUtils.parseEU;

public class RegistrosDAO {

    public List<Registro> listar(int limit, int offset) { return buscar(null, null, limit, offset); }

    public List<Registro> buscar(String campo, String valor, int limit, int offset) {
        String base = "SELECT id, nombre, fecha, cantidad FROM registros";
        String where = "";
        List<Object> params = new ArrayList<>();
        if (campo != null && valor != null && !valor.isBlank()) {
            switch (campo) {
                case "ID"       -> { where=" WHERE id = ?"; params.add(Integer.valueOf(valor)); }
                case "Nombre"   -> { where=" WHERE nombre LIKE ?"; params.add("%"+valor+"%"); }
                case "Fecha"    -> {
                    LocalDate ld = parseEU(valor); // dd/MM/yyyy → LocalDate
                    if (ld != null) { where=" WHERE fecha = ?"; params.add(Date.valueOf(ld)); }
                }
                case "Cantidad" -> { where=" WHERE cantidad = ?"; params.add(Integer.valueOf(valor)); }
            }
        }
        String sql = base + where + " ORDER BY id DESC LIMIT ? OFFSET ?";
        List<Registro> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) ps.setObject(i++, p);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new Registro(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDate("fecha").toLocalDate(),
                        (Integer) rs.getObject("cantidad")
                ));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public int insertar(Registro r) {
        String sql = "INSERT INTO registros(nombre, fecha, cantidad) VALUES(?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setDate(2, Date.valueOf(r.getFecha()));
            if (r.getCantidad()==null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, r.getCantidad());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int actualizar(Registro r) {
        if (r.getId()==null) throw new IllegalArgumentException("ID requerido");
        String sql = "UPDATE registros SET nombre=?, fecha=?, cantidad=? WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setDate(2, Date.valueOf(r.getFecha()));
            if (r.getCantidad()==null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, r.getCantidad());
            ps.setInt(4, r.getId());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /** Actualiza solo campos no nulos (edición en lote). */
    public int actualizarParcial(Registro r) {
        if (r.getId()==null) throw new IllegalArgumentException("ID requerido");
        StringBuilder sb = new StringBuilder("UPDATE registros SET ");
        List<Object> p = new ArrayList<>();
        if (r.getNombre()!=null)   { sb.append("nombre=?,"); p.add(r.getNombre()); }
        if (r.getFecha()!=null)    { sb.append("fecha=?,");  p.add(Date.valueOf(r.getFecha())); }
        if (r.getCantidad()!=null) { sb.append("cantidad=?,"); p.add(r.getCantidad()); }
        if (p.isEmpty()) return 0;
        sb.setLength(sb.length()-1);
        sb.append(" WHERE id=?");
        p.add(r.getId());
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i=1; for (Object o : p) ps.setObject(i++, o);
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int eliminar(Integer id) {
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM registros WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

