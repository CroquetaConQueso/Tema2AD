package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    // Ajusta estos valores a TU base de datos y driver
    // Ejemplo MySQL:
    private static final String URL  = "jdbc:mysql://localhost:3306/test1?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver"; // cámbialo si usas otro JDBC

    static {
        try { Class.forName(DRIVER); }
        catch (ClassNotFoundException e) { throw new RuntimeException("Driver JDBC no encontrado: " + DRIVER, e); }
    }

    public static Connection get() {
        try { return DriverManager.getConnection(URL, USER, PASS); }
        catch (Exception e) { throw new RuntimeException("Error de conexión: " + e.getMessage(), e); }
    }
}
