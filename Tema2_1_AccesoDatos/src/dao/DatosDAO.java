package dao;

import bd.Conexion;
import model.Dato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatosDAO {

    /* ==== Lecturas ==== */

    public List<Dato> listarTodos() throws SQLException {
        String sql = "SELECT identificador, nombre, edad FROM datos ORDER BY identificador";
        try (Connection cn = Conexion.get();
             PreparedStatement pst = cn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            List<Dato> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Dato(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return lista;
        }
    }

    /** Buscar por campo: "TODOS" (ID/Nombre/Edad), "ID", "NOMBRE", "EDAD". */
    public List<Dato> buscar(String campo, String q) throws SQLException {
        List<Dato> lista = new ArrayList<>();
        String sql;
        boolean like = true;

        switch (campo.toUpperCase()) {
            case "ID":
                // si es número, igual exacto; si no, LIKE sobre CAST
                if (esEntero(q)) {
                    sql = "SELECT identificador, nombre, edad FROM datos WHERE identificador = ? ORDER BY identificador";
                    try (Connection cn = Conexion.get(); PreparedStatement pst = cn.prepareStatement(sql)) {
                        pst.setInt(1, Integer.parseInt(q));
                        try (ResultSet rs = pst.executeQuery()) {
                            while (rs.next()) lista.add(map(rs));
                        }
                    }
                    return lista;
                } else {
                    sql = "SELECT identificador, nombre, edad FROM datos " +
                          "WHERE CAST(identificador AS CHAR) LIKE ? ORDER BY identificador";
                }
                break;
            case "NOMBRE":
                sql = "SELECT identificador, nombre, edad FROM datos WHERE nombre LIKE ? ORDER BY identificador";
                break;
            case "EDAD":
                if (esEntero(q)) {
                    sql = "SELECT identificador, nombre, edad FROM datos WHERE edad = ? ORDER BY identificador";
                    like = false;
                } else {
                    sql = "SELECT identificador, nombre, edad FROM datos " +
                          "WHERE CAST(edad AS CHAR) LIKE ? ORDER BY identificador";
                }
                break;
            case "TODOS":
            default:
                sql = "SELECT identificador, nombre, edad FROM datos " +
                      "WHERE CAST(identificador AS CHAR) LIKE ? " +
                      "   OR nombre LIKE ? " +
                      "   OR CAST(edad AS CHAR) LIKE ? " +
                      "ORDER BY identificador";
                try (Connection cn = Conexion.get(); PreparedStatement pst = cn.prepareStatement(sql)) {
                    String likeQ = "%" + q + "%";
                    pst.setString(1, likeQ);
                    pst.setString(2, likeQ);
                    pst.setString(3, likeQ);
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) lista.add(map(rs));
                    }
                }
                return lista;
        }

        try (Connection cn = Conexion.get(); PreparedStatement pst = cn.prepareStatement(sql)) {
            if (like) pst.setString(1, "%" + q + "%");
            else pst.setInt(1, Integer.parseInt(q));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    private static Dato map(ResultSet rs) throws SQLException {
        return new Dato(rs.getInt(1), rs.getString(2), rs.getInt(3));
    }

    private static boolean esEntero(String s) {
        try { Integer.parseInt(s.trim()); return true; } catch (Exception e) { return false; }
    }

    /* ==== Validaciones de duplicados ==== */

    /**
     * ¿Existe otro registro con mismo (LOWER(nombre), edad)?
     * @param nombre nombre a comprobar
     * @param edad edad a comprobar
     * @param excluirId si no es null, excluye ese id (para UPDATE)
     */
    public boolean existeNombreEdad(String nombre, int edad, Integer excluirId) throws SQLException {
        String base = "SELECT 1 FROM datos WHERE LOWER(nombre)=LOWER(?) AND edad=? ";
        String sql = (excluirId == null) ? base + "LIMIT 1" : base + "AND identificador <> ? LIMIT 1";

        try (Connection cn = Conexion.get(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setString(1, nombre);
            pst.setInt(2, edad);
            if (excluirId != null) pst.setInt(3, excluirId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* ==== Escrituras ==== */

    public int insertar(String nombre, int edad) throws SQLException {
        String sql = "INSERT INTO datos (nombre, edad) VALUES (?, ?)";
        try (Connection cn = Conexion.get();
             PreparedStatement pst = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, nombre);
            pst.setInt(2, edad);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean actualizar(int id, String nombre, int edad) throws SQLException {
        String sql = "UPDATE datos SET nombre = ?, edad = ? WHERE identificador = ?";
        try (Connection cn = Conexion.get(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setString(1, nombre);
            pst.setInt(2, edad);
            pst.setInt(3, id);
            return pst.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.get();
             PreparedStatement pst = cn.prepareStatement("DELETE FROM datos WHERE identificador = ?")) {
            pst.setInt(1, id);
            return pst.executeUpdate() == 1;
        }
    }
}
