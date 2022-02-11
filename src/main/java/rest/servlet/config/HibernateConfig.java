package rest.servlet.config;

import kilpo.util.ClassScanner;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import rest.servlet.core.annotation.Bean;
import rest.servlet.core.annotation.Configuration;
import rest.servlet.core.annotation.Property;

import javax.persistence.Entity;
import java.util.Properties;

@Configuration
public class HibernateConfig {
    @Property("url")
    private String url;
    @Property("username")
    private String username;
    @Property("password")
    private String password;
    @Property("dialect")
    private String dialect;
    @Property("show_sql")
    private String showSql;
    @Property("hbm2ddl")
    private String hbm2ddl;

    @Bean
    public Session configuration() {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        Properties properties = new Properties();

        ClassScanner.scan().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .forEach(configuration::addAnnotatedClass);

        properties.put(AvailableSettings.URL, url);
        properties.put(AvailableSettings.USER, username);
        properties.put(AvailableSettings.PASS, password);
        properties.put(AvailableSettings.DIALECT, dialect);
        properties.put(AvailableSettings.SHOW_SQL, showSql);
        properties.put(AvailableSettings.HBM2DDL_AUTO, hbm2ddl);

        Session session = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(properties).build()).openSession();

        session.setHibernateFlushMode(FlushMode.COMMIT);

        return session;
    }
}
