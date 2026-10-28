package sharon.sprintmvc.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sharon.sprintmvc.utils.Mapfiles;
import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.ModelAndView;
import sharon.sprintmvc.utils.MySession;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;
import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.annotation.RestApi;
import sharon.sprintmvc.annotation.Param;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 50 * 1024 * 1024
)
public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;
    Map<UrlKey, Mapping> urlMapping;
    Map<String, MySession> sessions;
    String viewPrefix;
    String viewSuffix;
    String uploadPath;
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

        sessions = (Map<String, MySession>) getServletContext().getAttribute("sessions");
        if (sessions == null) {
            sessions = new ConcurrentHashMap<>();
        }

        uploadPath = getServletContext().getInitParameter("upload-path");
        if (uploadPath != null && !uploadPath.isBlank()) {
            try {
                Files.createDirectories(Paths.get(uploadPath));
            } catch (IOException e) {
                throw new ServletException(
                        "Impossible de creer le repertoire d'upload " + uploadPath, e);
            }
        }
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

                synchronizeSessions(args, req);

                if (method.isAnnotationPresent(RestApi.class)
                        || mapping.getControllerClass().isAnnotationPresent(RestApi.class)) {
                    Object data = result;
                    if (result instanceof ModelAndView) {
                        data = ((ModelAndView) result).getValues();
                    }
                    sendJson(res, data);
                    return;
                }

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

                if (result instanceof String) {
                    String text = (String) result;
                    res.setContentType("text/plain;charset=UTF-8");
                    try (PrintWriter out = res.getWriter()) {
                        out.println("Resultat de la methode:\n");
                        out.println(text);
                    }
                    return;
                }

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

    private Object[] buildArgs(Method method, HttpServletRequest req) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Param ann = p.getAnnotation(Param.class);
            String name = (ann != null && !ann.value().isEmpty()) ? ann.value() : p.getName();

            if (isSimpleType(p.getType())) {
                args[i] = convertParam(readValue(req, name, ann), p.getType());
            } else if (p.getType() == MySession.class) {
                args[i] = getMySession(req);
            } else if (p.getType() == Mapfiles.class) {
                args[i] = readFile(name, req);
            } else if (List.class.isAssignableFrom(p.getType())) {
                args[i] = bindList(p.getParameterizedType(), name, req);
            } else {
                args[i] = bindObject(p.getType(), name, req);
            }
        }
        return args;
    }

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
                } else if (fieldType == Mapfiles.class) {
                    f.set(obj, readFile(fullName, req));
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

    private List<Object> bindList(Type generic, String fullName, HttpServletRequest req) {
        Class<?> elementType = String.class;
        if (generic instanceof ParameterizedType) {
            Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (arg instanceof Class) {
                elementType = (Class<?>) arg;
            }
        }

        if (elementType == Mapfiles.class) {
            return new ArrayList<Object>(readFiles(fullName, req));
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

    private MySession getMySession(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        MySession mySession = sessions.computeIfAbsent(session.getId(), id -> new MySession());
        for (String name : Collections.list(session.getAttributeNames())) {
            mySession.setAttribute(name, session.getAttribute(name));
        }
        return mySession;
    }

    private void synchronizeSessions(Object[] args, HttpServletRequest req) {
        for (Object arg : args) {
            if (arg instanceof MySession) {
                MySession ms = (MySession) arg;
                HttpSession session = req.getSession(true);

                for (String name : Collections.list(session.getAttributeNames())) {
                    if (!ms.getSessionData().containsKey(name)) {
                        session.removeAttribute(name);
                    }
                }
                for (Map.Entry<String, Object> en : ms.getSessionData().entrySet()) {
                    session.setAttribute(en.getKey(), en.getValue());
                }
            }
        }
    }

    private boolean isMultipart(HttpServletRequest req) {
        String type = req.getContentType();
        return type != null && type.toLowerCase().startsWith("multipart/");
    }

    private Mapfiles readFile(String name, HttpServletRequest req) {
        if (!isMultipart(req)) {
            return null;
        }
        try {
            Part part = req.getPart(name);
            return part == null ? null : saveFile(part);
        } catch (IOException | ServletException e) {
            throw new RuntimeException("Erreur de lecture du fichier '" + name + "'", e);
        }
    }

    private List<Mapfiles> readFiles(String name, HttpServletRequest req) {
        List<Mapfiles> files = new ArrayList<>();
        if (!isMultipart(req)) {
            return files;
        }
        try {
            for (Part part : req.getParts()) {
                if (name.equals(part.getName())) {
                    Mapfiles file = saveFile(part);
                    if (file != null) {
                        files.add(file);
                    }
                }
            }
        } catch (IOException | ServletException e) {
            throw new RuntimeException("Erreur de lecture des fichiers '" + name + "'", e);
        }
        return files;
    }

    private Mapfiles saveFile(Part part) throws IOException {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isEmpty()) {
            return null;
        }

        String path = submitted.replace('\\', '/');
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty()) {
            return null;
        }

        if (uploadPath == null || uploadPath.isBlank()) {
            throw new IOException("Le parametre de contexte 'upload-path' est absent du web.xml");
        }
        Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir) || target.equals(dir)) {
            throw new IOException("Nom de fichier invalide : " + fileName);
        }

        byte[] bytes;
        try (InputStream in = part.getInputStream()) {
            bytes = in.readAllBytes();
        }
        Files.write(target, bytes);

        int dot = fileName.lastIndexOf('.');
        String extension = (dot > 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1) : "";
        return new Mapfiles(fileName, extension, bytes);
    }

    private Object convertParam(String raw, Class<?> type) {
        if (raw != null && raw.isEmpty() && type != String.class) {
            raw = null;
        }
        if (raw == null) {
            if (type == int.class || type == Integer.class) return 0;
            if (type == char.class || type == Character.class) return '-';
            if (type == boolean.class || type == Boolean.class) return false;
            if (type == double.class || type == Double.class) return 0.0;
            if (type == long.class || type == Long.class) return 0L;
            return null;
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