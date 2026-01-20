package persistence;

import model.Empleado;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EmpleadoDAO {

    public void insertar(Empleado e) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.save(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void actualizar(Empleado e) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.update(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void borrarPorId(int empNo) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            Empleado e = s.get(Empleado.class, empNo);
            if (e != null) s.delete(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public Empleado buscarPorId(int empNo) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.get(Empleado.class, empNo);
        }
    }

    public List<Empleado> listarTodos() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Empleado", Empleado.class).list();
        }
    }

    public List<Empleado> buscar(String texto) {
        String t = (texto == null ? "" : texto.trim().toLowerCase());
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "from Empleado e where lower(e.apellido) like :t or lower(e.oficio) like :t",
                            Empleado.class
                    )
                    .setParameter("t", "%" + t + "%")
                    .list();
        }
    }
}
