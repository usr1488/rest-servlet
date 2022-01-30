package rest.servlet.dao;

import lombok.Builder;
import org.hibernate.Session;
import rest.servlet.entity.Human;
import rest.servlet.util.hibernate.HibernateTransaction;

import java.util.List;

import static rest.servlet.util.hibernate.HQLQuery.SELECT_ALL_HUMAN;

@Builder
public class HumanDao implements Dao<Human> {
    private final HibernateTransaction transaction;
    private final Human human;
    private final Session session;

    @Override
    public void save(Human entity) {
        transaction.startTransaction();
        session.save(entity);
        transaction.commitTransaction();
        session.close();
    }

    @Override
    public List<Human> findAll() {
        return session.createQuery(SELECT_ALL_HUMAN, Human.class).getResultList();
    }

    @Override
    public Human get(long id) {
        return session.get(Human.class, id);
    }

    @Override
    public void update(Human entity) {
        session.update(entity);
    }

    @Override
    public void delete(Human human) {
        session.delete(human);
    }
}
