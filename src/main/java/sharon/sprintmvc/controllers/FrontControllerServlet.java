package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.ModelAndView;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;
import sharon.sprintmvc.annotation.Controller;

public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;
    Map<UrlKey, Mapping> urlMapping;
    String viewPrefix;
    String viewSuffix;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        super.init();
        urlMapping = (Map<UrlKey, Mapping>) getServletContext()
                        .getAttribute("routesWithMethod");

        List<Class<?>> controllerList = (List<Class<?>>) getServletContext()
                        .getAttribute("controllerList");

        if (controllerList != null) {
            listClasses = Utils.intoString(controllerList);
        }

        viewPrefix = getServletContext().getInitParameter("view.prefix");
        viewSuffix = getServletContext().getInitParameter("view.suffix");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        // Exactement comme sprint5
        String pathInfo = req.getRequestURI().substring(req.getContextPath().length());
        UrlKey key = new UrlKey(pathInfo, req.getMethod());

        if (urlMapping != null && urlMapping.containsKey(key)) {
            Mapping mapping = urlMapping.get(key);
            System.out.println("Route trouvee : " + key + " -> " + mapping);

            try {
                Object controller = mapping.getControllerClass()
                                           .getDeclaredConstructor()
                                           .newInstance();
                Method method = mapping.getMethod();
                Object result = method.invoke(controller);

                if (result == null) {
                    throw new ServletException("La methode liee a " + key + " a retourne null");
                }

                // Cas 1 : ModelAndView → rediriger vers JSP
                if (result instanceof ModelAndView) {
                    ModelAndView mav = (ModelAndView) result;

                    if (mav.getValues() != null) {
                        req.setAttribute("map", mav.getValues());
                    }

                    if (mav.getView() != null && !mav.getView().isEmpty()) {
                        String viewPath = viewPrefix + mav.getView() + viewSuffix;
                        RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                        dispatcher.forward(req, res);
                        return;
                    }

                    throw new ServletException("Aucune vue definie pour " + key);
                }

                // Cas 2 : String → afficher directement
                if (result instanceof String) {
                    String text = (String) result;
                    res.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = res.getWriter()) {
                        out.println("Resultat de la methode:\n");
                        out.println(text);
                    }
                    return;
                }

                throw new ServletException("Type de retour non supporte pour "
                        + key + " : " + result.getClass().getName());

            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(
                    "Impossible d'executer la methode liee a " + key, e);
            }

        } else {
            res.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = res.getWriter()) {
                out.println("Aucune route trouvee pour l'URL : " + pathInfo);
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