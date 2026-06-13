package sharon.conquerant.sprintmvc.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * FrontController — point d'entree unique du framework sprint_mvc.
 * Capture toutes les URLs et affiche l'URL complete dans la page HTML.
 */
public class FrontControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Algo optimise — getRequestURL() natif Tomcat (thread-safe, zero copie String)
        StringBuffer fullUrl = req.getRequestURL();
        if (req.getQueryString() != null) {
            fullUrl.append("?").append(req.getQueryString());
        }

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Sprint MVC</title></head><body>");
        out.println("<h2>URL demandee :</h2>");
        out.println("<p style='font-size:1.5em; color:#2a6;'>" + fullUrl + "</p>");
        out.println("</body></html>");
    }
}
