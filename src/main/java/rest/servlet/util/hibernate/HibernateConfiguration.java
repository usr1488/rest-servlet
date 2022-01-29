package rest.servlet.util.hibernate;

public class HibernateConfiguration {
    private HibernateConfiguration() {
    }

    public static org.hibernate.cfg.Configuration configuration(Class<?> c) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration.addAnnotatedClass(c);
        return configuration;
    }
}
