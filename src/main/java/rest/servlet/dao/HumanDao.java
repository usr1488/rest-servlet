package rest.servlet.dao;

import lombok.Builder;
import lombok.Data;
import org.hibernate.Session;
import rest.servlet.entity.Human;
import rest.servlet.util.hibernate.HibernateTransaction;

import java.util.List;

@Data
@Builder
public class HumanDao implements Dao {
    private final HibernateTransaction transaction;
    private final Human human;
    private final Session session;

    @Override
    public void save(Object entity) {

    }

    @Override
    public List findAll() {
        return null;
    }

    @Override
    public Object get(long id) {
        return null;
    }

    @Override
    public void update(Object entity) {

    }

    @Override
    public void delete(long id) {

    }
}
