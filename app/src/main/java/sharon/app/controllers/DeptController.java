package sharon.app.controllers;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.Param;
import sharon.sprintmvc.annotation.RestApi;
import sharon.sprintmvc.annotation.URLMapping;
import sharon.sprintmvc.utils.ModelAndView;
import sharon.app.models.Dept;
import sharon.app.models.Ecole;
import sharon.app.models.Employe;
import sharon.app.models.User;

@Controller("DeptController")
public class DeptController {

    @URLMapping(value = "/api/dept/new", method = "GET")
    public String create() {
        return "Formulaire de creation d un nouveau departement";
    }

    @URLMapping(value = "/api/dept/save", method = "POST")
    public String save(@Param(value = "nom", defaultValue = "-") char nom,
                       @Param(value = "prenom", defaultValue = "-") char prenom,
                       @Param(value = "age", defaultValue = "0") int age) {
        return "Departement sauvegarde : nom=" + nom + ", prenom=" + prenom + ", age=" + age;
    }

    @URLMapping(value = "/api/dept/saveObject", method = "POST")
    public String saveObject(@Param("dept") Dept dept) {
        return "Departement sauvegarde (objet) : " + dept;
    }

    @URLMapping(value = "/api/dept/saveEmployes", method = "POST")
    public String saveEmployes(@Param("employes") List<Employe> employes) {
        return "Employes recus (" + employes.size() + ") : " + employes;
    }

    @URLMapping(value = "/api/dept/tags", method = "POST")
    public String tags(@Param("tags") List<String> tags) {
        return "tags = " + tags;
    }

    @URLMapping(value = "/api/dept/saveUser", method = "POST")
    public String saveUser(@Param("user") User user,
                           @Param("ecole") Ecole ecole,
                           @Param("dateInsertion") Date dateInsertion) {
        return "user={" + user + "}, ecole={" + ecole + "}, dateInsertion=" + dateInsertion;
    }

    @URLMapping(value = "/api/dept/formUser", method = "GET")
    public ModelAndView formUser() {
        ModelAndView mav = new ModelAndView();
        mav.setView("dept/formUser");
        return mav;
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

    @RestApi
    @URLMapping(value = "/api/dept/json", method = "GET")
    public List<Map<String, Object>> listJson() {
        Map<String, Object> d1 = new LinkedHashMap<>();
        d1.put("id", 1);
        d1.put("nom", "Informatique");
        Map<String, Object> d2 = new LinkedHashMap<>();
        d2.put("id", 2);
        d2.put("nom", "Mathematiques");
        return List.of(d1, d2);
    }

    @URLMapping(value = "/api/dept/form", method = "GET")
    public ModelAndView form() {
        ModelAndView mav = new ModelAndView();
        mav.setView("dept/form");
        return mav;
    }
}
