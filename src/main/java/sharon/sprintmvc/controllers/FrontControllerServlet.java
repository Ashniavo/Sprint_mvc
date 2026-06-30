package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.exception.UrlNotFoundException;
import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;

public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;
    Map<UrlKey, Mapping> urlMapping;

    public void init() throws ServletException {
        String initial = this.getInitParameter("Controller");
        try {
            List<Class<?>> controllers = Utils.loadClasses(initial, Controller.class);
            listClasses = Utils.intoString(controllers);
            urlMapping = Utils.buildUrlMapping(controllers);
        } catch (Exception e) {
            e.printStackTrace();
            listClasses = null;
            urlMapping = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res, "POST");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res, String httpMethod)
            throws ServletException, IOException {

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        String path = req.getPathInfo();
        if (path == null) {
            path = req.getServletPath();
        }

        // Nettoyer le slash final
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Sprint MVC</title></head><body>");
        out.println("<h2>URL demandee :</h2>");
        out.println("<p style='font-size:1.5em; color:#2a6;'>" + path + " [" + httpMethod + "]</p>");

        try {
            executeMapping(path, httpMethod, out);
        } catch (UrlNotFoundException e) {
            out.println("<h2 style='color:red;'>Erreur :</h2>");
            out.println("<p style='color:red;'>" + e.getMessage() + "</p>");
            out.println("<h3>URLs connues :</h3>");
            printKnownUrls(out);
        }

        out.println("</body></html>");
    }

    private void executeMapping(String url, String httpMethod, PrintWriter out) throws UrlNotFoundException {
        UrlKey key = new UrlKey(url, httpMethod);

        if (urlMapping == null || !urlMapping.containsKey(key)) {
            throw new UrlNotFoundException(url + " [" + httpMethod + "]");
        }

        Mapping mapping = urlMapping.get(key);
        try {
            Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();
            method.invoke(controllerInstance);

            out.println("<h2>Resultat :</h2>");
            out.println("<p>Methode executee : <b>" + mapping + "</b></p>");
        } catch (Exception e) {
            out.println("<p style='color:red;'>Erreur lors de l'execution de la methode : " + e.getMessage() + "</p>");
        }
    }

    private void printKnownUrls(PrintWriter out) {
        if (urlMapping == null || urlMapping.isEmpty()) {
            out.println("<p>Aucune URL connue.</p>");
            return;
        }
        out.println("<ul>");
        for (Map.Entry<UrlKey, Mapping> entry : urlMapping.entrySet()) {
            out.println("<li>" + entry.getKey() + " --&gt; " + entry.getValue() + "</li>");
        }
        out.println("</ul>");
    }
}