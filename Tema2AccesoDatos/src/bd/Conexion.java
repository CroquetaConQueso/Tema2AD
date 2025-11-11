package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL  = "jdbc:mysql://localhost:3306/empresa_clientes?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection get() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
