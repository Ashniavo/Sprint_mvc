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

    // Recuperer les routes preparees par AppServletContextListener
    urlMapping = (Map<UrlKey, Mapping>) getServletContext()
                    .getAttribute("routesWithMethod");

    // Recuperer la liste des controllers depuis le contexte
    List<Class<?>> controllerList = (List<Class<?>>) getServletContext()
                    .getAttribute("controllerList");

    if (controllerList != null) {
        listClasses = Utils.intoString(controllerList);
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

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Nettoyer le slash final
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Sprint MVC</title></head><body>");
        out.println("<h2>--- Sprint MVC ---</h2>");
        out.println("<p><b>URL :</b> " + path + "</p>");
        out.println("<p><b>Methode HTTP :</b> " + httpMethod + "</p>");
        out.println("<hr/>");

        UrlKey key = new UrlKey(path, httpMethod);

        if (urlMapping != null && urlMapping.containsKey(key)) {
            Mapping mapping = urlMapping.get(key);

            try {
                Object controller = mapping.getControllerClass()
                                           .getDeclaredConstructor()
                                           .newInstance();
                Method method = mapping.getMethod();
                Object result = method.invoke(controller);

                out.println("<p><b>Controller :</b> " + mapping.getControllerClass().getName() + "</p>");
                out.println("<p><b>Methode executee :</b> " + mapping.getMethod().getName() + "()</p>");

                if (result != null) {
                    out.println("<p><b>Resultat :</b> " + result + "</p>");
                }

            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(
                    "Impossible d'executer la methode liee a " + key, e);
            }

        } else {
            out.println("<p style='color:red;'><b>Erreur :</b> Aucune route trouvee pour : " + path + "</p>");
            out.println("<hr/>");
            out.println("<p><b>URLs connues :</b></p>");
            out.println("<ul>");
            if (urlMapping != null) {
                urlMapping.forEach((k, v) -> {
                    out.println("<li>" + k.getUrl() + " [" + k.getHttpMethod() + "]"
                                + " → " + v.getControllerClass().getSimpleName()
                                + "." + v.getMethod().getName() + "()</li>");
                });
            }
            out.println("</ul>");
        }

        out.println("</body></html>");
    }
    }
}