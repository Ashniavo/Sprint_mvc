package sharon.app.controllers;

import java.util.Arrays;
import java.util.List;

import sharon.app.entity.Departement;
import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.URLMapping;
import sharon.sprintmvc.utils.ModelAndView;

@Controller("DeptController")
public class DeptController {

    @URLMapping(value = "/api/dept/new", method = "GET")
    public String create() {
        return "Formulaire de creation d un nouveau departement";
    }

    @URLMapping(value = "/api/dept/new", method = "POST")
    public String save() {
        return "Departement sauvegarde avec succes !";
    }

    @URLMapping(value = "/api/dept/list", method = "GET")
    public ModelAndView list() {
        // Simulation BDD — sera remplace par un vrai DAO plus tard
        List<Departement> depts = Arrays.asList(
            new Departement(1, "Informatique"),
            new Departement(2, "Mathematiques"),
            new Departement(3, "Physique")
        );

        ModelAndView mav = new ModelAndView();
        mav.setView("dept/list");
        mav.addValue("depts", depts);
        return mav;
    }
}