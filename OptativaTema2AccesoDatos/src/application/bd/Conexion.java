package application.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL  = "jdbc:mysql://localhost:3306/taller?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver MySQL", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection c = Conexion.get()) {
            System.out.println("Conexión OK a " + c.getMetaData().getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
