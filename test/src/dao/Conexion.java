package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

    // Ajusta USER/PASS si hace falta
    private static final String URL = "jdbc:mysql://localhost:3306/tema2app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Connector/J 9.5.0
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver MySQL: " + e.getMessage(), e);
        }
    }

    public static Connection get() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión: " + e.getMessage(), e);
        }
    }
}
