package rest.servlet.controller;

import rest.servlet.annotation.Controller;
import rest.servlet.annotation.Mapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {
    @Mapping
    public List<String> home() {
        return Arrays.asList("1", "2", "3");
    }
}
