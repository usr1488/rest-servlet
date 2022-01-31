package rest.servlet.util.hibernate;

import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import rest.servlet.core.util.PropertiesUtil;

public class HibernateSession {
    private final Class<?> clazz;

    public HibernateSession(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Session createSession() {
        return HibernateConfiguration.configuration(clazz)
                .buildSessionFactory(new StandardServiceRegistryBuilder()
                        .applySettings(PropertiesUtil.getAllProperties())
                        .build())
                .openSession();
    }
}
