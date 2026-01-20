package primero;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class App {

    public static void main(String[] args) {

        System.out.println("Hola Alumnado de 1º DAM!");

        Alumno alumno = new Alumno(1, "Kumari", 25, "Femenino", "Camino Santiago", "952828209");

        Configuration config = new Configuration();
        config.configure("hibernate.cfg.xml");

        SessionFactory factory = config.buildSessionFactory();
        Session session = factory.openSession();

        Transaction tx = session.beginTransaction();
        session.save(alumno);
        tx.commit();

        session.close();
        factory.close();

        System.out.println("Registro guardado");
    }
}
