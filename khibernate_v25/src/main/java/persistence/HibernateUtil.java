package persistence;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration cfg = new Configuration();

            // 1) Cargar el hibernate.cfg.xml DESDE RAÍZ DEL PROYECTO (sin classpath)
            File cfgFile = new File("hibernate.cfg.xml");
            if (!cfgFile.exists()) {
                throw new RuntimeException("No se encuentra hibernate.cfg.xml en el directorio de ejecución: "
                        + new File(".").getAbsolutePath());
            }
            cfg.configure(cfgFile);

            // 2) Añadir el HBM (también por ruta, sin classpath)
            File hbmDept = new File("src/main/java/model/Departamento.hbm.xml");
            if (!hbmDept.exists()) {
                throw new RuntimeException("No se encuentra Departamento.hbm.xml en: " + hbmDept.getPath());
            }
            cfg.addFile(hbmDept);

            // 3) Añadir clase anotada (Empleado con @Entity)
            cfg.addAnnotatedClass(model.Empleado.class);

            return cfg.buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("Error creando SessionFactory: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}
