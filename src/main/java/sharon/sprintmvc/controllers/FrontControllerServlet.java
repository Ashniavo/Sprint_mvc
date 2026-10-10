package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

                // Cas 3 : void / null -> rien a afficher
                if (result == null) {
                    res.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = res.getWriter()) {
                        out.println("OK (aucune valeur retournee)");
                    }
                    return;
                }

                throw new ServletException("Type de retour non supporte pour "
                        + key + " : " + result.getClass().getName());

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

    // ==================== BINDING (Sprint 7 / 7bis) ====================

    /** Construit les arguments de la methode a partir des parametres de la requete. */
    private Object[] buildArgs(Method method, HttpServletRequest req) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Param ann = p.getAnnotation(Param.class);
            String name = (ann != null && !ann.value().isEmpty()) ? ann.value() : p.getName();

            if (isSimpleType(p.getType())) {
                // Type simple : char, int, String, Date...
                args[i] = convertParam(readValue(req, name, ann), p.getType());
            } else if (List.class.isAssignableFrom(p.getType())) {
                // Argument de type List<X> (X simple ou objet)
                args[i] = bindList(p.getParameterizedType(), name, req);
            } else {
                // Objet complet 
                args[i] = bindObject(p.getType(), name, req);
            }
        }
        return args;
    }

    /** Lit un parametre HTTP, ou prend la valeur par defaut de @Param si absent/vide. */
    private String readValue(HttpServletRequest req, String name, Param ann) {
        String raw = req.getParameter(name);
        if ((raw == null || raw.isEmpty()) && ann != null && !ann.defaultValue().isEmpty()) {
            raw = ann.defaultValue();
        }
        return raw;
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class || type.isPrimitive()
                || type == Integer.class || type == Character.class
                || type == Boolean.class || type == Double.class || type == Long.class
                || type == Date.class;
    }

    /**
     * Cree un objet et remplit ses champs. Recursif : un champ objet ou liste
     * est rempli avec un prefixe (ex: "user.nom", "employes[0].nom").
     */
    private Object bindObject(Class<?> type, String prefix, HttpServletRequest req) {
        try {
            Object obj = type.getDeclaredConstructor().newInstance();

            for (Field f : type.getDeclaredFields()) {
                f.setAccessible(true);
                Param ann = f.getAnnotation(Param.class);
                String name = (ann != null && !ann.value().isEmpty()) ? ann.value() : f.getName();
                String fullName = prefix.isEmpty() ? name : prefix + "." + name;

                Class<?> fieldType = f.getType();
                if (isSimpleType(fieldType)) {
                    f.set(obj, convertParam(readValue(req, fullName, ann), fieldType));
                } else if (List.class.isAssignableFrom(fieldType)) {
                    f.set(obj, bindList(f.getGenericType(), fullName, req));
                } else {
                    f.set(obj, bindObject(fieldType, fullName, req));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de binding de l objet " + type.getName(), e);
        }
    }

    /*rempli une liste   */
    private List<Object> bindList(Type generic, String fullName, HttpServletRequest req) {
        Class<?> elementType = String.class;
        if (generic instanceof ParameterizedType) {
            Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (arg instanceof Class) {
                elementType = (Class<?>) arg;
            }
        }

        List<Object> list = new ArrayList<>();
        Set<Integer> indexes = findIndexes(fullName, req);

        if (isSimpleType(elementType)) {
            if (!indexes.isEmpty()) {
                for (int idx : indexes) {
                    list.add(convertParam(req.getParameter(fullName + "[" + idx + "]"), elementType));
                }
            } else {
                String[] values = req.getParameterValues(fullName);
                if (values != null) {
                    for (String v : values) {
                        list.add(convertParam(v, elementType));
                    }
                }
            }
        } else {
            for (int idx : indexes) {
                list.add(bindObject(elementType, fullName + "[" + idx + "]", req));
            }
        }
        return list;
    }

    /** Trouve les index d'une liste : "employes[0].nom", "employes[1].nom" -> {0, 1}. */
    private Set<Integer> findIndexes(String fullName, HttpServletRequest req) {
        Set<Integer> indexes = new TreeSet<>();
        String start = fullName + "[";
        for (String key : req.getParameterMap().keySet()) {
            if (key.startsWith(start)) {
                int end = key.indexOf(']', start.length());
                if (end > 0) {
                    try {
                        indexes.add(Integer.parseInt(key.substring(start.length(), end)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return indexes;
    }

    /** Convertit le texte recu (HTTP) vers le type Java attendu. */
    private Object convertParam(String raw, Class<?> type) {
        // Champ vide (ex: input number laisse vide) = absent, sauf pour String
        if (raw != null && raw.isEmpty() && type != String.class) {
            raw = null;
        }
        if (raw == null) {
            if (type == int.class || type == Integer.class) return 0;
            if (type == char.class || type == Character.class) return '-';
            if (type == boolean.class || type == Boolean.class) return false;
            if (type == double.class || type == Double.class) return 0.0;
            if (type == long.class || type == Long.class) return 0L;
            return null; // String, Date ou objet -> null
        }
        if (type == String.class) return raw;
        if (type == Date.class) return parseDate(raw);
        if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
        if (type == char.class || type == Character.class) return raw.charAt(0);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
        if (type == double.class || type == Double.class) return Double.parseDouble(raw);
        if (type == long.class || type == Long.class) return Long.parseLong(raw);
        return raw;
    }

    /** Accepte : 2026-10-10 (input date), 2026-10-10T14:30 (datetime-local), 10/10/2026. */
    private Date parseDate(String raw) {
        String[] formats = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd", "dd/MM/yyyy"};
        for (String f : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f);
                sdf.setLenient(false);
                return sdf.parse(raw);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Format de date invalide : " + raw);
    }
}