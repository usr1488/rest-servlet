package rest.servlet.controller;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rest.servlet.core.annotation.Controller;
import rest.servlet.core.annotation.Inject;
import rest.servlet.core.annotation.Mapping;
import rest.servlet.entity.Human;

@Controller
public class HomeController {
    @Inject
    private Session session;

    public HomeController() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    @Mapping
    public Human home() {
        Transaction transaction = session.beginTransaction();
        session.save(Human.builder().name("nigger").build());
        transaction.commit();
        session.clear();
        return session.get(Human.class, 1L);
    }
}
