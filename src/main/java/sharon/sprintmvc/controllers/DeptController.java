package sharon.sprintmvc.controllers;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.URLMapping;

@Controller("DeptController")
public class DeptController {

    @URLMapping(value = "/dept/new", method = "GET")
    public void create() {
        System.out.println("GET create()");
    }

    @URLMapping(value = "/dept/new", method = "POST")
    public void save() {
        System.out.println("POST save() - meme URL, methode differente !");
    }

    @URLMapping("/dept/list")
    public void list() {
        System.out.println("GET list()");
    }
}