package persistence;

import model.Departamento;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DepartamentoDAO {

    public void insertar(Departamento d) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.save(d);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void actualizar(Departamento d) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.update(d);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void borrarPorId(int deptoNo) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            Departamento d = s.get(Departamento.class, deptoNo);
            if (d != null) s.delete(d);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public Departamento buscarPorId(int deptoNo) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.get(Departamento.class, deptoNo);
        }
    }

    public List<Departamento> listarTodos() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Departamento", Departamento.class).list();
        }
    }

    public List<Departamento> buscar(String texto) {
        String t = (texto == null ? "" : texto.trim().toLowerCase());
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "from Departamento d where lower(d.dnombre) like :t or lower(d.loc) like :t",
                            Departamento.class
                    )
                    .setParameter("t", "%" + t + "%")
                    .list();
        }
    }
}
