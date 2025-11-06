package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL  = "jdbc:mysql://localhost:3306/empresa_clientes";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    
    public static void main(String[] args) {
        try (Connection c = get()) {
            System.out.println("OK: " + (c != null && !c.isClosed()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
