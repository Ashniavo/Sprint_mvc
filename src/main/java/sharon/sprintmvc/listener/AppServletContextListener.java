package sharon.sprintmvc.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.MySession;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;

public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[INIT] Tomcat demarre l'application. Lancement du scan des routes...");

        ServletContext appContext = sce.getServletContext();

        Map<String, MySession> sessions = new ConcurrentHashMap<>();
        appContext.setAttribute("sessions", sessions);

        String blockPackage = appContext.getInitParameter("package_list");
        String separator = appContext.getInitParameter("list_separator");
        String[] packageList = blockPackage.split(separator != null ? separator : ";");

        Set<Class<?>> withAnnotation = new HashSet<>();
        for (String pkg : packageList) {
            try {
                List<Class<?>> classes = Utils.loadClasses(pkg.trim(), Controller.class);
                withAnnotation.addAll(classes);
                System.out.println("[INFO] Package scanne : " + pkg.trim() + " -> " + classes.size() + " controller(s)");
            } catch (Exception e) {
                System.err.println("[ERREUR] Scan du package " + pkg + " : " + e.getMessage());
            }
        }

        List<Class<?>> classList = new ArrayList<>(withAnnotation);

        try {
            Map<UrlKey, Mapping> routesMapping = Utils.buildUrlMapping(classList);

            appContext.setAttribute("routesWithMethod", routesMapping);
            appContext.setAttribute("controllerList", classList);

            Map<Class<?>, Object> controllers = new HashMap<>();
            for (Class<?> controllerClass : classList) {
                try {
                    controllers.put(controllerClass, controllerClass.getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(
                            "Impossible d'instancier le controller " + controllerClass.getName(), e);
                }
            }
            appContext.setAttribute("controllers", controllers);
            System.out.println("[INFO] " + controllers.size() + " controller(s) instancie(s) (singleton)");

            String viewPrefix = appContext.getInitParameter("view.prefix");
            String viewSuffix = appContext.getInitParameter("view.suffix");
            appContext.setAttribute("prefix", viewPrefix);
            appContext.setAttribute("suffix", viewSuffix);

            System.out.println("[SUCCESS] " + routesMapping.size() + " routes chargees.");

        } catch (IllegalStateException e) {
            System.err.println("[ERREUR CRITIQUE] " + e.getMessage());
            throw new RuntimeException("Conflit de routes detecte.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SHUTDOWN] L'application s'arrete.");
    }
}