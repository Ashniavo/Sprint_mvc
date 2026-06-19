package main.java.controllers;

import main.java.annotation.Controller;

@Controller("HelloController")
public class HelloController {

    public String index() {
        return "Hello depuis HelloController !";
    }
}
