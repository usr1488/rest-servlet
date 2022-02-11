package rest.servlet.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import rest.servlet.core.annotation.Controller;
import rest.servlet.core.annotation.ExceptionHandler;
import rest.servlet.core.annotation.Inject;
import rest.servlet.core.annotation.Mapping;

@Controller
public class HomeController {
    @Inject
    private Session session;

    public HomeController() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    @ExceptionHandler
    public String handleException(Throwable throwable) {
        throwable.printStackTrace();
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }

    @Mapping(isPage = true)
    public String test(HttpServletRequest request) {
//        Transaction transaction = session.beginTransaction();
//        session.save(Human.builder().name("nigger").build());
//        transaction.commit();
//        return session.get(Human.class, 1L);
        request.setAttribute("zxc", "test");
        return "test";
    }
}
