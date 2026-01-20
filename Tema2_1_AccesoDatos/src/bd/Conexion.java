package bd;
import java.sql.*;

public class Conexion {
    private static final String URL  =
        "jdbc:mysql://localhost:3306/tema2_1_accesodatos?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root"; 

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
