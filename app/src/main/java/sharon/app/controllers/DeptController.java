package sharon.app.controllers;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.NeedLogin;
import sharon.sprintmvc.annotation.Param;
import sharon.sprintmvc.annotation.RestApi;
import sharon.sprintmvc.annotation.URLMapping;
import sharon.sprintmvc.utils.Mapfiles;
import sharon.sprintmvc.utils.ModelAndView;
import sharon.sprintmvc.utils.MySession;
import sharon.app.models.Dept;
import sharon.app.models.Document;
import sharon.app.models.Ecole;
import sharon.app.models.Employe;
import sharon.app.models.User;
import sharon.app.models.Utilisateur;

@Controller("DeptController")
public class DeptController {

    private final AtomicInteger appels = new AtomicInteger();

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

    @URLMapping(value = "/api/dept/formUpload", method = "GET")
    public ModelAndView formUpload() {
        ModelAndView mav = new ModelAndView();
        mav.setView("dept/formUpload");
        return mav;
    }

    @URLMapping(value = "/api/dept/upload", method = "POST")
    public String upload(@Param("nom") String nom,
                         @Param("fichier") Mapfiles fichier) {
        if (fichier == null) {
            return "Aucun fichier recu";
        }
        return "nom=" + nom + ", fichier=" + fichier.getNom()
                + ", extension=" + fichier.getExtension()
                + ", taille=" + fichier.getSize() + " octets (enregistre dans upload-path)";
    }

    @URLMapping(value = "/api/dept/uploadMulti", method = "POST")
    public String uploadMulti(@Param("fichiers") List<Mapfiles> fichiers) {
        StringBuilder sb = new StringBuilder("Fichiers recus : " + fichiers.size());
        for (Mapfiles f : fichiers) {
            sb.append("\n- ").append(f.getNom()).append(" (").append(f.getSize()).append(" octets)");
        }
        return sb.toString();
    }

    @URLMapping(value = "/api/dept/saveDocument", method = "POST")
    public String saveDocument(@Param("doc") Document doc) {
        return "Document recu : " + doc;
    }

    @URLMapping(value = "/api/dept/formSession", method = "GET")
    public ModelAndView formSession() {
        ModelAndView mav = new ModelAndView();
        mav.setView("dept/formSession");
        return mav;
    }

    @URLMapping(value = "/api/dept/login", method = "POST")
    public String login(@Param("nom") String nom, MySession session) {
        session.setAttribute("user", nom);
        return "Bonjour " + nom + ", vous etes connecte";
    }

    @URLMapping(value = "/api/dept/profil", method = "GET")
    public String profil(MySession session) {
        Object user = session.getAttribute("user");
        return "Utilisateur en session : " + (user == null ? "aucun" : user);
    }

    @URLMapping(value = "/api/dept/compteur", method = "GET")
    public String compteur(MySession session) {
        Object valeur = session.getAttribute("visites");
        int visites = (valeur == null ? 0 : (Integer) valeur) + 1;
        session.setAttribute("visites", visites);
        return "Nombre de visites : " + visites;
    }

    @URLMapping(value = "/api/dept/logout", method = "GET")
    public String logout(MySession session) {
        session.destroy();
        return "Session videe";
    }

    @URLMapping(value = "/api/dept/formLogin", method = "GET")
    public ModelAndView formLogin() {
        ModelAndView mav = new ModelAndView();
        mav.setView("login");
        return mav;
    }

    @URLMapping(value = "/api/dept/connexion", method = "POST")
    public String connexion(@Param("nom") String nom,
                            @Param(value = "role", defaultValue = "user") String role,
                            MySession session) {
        session.setAttribute("isConnected", new Utilisateur(nom, role));
        return "Connecte : " + nom + " (role " + role + ")";
    }

    @URLMapping(value = "/api/dept/deconnexion", method = "GET")
    public String deconnexion(MySession session) {
        session.removeAttribute("isConnected");
        return "Deconnecte";
    }

    @NeedLogin
    @URLMapping(value = "/api/dept/protege", method = "GET")
    public String protege(MySession session) {
        Utilisateur u = (Utilisateur) session.getAttribute("isConnected");
        return "Zone protegee : bonjour " + u;
    }

    @NeedLogin("admin")
    @URLMapping(value = "/api/dept/admin", method = "GET")
    public String admin(MySession session) {
        Utilisateur u = (Utilisateur) session.getAttribute("isConnected");
        return "Zone admin : bonjour " + u;
    }

    @URLMapping(value = "/api/dept/instance", method = "GET")
    public String instance() {
        return "Instance " + System.identityHashCode(this) + " - appel numero " + appels.incrementAndGet();
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