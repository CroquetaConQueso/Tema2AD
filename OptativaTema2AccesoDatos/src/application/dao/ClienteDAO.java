package application.dao;

import application.bd.Conexion;
import application.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> findAll() {
        List<Cliente> out = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, telefono, email FROM clientes ORDER BY id_cliente";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException("Error listando clientes", e); }
        return out;
    }

    public List<Cliente> searchBy(String tipo, String q) {
        String col = switch (tipo) {
            case "Teléfono" -> "telefono";
            case "Email" -> "email";
            default -> "nombre";
        };
        String sql = "SELECT id_cliente, nombre, telefono, email FROM clientes WHERE " + col + " LIKE ? ORDER BY " + col;
        List<Cliente> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("Error buscando clientes por "+col, e); }
        return out;
    }

    public List<Cliente> searchAdvanced(String nombre, String telefono, String email) {
        StringBuilder sql = new StringBuilder(
            "SELECT id_cliente, nombre, telefono, email FROM clientes WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (nombre   != null && !nombre.isBlank())   { sql.append(" AND nombre LIKE ?");   params.add("%"+nombre+"%"); }
        if (telefono != null && !telefono.isBlank()) { sql.append(" AND telefono LIKE ?"); params.add("%"+telefono+"%"); }
        if (email    != null && !email.isBlank())    { sql.append(" AND email LIKE ?");    params.add("%"+email+"%"); }
        sql.append(" ORDER BY id_cliente");

        List<Cliente> out = new ArrayList<>();
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(map(rs)); }
        } catch (SQLException e) { throw new RuntimeException("Error en búsqueda avanzada de clientes", e); }
        return out;
    }

    public void insert(Cliente cl){
        String sql = "INSERT INTO clientes(id_cliente, nombre, telefono, email) VALUES(?,?,?,?)";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cl.getIdCliente());
            ps.setString(2, cl.getNombre());
            ps.setString(3, cl.getTelefono());
            ps.setString(4, cl.getEmail());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error insertando cliente", e); }
    }

    public void update(Cliente cl){
        String sql = "UPDATE clientes SET nombre=?, telefono=?, email=? WHERE id_cliente=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cl.getNombre());
            ps.setString(2, cl.getTelefono());
            ps.setString(3, cl.getEmail());
            ps.setInt(4, cl.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error modificando cliente", e); }
    }

    /** Batch clásico: falla todo si hay una colisión (lo mantenemos por si lo quieres usar). */
    public void updateBatch(List<Cliente> clientes){
        if (clientes==null || clientes.isEmpty()) return;
        String sql = "UPDATE clientes SET nombre=?, telefono=?, email=? WHERE id_cliente=?";
        try (Connection c = Conexion.get()){
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)){
                for (Cliente cl : clientes){
                    ps.setString(1, cl.getNombre());
                    ps.setString(2, cl.getTelefono());
                    ps.setString(3, cl.getEmail());
                    ps.setInt(4, cl.getIdCliente());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        } catch (SQLException e){
            throw new RuntimeException("Error en actualización múltiple de clientes", e);
        }
    }

    /** Resultado resumido del modo parcial. */
    public static class BatchResult {
        public final List<Integer> okIds = new ArrayList<>();
        public final List<Integer> failIds = new ArrayList<>();
        public final List<String>  failMsgs = new ArrayList<>();
    }

    /**
     * Modo tolerante: intenta actualizar uno a uno dentro de una transacción con savepoints.
     * Si una fila viola la unicidad (p.ej., email duplicado), se revierte solo esa fila.
     */
    public BatchResult updateBatchPartial(List<Cliente> clientes){
        BatchResult result = new BatchResult();
        if (clientes==null || clientes.isEmpty()) return result;

        String sql = "UPDATE clientes SET nombre=?, telefono=?, email=? WHERE id_cliente=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            c.setAutoCommit(false);
            for (Cliente cl : clientes){
                Savepoint sp = c.setSavepoint();
                try {
                    ps.setString(1, cl.getNombre());
                    ps.setString(2, cl.getTelefono());
                    ps.setString(3, cl.getEmail());
                    ps.setInt(4, cl.getIdCliente());
                    ps.executeUpdate();
                    result.okIds.add(cl.getIdCliente());
                } catch (SQLException ex){
                    c.rollback(sp);
                    result.failIds.add(cl.getIdCliente());
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("Duplicate entry") && msg.contains("uq_clientes_email")) {
                        msg = "Email duplicado";
                    }
                    result.failMsgs.add("ID "+cl.getIdCliente()+": "+msg);
                }
            }
            c.commit();
        } catch (SQLException e){
            throw new RuntimeException("Error en actualización parcial de clientes", e);
        }
        return result;
    }

    public void delete(int id){
        String sql = "DELETE FROM clientes WHERE id_cliente=?";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando cliente", e); }
    }

    public void deleteMany(List<Integer> ids){
        if (ids==null || ids.isEmpty()) return;
        String placeholders = String.join(",", ids.stream().map(i->"?").toList());
        String sql = "DELETE FROM clientes WHERE id_cliente IN ("+placeholders+")";
        try (Connection c = Conexion.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i=1; for(Integer id: ids) ps.setInt(i++, id);
            ps.executeUpdate();
        } catch (SQLException e){ throw new RuntimeException("Error borrando múltiples clientes", e); }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_cliente"),
            rs.getString("nombre"),
            rs.getString("telefono"),
            rs.getString("email")
        );
    }
}
