package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;

public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;
    Map<UrlKey, Mapping> urlMapping;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        super.init();
        urlMapping = (Map<UrlKey, Mapping>) getServletContext()
                        .getAttribute("routesWithMethod");

        try {
            String initial = this.getInitParameter("Controller");
            listClasses = Utils.intoString(Utils.loadClasses(initial, Controller.class));
        } catch (Exception e) {
            e.printStackTrace();
            listClasses = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, "POST");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res, String httpMethod)
            throws IOException {

        res.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = res.getWriter()) {
            out.println("--- Sprint MVC ---");

            String path = req.getRequestURI().substring(req.getContextPath().length());

            // Nettoyer le slash final
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            UrlKey key = new UrlKey(path, httpMethod);

            if (urlMapping != null && urlMapping.containsKey(key)) {
                Mapping mapping = urlMapping.get(key);
                out.println("Route trouvee : " + key + " -> " + mapping);

                try {
                    Object controller = mapping.getControllerClass()
                                               .getDeclaredConstructor()
                                               .newInstance();
                    Method method = mapping.getMethod();
                    Object result = method.invoke(controller);

                    if (result != null) {
                        out.println("Resultat de la methode :");
                        out.println(result);
                    }

                } catch (InstantiationException | IllegalAccessException |
                         InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(
                        "Impossible d'executer la methode liee a " + key, e);
                }

            } else {
                out.println("Aucune route trouvee pour l'URL : " + path);
                if (urlMapping != null) {
                    urlMapping.forEach((k, v) -> {
                        out.println(k + " -> " + v.getControllerClass().getName()
                                    + "->" + v.getMethod().getName() + "()");
                    });
                }
            }
        }
    }
}