package rest.servlet.controller;

import rest.servlet.annotation.Controller;
import rest.servlet.annotation.Mapping;

@Controller
public class HomeController {
    @Mapping
    public String home() {
        return "home controller";
    }
}
