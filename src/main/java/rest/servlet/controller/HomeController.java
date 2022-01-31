package rest.servlet.controller;

import rest.servlet.core.annotation.*;

@Controller
@Configuration
public class HomeController {
    @Property("hibernate.connection.url")
    private String url;

    @Inject
    private Runnable runnable;

    @Mapping
    public String home() {
        runnable.run();
        return url;
    }

    @Bean
    public Runnable foo() {
        return () -> System.out.println("bean");
    }
}
