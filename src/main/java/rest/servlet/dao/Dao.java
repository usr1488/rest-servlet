package rest.servlet.dao;

import java.util.List;

public interface Dao<T> {
    void save(T entity);
    List<T> findAll();
    T get(long id);
    void update(T entity);
    void delete(T entity);
}
