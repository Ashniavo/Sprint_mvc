package sharon.app.controllers;

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
        ModelAndView mav = new ModelAndView();
        mav.setView("dept/list");
        mav.addValue("message", "Liste des departements chargee !");
        mav.addValue("dept1", "Informatique");
        mav.addValue("dept2", "Mathematiques");
        return mav;
    }
}