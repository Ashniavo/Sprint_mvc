package sharon.sprintmvc.controllers;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.URLMapping;

@Controller("DeptController")
public class DeptController {

    @URLMapping("/dept/new")
    public void create() {
        System.out.println("Methode create() : creation d'un nouveau departement.");
    }

    @URLMapping("/dept/list")
    public void list() {
        System.out.println("Methode list() : liste des departements.");
    }
}