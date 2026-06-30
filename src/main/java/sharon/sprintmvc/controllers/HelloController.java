package sharon.sprintmvc.controllers;

import sharon.sprintmvc.annotation.Controller;

@Controller("HelloController")
public class HelloController {

    public String index() {
        return "Hello depuis HelloController !";
    }
}
