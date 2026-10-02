package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.ModelAndView;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;
import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.RestApi;

import java.lang.reflect.Parameter;
import sharon.sprintmvc.annotation.Param;

public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;
    Map<UrlKey, Mapping> urlMapping;
    String viewPrefix;
    String viewSuffix;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

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

        viewPrefix = (String) getServletContext().getAttribute("prefix");
        viewSuffix = (String) getServletContext().getAttribute("suffix");
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
                Object[] args = buildArgs(method, req);
                Object result = method.invoke(controller, args);

                // Cas 0 : @RestApi -> reponse JSON
                if (method.isAnnotationPresent(RestApi.class)
                        || mapping.getControllerClass().isAnnotationPresent(RestApi.class)) {
                    Object data = result;
                    if (result instanceof ModelAndView) {
                        data = ((ModelAndView) result).getValues();
                    }
                    sendJson(res, data);
                    return;
                }

                // Cas 1 : ModelAndView -> rediriger vers JSP
                if (result instanceof ModelAndView) {
                    ModelAndView mav = (ModelAndView) result;

                    for (Map.Entry<String, Object> en : mav.getValues().entrySet()) {
                        req.setAttribute(en.getKey(), en.getValue());
                    }

                    if (mav.getView() != null && !mav.getView().isEmpty()) {
                        String viewPath = viewPrefix + mav.getView() + viewSuffix;
                        RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                        dispatcher.forward(req, res);
                        return;
                    }

                    throw new ServletException("Aucune vue definie pour " + key);
                }

                // Cas 2 : String -> afficher directement
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
                        + key + " : " + (result == null ? "null" : result.getClass().getName()));

            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {

                Method m = mapping.getMethod();
                if (m.isAnnotationPresent(RestApi.class)
                        || mapping.getControllerClass().isAnnotationPresent(RestApi.class)) {
                    Throwable cause = (e instanceof InvocationTargetException && e.getCause() != null)
                            ? e.getCause() : e;
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("status", "error");
                    err.put("code", 500);
                    err.put("message", String.valueOf(cause.getMessage()));
                    sendJson(res, err);
                    return;
                }

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

    private void sendJson(HttpServletResponse res, Object data) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = res.getWriter()) {
            out.print(gson.toJson(data));
        }
    }

    private Object[] buildArgs(Method method, HttpServletRequest req) {
    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
        Parameter p = parameters[i];
        Param ann = p.getAnnotation(Param.class);
        String paramName = (ann != null && !ann.value().isEmpty()) ? ann.value() : p.getName();

        String raw = req.getParameter(paramName);
        if ((raw == null || raw.isEmpty()) && ann != null && !ann.defaultValue().isEmpty()) {
            raw = ann.defaultValue();
        }
        args[i] = convertParam(raw, p.getType());
    }
    return args;
}

    private Object convertParam(String raw, Class<?> type) {
        if (raw == null) {
            if (type == int.class || type == Integer.class) return 0;
            if (type == char.class || type == Character.class) return '-';
            if (type == boolean.class || type == Boolean.class) return false;
            if (type == double.class || type == Double.class) return 0.0;
            if (type == long.class || type == Long.class) return 0L;
            return null; // String ou objet -> null
        }
        if (type == String.class) return raw;
        if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
        if (type == char.class || type == Character.class) return raw.isEmpty() ? '-' : raw.charAt(0);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
        if (type == double.class || type == Double.class) return Double.parseDouble(raw);
        if (type == long.class || type == Long.class) return Long.parseLong(raw);
        return raw;
    }
}