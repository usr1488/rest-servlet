package rest.servlet.util.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTransaction {
    private final Session session;
    private Transaction transaction;

    public HibernateTransaction(Session session) {
        this.session = session;
    }

    public void startTransaction() {
        transaction = session.beginTransaction();
    }

    public void commitTransaction() {
        transaction.commit();
    }
}
