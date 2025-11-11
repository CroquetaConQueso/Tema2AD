package application.dao;

import application.bd.Conexion;
import application.model.OrdenTrabajo;
import application.model.OrdenTotal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO {

    public List<OrdenTrabajo> findAll(){
        List<OrdenTrabajo> out = new ArrayList<>();
        String sql = "SELECT id_orden, fecha, descripcion, estado, id_vehiculo FROM ordenes_trabajo ORDER BY id_orden";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e){ throw new RuntimeException("Error listando órdenes", e); }
        return out;
    }

    public List<OrdenTotal> findTotales(){
        List<OrdenTotal> out = new ArrayList<>();
        String sql = """
            SELECT id_orden, fecha, descripcion, estado, id_vehiculo, matricula, nombre_cliente, total
            FROM v_ordenes_totales ORDER BY id_orden
            """;
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()) out.add(mapTotal(rs));
        } catch (SQLException e){ throw new RuntimeException("Error leyendo totales", e); }
        return out;
    }

    public List<OrdenTotal> findByEstado(String estado){
        if (estado==null || estado.isBlank() || estado.equals("(Todos)")) return findTotales();
        String sql = """
            SELECT id_orden, fecha, descripcion, estado, id_vehiculo, matricula, nombre_cliente, total
            FROM v_ordenes_totales WHERE estado = ? ORDER BY id_orden
            """;
        List<OrdenTotal> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()){ while (rs.next()) out.add(mapTotal(rs)); }
        } catch (SQLException e){ throw new RuntimeException("Error filtrando por estado", e); }
        return out;
    }

    public List<OrdenTotal> findByCriterios(String estado, LocalDate desde, LocalDate hasta,
                                            String matricula, String nombreCliente){
        StringBuilder sql = new StringBuilder("""
            SELECT id_orden, fecha, descripcion, estado, id_vehiculo, matricula, nombre_cliente, total
            FROM v_ordenes_totales WHERE 1=1""");
        List<Object> params = new ArrayList<>();

        if (estado!=null && !estado.isBlank() && !estado.equals("(Todos)")){ sql.append(" AND estado = ?"); params.add(estado); }
        if (desde != null){ sql.append(" AND fecha >= ?"); params.add(Date.valueOf(desde)); }
        if (hasta != null){ sql.append(" AND fecha <= ?"); params.add(Date.valueOf(hasta)); }
        if (matricula != null && !matricula.isBlank()){ sql.append(" AND matricula LIKE ?"); params.add("%"+matricula+"%"); }
        if (nombreCliente != null && !nombreCliente.isBlank()){ sql.append(" AND nombre_cliente LIKE ?"); params.add("%"+nombreCliente+"%"); }
        sql.append(" ORDER BY id_orden");

        List<OrdenTotal> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql.toString())){
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()){ while (rs.next()) out.add(mapTotal(rs)); }
        } catch (SQLException e){ throw new RuntimeException("Error en búsqueda avanzada de órdenes", e); }
        return out;
    }

    public void insert(OrdenTrabajo o){
        String sql = "INSERT INTO ordenes_trabajo(id_orden, fecha, descripcion, estado, id_vehiculo) VALUES(?,?,?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, o.getIdOrden());
            ps.setDate(2, Date.valueOf(o.getFecha()));
            ps.setString(3, o.getDescripcion());
            ps.setString(4, o.getEstado());
            ps.setInt(5, o.getIdVehiculo());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error insertando orden", e); }
    }

    public void update(OrdenTrabajo o){
        String sql = "UPDATE ordenes_trabajo SET fecha=?, descripcion=?, estado=?, id_vehiculo=? WHERE id_orden=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setDate(1, Date.valueOf(o.getFecha()));
            ps.setString(2, o.getDescripcion());
            ps.setString(3, o.getEstado());
            ps.setInt(4, o.getIdVehiculo());
            ps.setInt(5, o.getIdOrden());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error modificando orden", e); }
    }

    /** Actualización en lote de órdenes (batch). */
    public void updateBatch(List<OrdenTrabajo> ordenes){
        if (ordenes==null || ordenes.isEmpty()) return;
        String sql = "UPDATE ordenes_trabajo SET fecha=?, descripcion=?, estado=?, id_vehiculo=? WHERE id_orden=?";
        try (Connection c = Conexion.get()){
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)){
                for (OrdenTrabajo o : ordenes){
                    ps.setDate(1, Date.valueOf(o.getFecha()));
                    ps.setString(2, o.getDescripcion());
                    ps.setString(3, o.getEstado());
                    ps.setInt(4, o.getIdVehiculo());
                    ps.setInt(5, o.getIdOrden());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        } catch (SQLException e){
            throw new RuntimeException("Error en actualización múltiple de órdenes", e);
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM ordenes_trabajo WHERE id_orden=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando orden", e); }
    }

    public void deleteMany(List<Integer> ids){
        if (ids==null || ids.isEmpty()) return;
        String placeholders = String.join(",", ids.stream().map(i->"?").toList());
        String sql = "DELETE FROM ordenes_trabajo WHERE id_orden IN ("+placeholders+")";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            int i=1; for(Integer id: ids) ps.setInt(i++, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando múltiples órdenes", e); }
    }

    private OrdenTrabajo map(ResultSet rs) throws SQLException {
        return new OrdenTrabajo(
            rs.getInt("id_orden"),
            rs.getDate("fecha").toLocalDate(),
            rs.getString("descripcion"),
            rs.getString("estado"),
            rs.getInt("id_vehiculo")
        );
    }

    private OrdenTotal mapTotal(ResultSet rs) throws SQLException {
        return new OrdenTotal(
            rs.getInt("id_orden"),
            rs.getDate("fecha").toLocalDate(),
            rs.getString("descripcion"),
            rs.getString("estado"),
            rs.getInt("id_vehiculo"),
            rs.getString("matricula"),
            rs.getString("nombre_cliente"),
            rs.getDouble("total")
        );
    }
}
