package dao;

import model.ArchivoBinario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivosDAO {

    public List<ArchivoBinario> buscar(String campo, String valor, int limit, int offset) {
        List<ArchivoBinario> out = new ArrayList<>();
        String base = "SELECT id, nombre_original, mime, datos, creado_at FROM archivos";
        String where = "";
        List<Object> params = new ArrayList<>();

        if (campo != null && valor != null && !valor.isBlank()) {
            switch (campo) {
                case "ID" -> { where = " WHERE id = ?"; params.add(Integer.valueOf(valor)); }
                case "Nombre" -> { where = " WHERE nombre_original LIKE ?"; params.add("%" + valor + "%"); }
                case "MIME" -> { where = " WHERE mime LIKE ?"; params.add("%" + valor + "%"); }
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
                out.add(new ArchivoBinario(
                        rs.getInt("id"),
                        rs.getString("nombre_original"),
                        rs.getString("mime"),
                        rs.getBytes("datos"),  // puede venir null si SELECT lo trae truncado por config; en export cargamos por id
                        String.valueOf(rs.getTimestamp("creado_at"))
                ));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public ArchivoBinario findById(int id) {
        String sql = "SELECT id, nombre_original, mime, datos, creado_at FROM archivos WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ArchivoBinario(
                            rs.getInt("id"),
                            rs.getString("nombre_original"),
                            rs.getString("mime"),
                            rs.getBytes("datos"),
                            String.valueOf(rs.getTimestamp("creado_at"))
                    );
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return null;
    }

    public int insertar(ArchivoBinario a) {
        String sql = "INSERT INTO archivos(nombre_original, mime, datos) VALUES(?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getNombreOriginal());
            ps.setString(2, a.getMime());
            ps.setBytes(3, a.getDatos());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int actualizarMetadatos(ArchivoBinario a) {
        if (a.getId() == null) throw new IllegalArgumentException("ID requerido para actualizar");
        String sql = "UPDATE archivos SET nombre_original=?, mime=? WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getNombreOriginal());
            ps.setString(2, a.getMime());
            ps.setInt(3, a.getId());
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public int eliminar(Integer id) {
        String sql = "DELETE FROM archivos WHERE id=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
