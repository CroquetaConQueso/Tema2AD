package application.dao;

import application.bd.Conexion;
import application.model.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public List<Vehiculo> findAll(){
        List<Vehiculo> out = new ArrayList<>();
        String sql = "SELECT id_vehiculo, matricula, marca, modelo, id_cliente FROM vehiculos ORDER BY id_vehiculo";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()){
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e){ throw new RuntimeException("Error listando vehículos", e); }
        return out;
    }

    public List<Vehiculo> searchBy(String tipo, String q){
        String col = switch (tipo) {
            case "Marca" -> "marca";
            case "Modelo" -> "modelo";
            case "IdCliente" -> "id_cliente";
            default -> "matricula";
        };
        String sql = "SELECT id_vehiculo, matricula, marca, modelo, id_cliente FROM vehiculos "+
                     (col.equals("id_cliente") ? "WHERE id_cliente = ? " : "WHERE "+col+" LIKE ? ") +
                     "ORDER BY "+(col.equals("id_cliente")? "id_cliente" : col);
        List<Vehiculo> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            if (col.equals("id_cliente")) ps.setInt(1, Integer.parseInt(q));
            else ps.setString(1, "%"+q+"%");
            try (ResultSet rs = ps.executeQuery()){ while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e){ throw new RuntimeException("Error buscando vehículos por "+col, e); }
        return out;
    }

    public List<Vehiculo> searchAdvanced(String matricula, String marca, String modelo, Integer idCliente){
        StringBuilder sql = new StringBuilder(
            "SELECT id_vehiculo, matricula, marca, modelo, id_cliente FROM vehiculos WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (matricula != null && !matricula.isBlank()){ sql.append(" AND matricula LIKE ?"); params.add("%"+matricula+"%"); }
        if (marca     != null && !marca.isBlank())    { sql.append(" AND marca LIKE ?");     params.add("%"+marca+"%"); }
        if (modelo    != null && !modelo.isBlank())   { sql.append(" AND modelo LIKE ?");    params.add("%"+modelo+"%"); }
        if (idCliente != null)                        { sql.append(" AND id_cliente = ?");   params.add(idCliente); }
        sql.append(" ORDER BY id_vehiculo");

        List<Vehiculo> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql.toString())){
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()){ while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e){ throw new RuntimeException("Error en búsqueda avanzada de vehículos", e); }
        return out;
    }

    public void insert(Vehiculo v){
        String sql = "INSERT INTO vehiculos(id_vehiculo, matricula, marca, modelo, id_cliente) VALUES(?,?,?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, v.getIdVehiculo());
            ps.setString(2, v.getMatricula());
            ps.setString(3, v.getMarca());
            ps.setString(4, v.getModelo());
            ps.setInt(5, v.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error insertando vehículo", e); }
    }

    public void update(Vehiculo v){
        String sql = "UPDATE vehiculos SET matricula=?, marca=?, modelo=?, id_cliente=? WHERE id_vehiculo=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, v.getMatricula());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getIdCliente());
            ps.setInt(5, v.getIdVehiculo());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error modificando vehículo", e); }
    }

    /** Actualización en lote de vehículos (batch). */
    public void updateBatch(List<Vehiculo> vehiculos){
        if (vehiculos==null || vehiculos.isEmpty()) return;
        String sql = "UPDATE vehiculos SET matricula=?, marca=?, modelo=?, id_cliente=? WHERE id_vehiculo=?";
        try (Connection c = Conexion.get()){
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)){
                for (Vehiculo v : vehiculos){
                    ps.setString(1, v.getMatricula());
                    ps.setString(2, v.getMarca());
                    ps.setString(3, v.getModelo());
                    ps.setInt(4, v.getIdCliente());
                    ps.setInt(5, v.getIdVehiculo());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        } catch (SQLException e){
            throw new RuntimeException("Error en actualización múltiple de vehículos", e);
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM vehiculos WHERE id_vehiculo=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando vehículo", e); }
    }

    public void deleteMany(List<Integer> ids){
        if (ids==null || ids.isEmpty()) return;
        String placeholders = String.join(",", ids.stream().map(i->"?").toList());
        String sql = "DELETE FROM vehiculos WHERE id_vehiculo IN ("+placeholders+")";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)){
            int i=1; for(Integer id: ids) ps.setInt(i++, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando múltiples vehículos", e); }
    }

    private Vehiculo map(ResultSet rs) throws SQLException {
        return new Vehiculo(
            rs.getInt("id_vehiculo"),
            rs.getString("matricula"),
            rs.getString("marca"),
            rs.getString("modelo"),
            rs.getInt("id_cliente")
        );
    }
}
